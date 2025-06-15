import com.oocourse.elevator2.PersonRequest;

import java.util.HashMap;

public interface Strategy {
    Status decideStatus(String currentFloor, String direction, int currentPeopleCount,
        HashMap<Integer, PersonRequest> personInsideInfoMap, RequestTable requestTable);
}
