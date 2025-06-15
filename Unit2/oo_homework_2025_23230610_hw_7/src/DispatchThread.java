import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;

public class DispatchThread implements Runnable {
    private RequestTable globalRequestTable;
    private static ArrayList<RequestTable> requestTableList;
    private static ArrayList<RequestTable> DCrequestTableList;
    private int idx;

    public DispatchThread(ArrayList<RequestTable> requestTableList,
        ArrayList<RequestTable> dcRequestTableList, RequestTable globalRequestTable) {
        this.globalRequestTable = globalRequestTable;
        this.requestTableList = requestTableList;
        this.DCrequestTableList = dcRequestTableList;
        this.idx = 0;
    }

    @Override
    public void run() {
        while (true) {
            if (ElevatorThread.getSchedulingCount() == 0 && DCelevatorThread.getTransCount() == 0
                && ElevatorThread.getUpdateCount() == 0
                && globalRequestTable.isPersonRequestEmpty() && globalRequestTable.isEnd()) {
                for (RequestTable requestTable : requestTableList) {
                    requestTable.setEnd();
                }
                for (RequestTable requestTable : DCrequestTableList) {
                    requestTable.setEnd();
                }
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            PersonRequest personRequest = globalRequestTable.getOnePersonRequestAndRemove();
            if (personRequest == null) {
                continue;
            }

            averageDispatch(personRequest);
        }
    }

    public static RequestTable getDCrequestTable(String elevatorId) {
        return DCrequestTableList.get(Integer.parseInt(elevatorId) - 1);
    }

    public static RequestTable getRequestTable(String elevatorId) {
        return requestTableList.get(Integer.parseInt(elevatorId) - 1);
    }

    // 平均分配乘客请求
    private void averageDispatch(PersonRequest personRequest) {
        while (true) {
            // 平均分配乘客
            int elevatorId = idx % 6 + 1;
            idx = (idx + 1) % 6;
            if (!requestTableList.get(elevatorId - 1).isDCelevator()) {
                // 普通电梯
                synchronized (requestTableList.get(elevatorId - 1)) {
                    requestTableList.get(elevatorId - 1).
                            addPersonRequest(personRequest);
                    TimableOutput.println("RECEIVE-" +
                        personRequest.getPersonId() + "-" + elevatorId);
                }
                break;
            } else {
                // 双轿厢电梯，两个表都锁上
                synchronized (DCrequestTableList.get(elevatorId - 1)) {
                    int anotherElevatorId = requestTableList.get(elevatorId - 1).
                        getAnotherElevatorId();
                    String transFloor = requestTableList.get(elevatorId - 1).getTransFloor();
                    String abType = requestTableList.get(elevatorId - 1).getAbType();
                    boolean fromIsLowerThanTrans = RequestTable.getFloorValue(
                        personRequest.getFromFloor()) < RequestTable.getFloorValue(transFloor);
                    boolean fromIsHigherThanTrans = RequestTable.getFloorValue(
                        personRequest.getFromFloor()) > RequestTable.getFloorValue(transFloor);

                    boolean toIsLowerThanTrans = RequestTable.getFloorValue(
                        personRequest.getToFloor()) < RequestTable.getFloorValue(transFloor);
                    boolean toIsHigherThanTrans = RequestTable.getFloorValue(
                        personRequest.getToFloor()) > RequestTable.getFloorValue(transFloor);

                    boolean fromEqualTrans = RequestTable.getFloorValue(
                        personRequest.getFromFloor()) == RequestTable.getFloorValue(transFloor);

                    boolean condition1 = (abType.equals("B") && fromIsLowerThanTrans)
                        || (abType.equals("A") && fromIsHigherThanTrans);
                    boolean condition2 =
                        (abType.equals("A") && fromEqualTrans && toIsHigherThanTrans)
                        || (abType.equals("B") && fromEqualTrans && toIsLowerThanTrans);

                    boolean shouldTake = (condition1 || condition2)
                        && !DCrequestTableList.get(anotherElevatorId - 1).isUpdating();

                    synchronized (DCrequestTableList.get(anotherElevatorId - 1)) {
                        if (shouldTake) {
                            DCrequestTableList.get(elevatorId - 1).addPersonRequest(personRequest);
                            TimableOutput.println("RECEIVE-" +
                                personRequest.getPersonId() + "-" + elevatorId);
                            judgeTrans(personRequest, transFloor);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void judgeTrans(PersonRequest personRequest, String transFloor) {
        int fromFloor = RequestTable.getFloorValue(personRequest.getFromFloor());
        int toFloor = RequestTable.getFloorValue(personRequest.getToFloor());
        int transFloorValue = RequestTable.getFloorValue(transFloor);

        if (fromFloor < transFloorValue && toFloor > transFloorValue) {
            DCelevatorThread.increaseTransCount();
        } else if (fromFloor > transFloorValue && toFloor < transFloorValue) {
            DCelevatorThread.increaseTransCount();
        }
    }
}
