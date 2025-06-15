import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        ArrayList<RequestTable> requestTableList = new ArrayList<>();
        RequestTable globalRequestTable = new RequestTable();

        for (int i = 1; i <= 6; i++) {
            RequestTable elevatorRequestTable = new RequestTable();
            requestTableList.add(elevatorRequestTable);
            Thread elevatorThread =
                new Thread(new ElevatorThread(elevatorRequestTable, globalRequestTable), "" + i);
            elevatorThread.start();
        }

        Thread inputThread = new Thread(new InputThread(requestTableList, globalRequestTable));
        inputThread.start();

        Thread dispatcherThread =
            new Thread(new DispatchThread(requestTableList, globalRequestTable));
        dispatcherThread.start();

    }
}