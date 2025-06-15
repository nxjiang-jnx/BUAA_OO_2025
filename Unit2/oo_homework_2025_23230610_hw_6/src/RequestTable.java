import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.ScheRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestTable {
    private final HashMap<String, ArrayList<PersonRequest>> personRequestMap;
    private final ArrayList<ScheRequest> scheRequestList;
    private boolean isEnd;

    public RequestTable() {
        personRequestMap = new HashMap<>();
        scheRequestList = new ArrayList<>();
        isEnd = false;
    }

    public synchronized HashMap<String, ArrayList<PersonRequest>> getPersonRequestMap() {
        return personRequestMap;
    }

    public synchronized void clearPersonRequestMap() {
        personRequestMap.clear();
    }

    public synchronized void addPersonRequest(PersonRequest personRequest) {
        if (personRequestMap.containsKey(personRequest.getFromFloor())) {
            personRequestMap.get(personRequest.getFromFloor()).add(personRequest);
        } else {
            ArrayList<PersonRequest> list = new ArrayList<>();
            list.add(personRequest);
            personRequestMap.put(personRequest.getFromFloor(), list);
        }
        notifyAll();
    }

    public synchronized void addScheRequest(ScheRequest scheRequest) {
        scheRequestList.add(scheRequest);
        notifyAll();
    }

    public synchronized boolean isPersonRequestEmpty() {
        return personRequestMap.isEmpty();
    }

    public synchronized boolean isScheRequestEmpty() {
        return scheRequestList.isEmpty();
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
        if (personRequestMap.containsKey(currentFloor)) {
            for (PersonRequest personRequest : personRequestMap.get(currentFloor)) {
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

    static int getFloorValue(String floor) {
        // 楼层为地下 B4-B1, 地上F1-F7
        if (floor.charAt(0) == 'B') {
            return -Integer.parseInt(floor.substring(1));
        } else {
            return Integer.parseInt(floor.substring(1));
        }
    }

    public synchronized boolean checkRequestIsTurnaround(String currentFloor, String direction) {
        if (isPersonRequestEmpty()) {
            return false;
        }
        for (String key : personRequestMap.keySet()) {
            if ((getFloorValue(key) > getFloorValue(currentFloor) && direction.equals("UP"))
                || (getFloorValue(key) < getFloorValue(currentFloor) && direction.equals("DOWN"))) {
                return false;
            }
        }
        notifyAll();
        return true;
    }

    public synchronized PersonRequest choosePerson(String currentFloor, String direction) {
        ArrayList<PersonRequest> curFloorRequestList = personRequestMap.get(currentFloor);

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
                personRequestMap.remove(currentFloor);
            }
            notifyAll();
        }

        return targetRequest;
    }

    public synchronized ScheRequest chooseScheRequest() {
        notifyAll();
        return scheRequestList.remove(0);
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized PersonRequest getOnePersonRequestAndRemove() {
        if (personRequestMap.isEmpty() && !isEnd()) {
            waitRequest();
        }
        if (personRequestMap.isEmpty()) {
            return null;
        }
        for (HashMap.Entry<String, ArrayList<PersonRequest>> entry : personRequestMap.entrySet()) {
            ArrayList<PersonRequest> list = entry.getValue();
            if (!list.isEmpty()) {
                PersonRequest person = list.remove(0);
                if (list.isEmpty()) {
                    personRequestMap.remove(entry.getKey());
                }
                notifyAll();
                return person;
            }
        }

        return null;
    }
}
