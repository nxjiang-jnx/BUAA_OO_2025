import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class ElevatorThread implements Runnable {
    private static final int MAX_PEOPLE_COUNT = 6;
    private static RequestTable globalRequestTable;
    private static int schedulingCount = 0;
    private int moveTime;
    private String currentFloor;
    private int currentPeopleCount;
    private String direction;
    private final Strategy strategy;
    private final RequestTable requestTable;
    private final HashMap<Integer, PersonRequest> personInsideInfoMap;   //<乘客id，请求>

    public ElevatorThread(RequestTable requestTable, RequestTable globalRequestTable) {
        this.globalRequestTable = globalRequestTable;
        this.moveTime = 400;
        this.currentFloor = "F1";
        this.currentPeopleCount = 0;
        this.direction = "UP";
        this.strategy = new BasicStrategy();
        this.requestTable = requestTable;
        this.personInsideInfoMap = new HashMap<>();
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
                openAndClose();
            } else if (status == Status.TURNAROUND) {
                turnaround();
            } else if (status == Status.MOVE) {
                move();
            } else if (status == Status.SCHE) {
                scheduleTemporarily();
            }
        }
    }

    // 以下是辅助方法
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
        return false;
    }

    private void openAndClose() {
        // 开门
        TimableOutput.println("OPEN-" + currentFloor + "-" + currentThread().getName());
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
        TimableOutput.println("CLOSE-" + currentFloor + "-" + currentThread().getName());
    }

    public void pickUp() {
        while (!checkElevatorIsFull()) {
            PersonRequest personRequest = requestTable.choosePerson(currentFloor, direction);
            if (personRequest == null) {
                break;
            }
            personInsideInfoMap.put(personRequest.getPersonId(), personRequest);
            currentPeopleCount++;
            TimableOutput.println("IN-" + personRequest.getPersonId() + "-" + currentFloor + "-" +
                currentThread().getName());
        }
    }

    public void dropOff() {
        Iterator<HashMap.Entry<Integer, PersonRequest>> iterator =
            personInsideInfoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next().getValue();
            if (personRequest.getToFloor().equals(currentFloor)) {
                TimableOutput.println("OUT-S-" + personRequest.getPersonId() + "-" +
                    currentFloor + "-" + currentThread().getName());
                iterator.remove();  // 安全地从 Map 中删除
                currentPeopleCount--;
            }
        }
    }

    public void turnaround() {
        if (direction.equals("UP")) {
            direction = "DOWN";
        } else {
            direction = "UP";
        }
    }

    public void move() {
        // 电梯移动至下一层
        currentFloor = nextFloor();

        try {
            sleep(moveTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println("ARRIVE-" + currentFloor + "-" + currentThread().getName());
    }

    public void scheduleTemporarily() {
        synchronized (requestTable) {
            synchronized (globalRequestTable) {
                // 所有人全部下电梯
                everybodyOut();

                // 清空当前候乘表
                requestTable.getPersonRequestMap().values().stream()
                        .flatMap(ArrayList::stream)
                        .forEach(globalRequestTable::addPersonRequest);
                requestTable.clearPersonRequestMap();

                // 临时调度开始
                TimableOutput.println("SCHE-BEGIN-" + currentThread().getName());
            }

            ScheRequest scheRequest = requestTable.chooseScheRequest();
            // 重置电梯速度，前往调度请求的楼层
            String toFloor = scheRequest.getToFloor();
            // 判断电梯是否应该调头
            if ((direction.equals("UP") &&
                RequestTable.getFloorValue(toFloor) < RequestTable.getFloorValue(currentFloor)) ||
                (direction.equals("DOWN") &&
                RequestTable.getFloorValue(toFloor) > RequestTable.getFloorValue(currentFloor))) {
                turnaround();
            }
            // 更改电梯速度
            moveTime = (int)(scheRequest.getSpeed() * 1000);
            // 电梯移动至调度请求的楼层
            while (!currentFloor.equals(toFloor)) {
                move();
            }
            // 电梯开门
            TimableOutput.println("OPEN-" + currentFloor + "-" + currentThread().getName());
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 电梯关门
            TimableOutput.println("CLOSE-" + currentFloor + "-" + currentThread().getName());
            // 临时调度结束
            TimableOutput.println("SCHE-END-" + currentThread().getName());
            // 更改电梯速度
            moveTime = 400;

            decreaseSchedulingCount();
        }
    }

    public String nextFloor() {
        // 楼层为地下 B4-B1, 地上F1-F7
        if (direction.equals("UP")) {
            if (currentFloor.equals("B1")) {
                currentFloor = "F1";
            } else if (currentFloor.charAt(0) == 'B') {
                currentFloor = "B" + (Integer.parseInt(currentFloor.substring(1)) - 1);
            } else {
                currentFloor = "F" + (Integer.parseInt(currentFloor.substring(1)) + 1);
            }
        } else {
            if (currentFloor.equals("F1")) {
                currentFloor = "B1";
            } else if (currentFloor.charAt(0) == 'F') {
                currentFloor = "F" + (Integer.parseInt(currentFloor.substring(1)) - 1);
            } else {
                currentFloor = "B" + (Integer.parseInt(currentFloor.substring(1)) + 1);
            }
        }
        return currentFloor;
    }

    private void everybodyOut() {
        if (!personInsideInfoMap.isEmpty()) {
            TimableOutput.println("OPEN-" + currentFloor + "-" + currentThread().getName());
            long startTime = System.currentTimeMillis();
            Iterator<HashMap.Entry<Integer, PersonRequest>> iterator =
                personInsideInfoMap.entrySet().iterator();
            while (iterator.hasNext()) {
                PersonRequest personRequest = iterator.next().getValue();

                if (personRequest.getToFloor().equals(currentFloor)) {
                    TimableOutput.println("OUT-S-" + personRequest.getPersonId() + "-" +
                        currentFloor + "-" + currentThread().getName());
                } else {
                    TimableOutput.println("OUT-F-" + personRequest.getPersonId() + "-" +
                        currentFloor + "-" + currentThread().getName());
                    // 将未到达目的地的乘客重新加入候乘表，先更新此人的 fromFloor 为当前楼层
                    PersonRequest newPersonRequest = new PersonRequest(
                        currentFloor, personRequest.getToFloor(),
                        personRequest.getPersonId(), personRequest.getPriority());
                    globalRequestTable.addPersonRequest(newPersonRequest);
                }
                iterator.remove();
                currentPeopleCount--;
            }
            long waitingTime = 400 + startTime - System.currentTimeMillis();
            if (waitingTime < 0) {
                waitingTime = 0;
            }
            try {
                sleep(waitingTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println("CLOSE-" + currentFloor + "-" + currentThread().getName());
        }
    }

    public static int getSchedulingCount() {
        return schedulingCount;
    }

    public static synchronized void increaseSchedulingCount() {
        schedulingCount++;
    }

    public static synchronized void decreaseSchedulingCount() {
        schedulingCount--;
    }
}