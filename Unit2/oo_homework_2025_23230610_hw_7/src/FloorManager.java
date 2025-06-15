public class FloorManager {
    public static String nextFloor(String currentFloor, String direction) {
        String target;
        if (direction.equals("UP")) {
            if (currentFloor.equals("B1")) {
                target = "F1";
            } else if (currentFloor.charAt(0) == 'B') {
                target = "B" + (Integer.parseInt(currentFloor.substring(1)) - 1);
            } else {
                target = "F" + (Integer.parseInt(currentFloor.substring(1)) + 1);
            }
        } else {
            if (currentFloor.equals("F1")) {
                target = "B1";
            } else if (currentFloor.charAt(0) == 'F') {
                target = "F" + (Integer.parseInt(currentFloor.substring(1)) - 1);
            } else {
                target = "B" + (Integer.parseInt(currentFloor.substring(1)) + 1);
            }
        }
        return target;
    }

    public static String aboveFloor(String currentFloor) {
        String target;
        if (currentFloor.equals("B1")) {
            target = "F1";
        } else if (currentFloor.charAt(0) == 'B') {
            target = "B" + (Integer.parseInt(currentFloor.substring(1)) - 1);
        } else {
            target = "F" + (Integer.parseInt(currentFloor.substring(1)) + 1);
        }
        return target;
    }

    public static String belowFloor(String currentFloor) {
        String target;
        if (currentFloor.equals("F1")) {
            target = "B1";
        } else if (currentFloor.charAt(0) == 'F') {
            target = "F" + (Integer.parseInt(currentFloor.substring(1)) - 1);
        } else {
            target = "B" + (Integer.parseInt(currentFloor.substring(1)) + 1);
        }
        return target;
    }
}
