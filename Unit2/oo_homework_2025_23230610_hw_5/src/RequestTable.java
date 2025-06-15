import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestTable {
    private final HashMap<String, ArrayList<PersonRequest>> requestMap;
    private boolean isEnd;

    public RequestTable() {
        requestMap = new HashMap<>();
        isEnd = false;
    }

    public synchronized void addPersonRequest(PersonRequest personRequest) {
        if (requestMap.containsKey(personRequest.getFromFloor())) {
            requestMap.get(personRequest.getFromFloor()).add(personRequest);
        } else {
            ArrayList<PersonRequest> list = new ArrayList<>();
            list.add(personRequest);
            requestMap.put(personRequest.getFromFloor(), list);
        }
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return requestMap.isEmpty();
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        if (isEnd) {
            notifyAll();
        }
        return isEnd;
    }

    public synchronized boolean checkRequestIsPickUp(String currentFloor, String direction) {
        if (requestMap.containsKey(currentFloor)) {
            for (PersonRequest personRequest : requestMap.get(currentFloor)) {
                if (getRequestDirection(personRequest).equals(direction)) {
                    notifyAll();
                    return true;
                }
            }
        }
        return false;
    }

    public String getRequestDirection(PersonRequest request) {
        return getFloorValue(request.getToFloor()) > getFloorValue(request.getFromFloor())
                ? "UP" : "DOWN";
    }

    private int getFloorValue(String floor) {
        // 楼层为地下 B4-B1, 地上F1-F7
        if (floor.charAt(0) == 'B') {
            return -Integer.parseInt(floor.substring(1));
        } else {
            return Integer.parseInt(floor.substring(1));
        }
    }

    public synchronized boolean checkRequestIsTurnaround(String currentFloor, String direction) {
        if (isEmpty()) {
            return false;
        }
        for (String key : requestMap.keySet()) {
            if ((getFloorValue(key) > getFloorValue(currentFloor) && direction.equals("UP"))
                || (getFloorValue(key) < getFloorValue(currentFloor) && direction.equals("DOWN"))) {
                return false;
            }
        }
        notifyAll();
        return true;
    }

    public synchronized PersonRequest choosePerson(String currentFloor, String direction) {
        ArrayList<PersonRequest> curFloorRequestList = requestMap.get(currentFloor);

        if (curFloorRequestList == null || curFloorRequestList.isEmpty()) {
            return null;
        }

        // 选取方向相同 & 优先级最高 & 距离最近的人
        PersonRequest targetRequest = curFloorRequestList.stream()
            .filter(personRequest -> getRequestDirection(personRequest).equals(direction))
            .sorted((a, b) -> {
                if (b.getPriority() != a.getPriority()) {
                    return b.getPriority() - a.getPriority();
                } else {
                    // 优先级相同，优先选距离近的
                    return direction.equals("UP")
                            ? getFloorValue(a.getToFloor()) - getFloorValue(b.getToFloor())
                            : getFloorValue(b.getToFloor()) - getFloorValue(a.getToFloor());
                }
            })
            .findFirst()
            .orElse(null);

        if (targetRequest != null) {
            curFloorRequestList.remove(targetRequest);
            if (curFloorRequestList.isEmpty()) {
                requestMap.remove(currentFloor);
            }
            notifyAll();
        }

        return targetRequest;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
