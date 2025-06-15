import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Thread.sleep;

public class DCelevatorThread implements Runnable {
    private static final int MAX_PEOPLE_COUNT = 6;
    private static RequestTable globalRequestTable;
    private String abType;
    private String currentFloor;
    private String direction;
    private int currentPeopleCount;
    private final String transFloor;
    private final String dcElevatorId;
    private final Strategy strategy;
    private RequestTable requestTable;
    private final HashMap<Integer, PersonRequest> personInsideInfoMap;   //<乘客id，请求>

    private static int transCount = 0;

    // 另一个轿厢
    private RequestTable anotherRequestTable;

    // 用于管理目标楼层调度器
    private final TransFloorLock transFloorLock;

    public DCelevatorThread(String id, String abType, String transFloor,
        TransFloorLock lock, RequestTable globalRequestTable) {
        this.transFloor = transFloor;
        this.dcElevatorId = id;
        this.strategy = new BasicStrategy();
        this.requestTable = new RequestTable();
        this.personInsideInfoMap = new HashMap<>();
        this.abType = abType;
        this.currentFloor = abType.equals("A") ? FloorManager.aboveFloor(transFloor)
                : FloorManager.belowFloor(transFloor);
        this.direction = abType.equals("A") ? "UP" : "DOWN";
        this.requestTable.setAbType(abType);
        this.currentPeopleCount = 0;
        this.transFloorLock = lock;
        this.globalRequestTable = globalRequestTable;
    }

    @Override
    public void run() {
        while (true) {
            Status status;
            synchronized (requestTable) {
                status = strategy.decideStatus(currentFloor, direction, currentPeopleCount,
                        personInsideInfoMap, requestTable);
                if (status == Status.WAIT) {
                    requestTable.waitRequest();
                    continue;
                }
            }
            if (status == Status.END) {
                break;
            } else if (status == Status.OPEN) {
                DCelevatorOpenAndClose();
            } else if (status == Status.TURNAROUND) {
                DCelevatorTurnaround();
            } else if (status == Status.MOVE) {
                DCelevatorMove();
            }
        }
    }

    // 以下是双轿厢电梯行为
    public void DCelevatorOpenAndClose() {
        // 开门
        TimableOutput.println("OPEN-" + currentFloor + "-" + dcElevatorId);
        // 先下后上
        if (checkElevatorShouldDropOff()) {
            dropOff();
        }
        if (checkElevatorShouldPickUp()) {
            pickUp();
        }
        // 关门
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println("CLOSE-" + currentFloor + "-" + dcElevatorId);
    }

    public void DCelevatorTurnaround() {
        if (direction.equals("UP")) {
            direction = "DOWN";
        } else {
            direction = "UP";
        }
    }

    public void DCelevatorMove() {
        try {
            sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 先特判换乘楼层，要求A在上B在下
        if ((abType.equals("B") && direction.equals("UP") &&
            FloorManager.aboveFloor(currentFloor).equals(transFloor))
            || (abType.equals("A") && direction.equals("DOWN") &&
            FloorManager.belowFloor(currentFloor).equals(transFloor))) {
            synchronized (transFloorLock) {
                // 抵达换乘楼层，调头后清空轿厢并接新乘客
                currentFloor = FloorManager.nextFloor(currentFloor, direction);
                TimableOutput.println("ARRIVE-" + currentFloor + "-" + dcElevatorId);
                DCelevatorTurnaround();
                DCelevatorOpenAndClose();

                // 无条件移动一层，为另外轿厢腾出空间
                currentFloor = FloorManager.nextFloor(currentFloor, direction);
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                TimableOutput.println("ARRIVE-" + currentFloor + "-" + dcElevatorId);
            }
        } else {
            currentFloor = FloorManager.nextFloor(currentFloor, direction);
            TimableOutput.println("ARRIVE-" + currentFloor + "-" + dcElevatorId);
        }
    }

    // 以下是辅助方法
    public void setAnotherRequestTable(RequestTable anotherRequestTable) {
        this.anotherRequestTable = anotherRequestTable;
    }

    private boolean checkElevatorIsFull() {
        return currentPeopleCount >= MAX_PEOPLE_COUNT;
    }

    private boolean checkElevatorShouldPickUp() {
        // 电梯未满，且有新增乘坐需求
        return !checkElevatorIsFull() && requestTable.checkRequestIsPickUp(currentFloor, direction);
    }

    private boolean checkElevatorShouldDropOff() {
        for (PersonRequest personRequest : personInsideInfoMap.values()) {
            if (personRequest.getToFloor().equals(currentFloor)) {
                return true;
            }
        }
        // 电梯有乘客，则到换成楼层时强制清空
        return currentPeopleCount != 0 && currentFloor.equals(transFloor);
    }

    public void dropOff() {
        if (currentFloor.equals(transFloor)) {
            everyBodyOut();
        } else {
            Iterator<HashMap.Entry<Integer, PersonRequest>> iterator =
                personInsideInfoMap.entrySet().iterator();
            while (iterator.hasNext()) {
                PersonRequest personRequest = iterator.next().getValue();
                if (personRequest.getToFloor().equals(currentFloor)) {
                    TimableOutput.println("OUT-S-" + personRequest.getPersonId() + "-" +
                        currentFloor + "-" + dcElevatorId);
                    iterator.remove();  // 安全地从 Map 中删除
                    currentPeopleCount--;
                }
            }
        }
    }

    public void pickUp() {
        while (!checkElevatorIsFull()) {
            synchronized (requestTable) {
                PersonRequest personRequest = requestTable.choosePerson(currentFloor, direction);
                if (personRequest == null) {
                    break;
                }
                personInsideInfoMap.put(personRequest.getPersonId(), personRequest);
                currentPeopleCount++;
                TimableOutput.println("IN-" + personRequest.getPersonId() + "-" + currentFloor +
                    "-" + dcElevatorId);
            }
        }
    }

    public void everyBodyOut() {
        if (!personInsideInfoMap.isEmpty()) {
            synchronized (anotherRequestTable) {
                Iterator<HashMap.Entry<Integer, PersonRequest>> iterator =
                    personInsideInfoMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    PersonRequest personRequest = iterator.next().getValue();

                    if (personRequest.getToFloor().equals(currentFloor)) {
                        TimableOutput.println("OUT-S-" + personRequest.getPersonId() + "-" +
                            currentFloor + "-" + dcElevatorId);
                    } else {
                        TimableOutput.println("OUT-F-" + personRequest.getPersonId() + "-" +
                            currentFloor + "-" + dcElevatorId);
                        // 将未到达目的地的乘客加入另一轿厢候乘表，先更新此人的 fromFloor 为当前楼层
                        PersonRequest newPersonRequest = new PersonRequest(
                            currentFloor, personRequest.getToFloor(),
                            personRequest.getPersonId(), personRequest.getPriority());
                        globalRequestTable.addPersonRequest(newPersonRequest);
                        decreaseTransCount();
                    }
                    iterator.remove();
                    currentPeopleCount--;
                }
            }
        }
    }

    public void setRequestTable(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public void setAnotherElevatorId(String anotherElevatorId) {
        requestTable.setAnotherElevatorId(anotherElevatorId);
    }

    public static int getTransCount() {
        return transCount;
    }

    public static synchronized void increaseTransCount() {
        transCount++;
    }

    public static synchronized void decreaseTransCount() {
        transCount--;
    }
}
