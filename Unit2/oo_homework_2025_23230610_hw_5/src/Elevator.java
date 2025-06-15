import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class Elevator implements Runnable {
    private static final int MAX_PEOPLE_COUNT = 6;
    private String currentFloor;
    private int currentPeopleCount;
    private String direction;
    private final ElevatorStrategy strategy;
    private final RequestTable requestTable;
    private final HashMap<Integer, PersonRequest> personInsideInfoMap;   //<乘客id，请求>

    // 用于量子电梯
    private long lastDepartureTime;

    public Elevator(RequestTable requestTable) {
        this.currentFloor = "F1";
        this.currentPeopleCount = 0;
        this.direction = "UP";
        this.strategy = new BasicElevatorStrategy();
        this.requestTable = requestTable;
        this.personInsideInfoMap = new HashMap<>();
        this.lastDepartureTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (true) {
            Status status = checkStatus();
            if (status == Status.END) {
                break;
            } else if (status == Status.OPEN) {
                openAndClose();
            } else if (status == Status.TURNAROUND) {
                turnaround();
            } else if (status == Status.MOVE) {
                move();
            } else {
                pause();
            }
        }
    }

    // 以下是辅助方法
    private Status checkStatus() {
        return strategy.decideStatus(currentFloor, direction, currentPeopleCount,
                personInsideInfoMap, requestTable);
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

        // 更新电梯出发时间
        lastDepartureTime = System.currentTimeMillis();
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
            HashMap.Entry<Integer, PersonRequest> entry = iterator.next();
            PersonRequest personRequest = entry.getValue();
            if (personRequest.getToFloor().equals(currentFloor)) {
                TimableOutput.println("OUT-" + personRequest.getPersonId() + "-" +
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
        // 构造量子电梯
        long currentTime = System.currentTimeMillis();
        long waitingTime = 400 + lastDepartureTime - currentTime;
        if (waitingTime < 0) {
            waitingTime = 0;
        }

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

        try {
            sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println("ARRIVE-" + currentFloor + "-" + currentThread().getName());

        // 更新电梯出发时刻
        lastDepartureTime = System.currentTimeMillis();
    }

    public void pause() {
        requestTable.waitRequest();
    }
}
