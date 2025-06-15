import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.io.IOException;
import java.util.ArrayList;

public class Dispatcher implements Runnable {
    private final ElevatorInput elevatorInput;
    private final ArrayList<RequestTable> requestTableList;

    public Dispatcher(ArrayList<RequestTable> requestTableList) {
        this.elevatorInput = new ElevatorInput(System.in);
        this.requestTableList = requestTableList;
    }

    @Override
    public void run() {
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                for (RequestTable requestTable : requestTableList) {
                    requestTable.setEnd();
                }
                break;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    // 注意 MainClass 中直接 add 了，因此这里索引要为电梯 id-1
                    requestTableList.get(personRequest.getElevatorId() - 1).
                            addPersonRequest(personRequest);
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
