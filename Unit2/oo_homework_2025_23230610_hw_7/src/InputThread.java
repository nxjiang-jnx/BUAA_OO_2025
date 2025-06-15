import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.PersonRequest;

import java.io.IOException;
import java.util.ArrayList;

public class InputThread implements Runnable {
    private final ElevatorInput elevatorInput;
    private RequestTable globalRequestTable;
    private final ArrayList<RequestTable> requestTableList;

    public InputThread(ArrayList<RequestTable> requestTableList, RequestTable globalRequestTable) {
        this.elevatorInput = new ElevatorInput(System.in);
        this.globalRequestTable = globalRequestTable;
        this.requestTableList = requestTableList;
    }

    @Override
    public void run() {
        while (true) {
            Request request = elevatorInput.nextRequest();

            if (request == null) {
                globalRequestTable.setEnd();
                break;
            } else if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                globalRequestTable.addPersonRequest(personRequest);
            } else if (request instanceof ScheRequest) {
                ElevatorThread.increaseSchedulingCount();
                requestTableList.get(((ScheRequest) request).getElevatorId() - 1).
                        addScheRequest((ScheRequest) request);
            } else if (request instanceof UpdateRequest) {
                ElevatorThread.increaseUpdateCount();
                UpdateRequest updateRequest = (UpdateRequest) request;
                Object lock = new Object();
                requestTableList.get(updateRequest.getElevatorAId() - 1).
                    addUpdateRequest(updateRequest, "A", lock);
                requestTableList.get(updateRequest.getElevatorBId() - 1).
                    addUpdateRequest(updateRequest, "B", lock);
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
