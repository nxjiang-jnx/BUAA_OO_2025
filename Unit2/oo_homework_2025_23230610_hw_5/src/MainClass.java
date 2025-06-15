import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        ArrayList<RequestTable> requestTableList = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            RequestTable elevatorRequestTable = new RequestTable();
            requestTableList.add(elevatorRequestTable);
            Thread elevatorThread = new Thread(new Elevator(elevatorRequestTable), "" + i);
            elevatorThread.start();
        }
        Thread dispatcherThread = new Thread(new Dispatcher(requestTableList));
        dispatcherThread.start();
    }
}