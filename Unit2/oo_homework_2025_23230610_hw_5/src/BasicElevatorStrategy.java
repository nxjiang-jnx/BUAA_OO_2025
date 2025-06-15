import com.oocourse.elevator1.PersonRequest;
import java.util.HashMap;

public class BasicElevatorStrategy implements ElevatorStrategy {
    @Override
    public Status decideStatus(String currentFloor, String direction, int currentPeopleCount,
        HashMap<Integer, PersonRequest> personInsideInfoMap, RequestTable requestTable) {
        boolean isFull = currentPeopleCount >= 6;

        boolean hasDropOff = personInsideInfoMap.values().stream()
            .anyMatch(p -> p.getToFloor().equals(currentFloor));

        boolean hasPickUp = !isFull && requestTable.checkRequestIsPickUp(currentFloor, direction);

        if (hasDropOff || hasPickUp) {
            return Status.OPEN;
        } else if (currentPeopleCount == 0 && requestTable.isEmpty() && requestTable.isEnd()) {
            return Status.END;
        } else if (currentPeopleCount == 0 && requestTable.isEmpty() && !requestTable.isEnd()) {
            return Status.WAIT;
        } else if (currentPeopleCount == 0 &&
            requestTable.checkRequestIsTurnaround(currentFloor, direction)) {
            return Status.TURNAROUND;
        } else {
            return Status.MOVE;
        }
    }
}
