import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class DispatchThread implements Runnable {
    private RequestTable globalRequestTable;
    private final ArrayList<RequestTable> requestTableList;
    private int idx;

    public DispatchThread(ArrayList<RequestTable> requestTableList,
        RequestTable globalRequestTable) {
        this.globalRequestTable = globalRequestTable;
        this.requestTableList = requestTableList;
        this.idx = 0;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (requestTableList) {
                if (ElevatorThread.getSchedulingCount() == 0 &&
                    globalRequestTable.isPersonRequestEmpty() && globalRequestTable.isEnd()) {
                    for (RequestTable requestTable : requestTableList) {
                        requestTable.setEnd();
                    }
                    return;
                }
            }
            PersonRequest personRequest = globalRequestTable.getOnePersonRequestAndRemove();
            if (personRequest == null) {
                continue;
            }
            // 平均分配乘客
            int elevatorId = idx % 6 + 1;
            idx = (idx + 1) % 6;
            synchronized (requestTableList.get(elevatorId - 1)) {
                requestTableList.get(elevatorId - 1).
                        addPersonRequest(personRequest);
                TimableOutput.println("RECEIVE-" +
                    personRequest.getPersonId() + "-" + elevatorId);
            }
        }
    }
}
