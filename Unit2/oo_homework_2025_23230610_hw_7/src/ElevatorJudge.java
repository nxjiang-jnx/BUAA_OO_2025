import com.oocourse.elevator3.ScheRequest;

import java.io.*;
        import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

public class ElevatorJudge {
    static class PassengerRequest {
        double timestamp;
        int id, pri;
        String from, to;

        PassengerRequest(double timestamp, int id, int pri, String from, String to) {
            this.timestamp = timestamp;
            this.id = id;
            this.pri = pri;
            this.from = from;
            this.to = to;
        }

        public String toString() {
            return String.format("[%.1f]%d-PRI-%d-FROM-%s-TO-%s", timestamp, id, pri, from, to);
        }
    }

    static class ScheRequest1 {
        double timestamp;
        int elevatorId;
        double speed;
        String targetFloor;

        ScheRequest1(double timestamp, int elevatorId, double speed, String targetFloor) {
            this.timestamp = timestamp;
            this.elevatorId = elevatorId;
            this.speed = speed;
            this.targetFloor = targetFloor;
        }

        public String toString() {
            return String.format("[%.1f]SCHE-%d-%.1f-%s", timestamp, elevatorId, speed, targetFloor);
        }
    }

    static class UpdateRequest1 {
        double timestamp;
        int elevatorAId;
        int elevatorBId;
        String transformerFloor;

        public UpdateRequest1(double timestamp, int elevatorAId, int elevatorBId, String transformerFloor) {
            this.timestamp = timestamp;
            this.elevatorAId = elevatorAId;
            this.elevatorBId = elevatorBId;
            this.transformerFloor = transformerFloor;
        }

        public String toString() {
            return String.format("[%.1f]UPDATE-%d-%d-%s", timestamp, elevatorAId, elevatorBId, transformerFloor);
        }
    }

    static Random rand = new Random();

    static List<Object> generateInput() {
        List<Object> requests = new ArrayList<>();
        int totalPassengerRequests = rand.nextInt(100) + 100;
        int totalUpdateRequests = rand.nextInt(3) + 1;
        double currentTime = 0;
        List<Integer> updateMatch = range(1, 7).boxed().collect(Collectors.toList());
        Collections.shuffle(updateMatch);
        ArrayList<Double> timeLimit = range(1, 7).mapToDouble(i -> 47.0).boxed().collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < totalUpdateRequests; i++) {
            currentTime = rand.nextDouble() * 60.0;
            int elevatorAId = updateMatch.get(2 * i);
            int elevatorBId = updateMatch.get(2 * i + 1);
            String[] possibleFloors = {"B2", "B1", "F1", "F2", "F3", "F4", "F5"};
            String transferFloor = possibleFloors[rand.nextInt(possibleFloors.length)];
            requests.add(new UpdateRequest1(currentTime, elevatorAId, elevatorBId, transferFloor));
            timeLimit.set(elevatorAId - 1, currentTime - 8.0);
            timeLimit.set(elevatorBId - 1, currentTime - 8.0);
        }
        currentTime = 0;
        for (int i = 1; i <= totalPassengerRequests; i++) {
            currentTime += rand.nextDouble() * (120.0 / totalPassengerRequests);
            int pri = rand.nextInt(100) + 1;
            String from = randomFloor();
            String to;
            do {
                to = randomFloor();
            } while (to.equals(from));

            requests.add(new PassengerRequest(currentTime, i, pri, from, to));
        }

        int totalScheRequests = rand.nextInt(10) + 10;
        double[] lastScheTime = {-7.0, -7.0, -7.0, -7.0, -7.0, -7.0, -7.0};

        for (int i = 0; i < totalScheRequests; i++) {
            int elevatorId = 0;
            Collections.shuffle(updateMatch);
            for (Integer id : updateMatch) {
                if (lastScheTime[id] > timeLimit.get(id - 1) - 10.0) {
                    continue;
                }
                elevatorId = id;
                break;
            }
            if (elevatorId == 0) {
                break;
            }
            double scheTime = lastScheTime[elevatorId] + 8.0 + 2.0 * rand.nextDouble();
            lastScheTime[elevatorId] = scheTime;
            for (int j = 1; j <= 6; j++) {
                if (j != elevatorId) {
                    lastScheTime[j] += rand.nextDouble() * 2.0;
                }
            }
            double[] possibleSpeeds = {0.2, 0.3, 0.4, 0.5};
            String[] possibleFloors = {"B2", "B1", "F1", "F2", "F3", "F4", "F5"};

            double speed = possibleSpeeds[rand.nextInt(possibleSpeeds.length)];
            String targetFloor = possibleFloors[rand.nextInt(possibleFloors.length)];

            requests.add(new ScheRequest1(scheTime, elevatorId, speed, targetFloor));
        }

        requests.sort(Comparator.comparingDouble(req -> (req instanceof PassengerRequest)
                ? ((PassengerRequest) req).timestamp
                : ((req instanceof ScheRequest1) ? ((ScheRequest1) req).timestamp : ((UpdateRequest1) req).timestamp)));

        return requests;
    }

    static String randomFloor() {
        String[] floors = {"B4", "B3", "B2", "B1", "F1", "F2", "F3", "F4", "F5", "F6", "F7"};
        return floors[rand.nextInt(floors.length)];
    }

    static void saveRequestsToFile(List<Object> requests, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (Object r : requests) {
            writer.write(r.toString());
            writer.newLine();
        }
        writer.close();
    }

    // 新增部分
    static class Elevator {
        boolean doorOpen = false;
        String currentFloor = "F1";
        int passengerCount = 0;
        boolean inSche = false;
        double scheStartTime = 0;
        double scheEndTime = 0;
        String scheTargetFloor = "";
        double scheSpeed = 0.4;
        int arriveAfterScheAccept = 0;
        Set<Integer> receiveSet = new HashSet<>();
    }

    static class PassengerStatus {
        String location;
        boolean completed = false;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 100; i++) {
            List<Object> requests = generateInput();
            String filename = "E:\\myFolder_3\\25春\\OO\\testData\\" + i + ".txt";
            saveRequestsToFile(requests, filename);
            System.out.println("----- Elevator Judge Result " + i + " -----");

            long start = System.currentTimeMillis();

            ProcessBuilder pb = new ProcessBuilder("java", "-cp",
                    "E:\\myFolder_3\\25春\\OO\\oo_homework_2025_23230610_hw_7\\out\\production\\oo_homework_2025_23230610_hw_7;" +
                            "E:\\myFolder_3\\25春\\OO\\oo_homework_2025_23230610_hw_7\\officialTool\\elevator3.jar",
                    "TestMain");

            pb.redirectErrorStream(false); // 分离 stdout 与 stderr
            System.out.println("Start()");
            Process p = pb.start();

            // 准备 passenger map
            HashMap<Integer, PassengerRequest> passengerRequestMap = new HashMap<>();
            StringBuilder inputBuilder = new StringBuilder();
            for (Object r : requests) {
                inputBuilder.append(r.toString()).append("\n");
                if (r instanceof PassengerRequest) {
                    PassengerRequest pr = (PassengerRequest) r;
                    passengerRequestMap.put(pr.id, pr);
                }
            }

            // 写入输入（标准输入）
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            bw.write(inputBuilder.toString());
            bw.flush();
            bw.close();
            System.out.println("✅ 请求写入完成");

            // 读取标准输出
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            List<String> output = new ArrayList<>();

            Thread outThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdout.readLine()) != null) {
                        System.out.println("[ELEVATOR] " + line);
                        output.add(line.trim());
                    }
                } catch (IOException e) {
                    System.err.println("❌ 读取 stdout 失败: " + e.getMessage());
                }
            });

            Thread errThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stderr.readLine()) != null) {
                        System.err.println("[ELEVATOR ERROR] " + line);
                    }
                } catch (IOException e) {
                    System.err.println("❌ 读取 stderr 失败: " + e.getMessage());
                }
            });

            outThread.start();
            errThread.start();

            int exitCode = p.waitFor();
            outThread.join();
            errThread.join();

            long end = System.currentTimeMillis();
            double cpuTime = (end - start) / 1000.0;

            System.out.println("⏹ 子进程退出码: " + exitCode);
            System.out.println("📦 收到输出行数: " + output.size());

            ValidateOutput.checkByList(Arrays.asList(inputBuilder.toString().split("\n")), output);

            System.out.println("CPU运行时间: " + cpuTime + " 秒");
            System.out.println("请求数: " + passengerRequestMap.size());
            System.out.println("✅ 通过测试");
            System.out.println();

        }
    }

}
