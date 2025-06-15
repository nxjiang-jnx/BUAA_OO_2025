import com.oocourse.elevator1.PersonRequest;

import java.util.HashMap;

public interface ElevatorStrategy {
    Status decideStatus(String currentFloor, String direction, int currentPeopleCount,
        HashMap<Integer, PersonRequest> personInsideInfoMap, RequestTable requestTable);
}
