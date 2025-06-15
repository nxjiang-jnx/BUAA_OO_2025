import re

DEBUG_FLAG = '#'


class Elevator:
    def __init__(self):
        self.passengersInside = dict()
        self.floor = 5      #  初始楼层为F1 = 5
        self.doorOpen = False
        self.received = set()
        self.scheduling = False
        self.capacity = 6
        self.speed = 0.399
        self.toSche = False
        self.moves = 0
        self.arriveTime = 0
        self.openTime = 0
        self.acceptTime = 0
        #self.toSetCapacity = 0
        self.ScheTime = 0
        self.ScheFloor = 5
        self.scheDoorOpened = False
        self.scheOpenTime = 0
        self.scheCloseTime = 0
        self.configSpeed = 0.399

    pass


class ElevatorSystem:
    def __init__(self):
        self.ELEVATOR_CNT = 6
        self.passengers = dict()
        self.passengers_id = set()
        self.elevators = [0 if i == 0 else Elevator() for i in range(self.ELEVATOR_CNT + 1)]
        
    # 定义一个楼层字符串转为整数索引的映射函数
    @staticmethod
    def floor_to_int(floor_str):
        if floor_str.startswith('B'):
            return 5 - int(floor_str[1])  # B4 -> 1, B3 -> 2, B2 -> 3, B1 -> 4
        elif floor_str.startswith('F'):
            return int(floor_str[1:]) + 4  # F1 -> 5, F2 -> 6, ..., F7 -> 11
        else:
            raise ValueError(f"Invalid floor format: {floor_str}")

    def parse_person(self, tmp_input):
        pattern = r'(\d+)-PRI-(\d+)-FROM-([BF]\d+)-TO-([BF]\d+)'

        # 使用re.search()方法查找匹配项
        match = re.search(pattern, tmp_input)

        # 如果匹配成功，提取三个整数
        if match:
            self.passengers[int(match.group(1))] = [self.floor_to_int(match.group(3)), self.floor_to_int(match.group(4))]
            self.passengers_id.add(int(match.group(1)))
        else:
            print("Failed to match person request!")

    def check_arrive(self, tmp_output, tmp_time):
        parts = tmp_output.split('-')
        if len(parts) == 3:
            floor, elevator_id = self.floor_to_int(parts[1]), int(parts[2])
            if len(self.elevators[elevator_id].received) == 0 and self.elevators[elevator_id].scheduling == False:
                print(f"Elevator{elevator_id}: moved when no receives.")
                return False
            if self.elevators[elevator_id].scheduling and self.elevators[elevator_id].moves >= 2:
                print(f"Elevator{elevator_id}: moved too much after sche accept.")
                return False
            if abs(floor - self.elevators[elevator_id].floor) != 1 or floor < 1 or floor > 11:
                print(f"Elevator{elevator_id}: Illegal move to {floor}.")
                return False
            if self.elevators[elevator_id].doorOpen:
                print(f"Elevator{elevator_id}: Door not closed.")
                return False
            if tmp_time - self.elevators[elevator_id].arriveTime < self.elevators[elevator_id].speed - 0.001:
                print(f"Elevator{elevator_id}: Moved too Fast (faster than {self.elevators[elevator_id].speed}).")
                return False
            self.elevators[elevator_id].arriveTime = tmp_time
            self.elevators[elevator_id].floor = floor
            if self.elevators[elevator_id].toSche and self.elevators[elevator_id].scheduling == False:
                self.elevators[elevator_id].moves += 1
            return True
        else:
            return False

    def check_open(self, tmp_output, tmp_time):
        parts = tmp_output.split('-')
        if len(parts) == 3:
            floor, elevator_id = self.floor_to_int(parts[1]), int(parts[2])
            elev = self.elevators[elevator_id]

            # 如果是在调度状态，但不是调度目标楼层，不能开门
            if elev.scheduling and floor != elev.ScheFloor:
                print(f"Elevator{elevator_id}: opened at {floor} during scheduling, not at scheduled floor {elev.ScheFloor}.")
                return False

            # 正常开门检查
            if elev.floor != floor:
                print(f"Elevator{elevator_id}: Opened at incorrect floor {floor}.")
                return False
            if elev.doorOpen:
                print(f"Elevator{elevator_id}: Door opened twice at floor {floor}.")
                return False

            elev.doorOpen = True
            elev.openTime = tmp_time

            # 在调度目标楼层开门时，记录调度开门时间
            if elev.toSche and floor == elev.ScheFloor:
                elev.scheDoorOpened = True
                elev.scheOpenTime = tmp_time

            return True
        return False


    def check_close(self, tmp_output, tmp_time):
        # 解析输出字符串以获取电梯ID
        parts = tmp_output.split('-')
        if len(parts) == 3:
            floor, elevator_id = self.floor_to_int(parts[1]), int(parts[2])
            # 检查电梯门是否已经打开
            if not self.elevators[elevator_id].doorOpen:
                print(f"Elevator{elevator_id}: Door closed twice without being open.")
                return False
            # 检查电梯是否已经在指定楼层
            if self.elevators[elevator_id].floor != floor:
                print(f"Elevator{elevator_id}: Opened at incorrect floor {floor}.")
                return False
            # 检查电梯门关闭的时间间隔是否合理
            if tmp_time - self.elevators[elevator_id].openTime < 0.399:
                print(f"Elevator{elevator_id}: Door close too fast.")
                return False
            # 更新电梯的关门状态
            self.elevators[elevator_id].doorOpen = False
            
            if self.elevators[elevator_id].toSche and floor == self.elevators[elevator_id].ScheFloor:
                # 如果电梯正在重置，更新关门时间
                self.elevators[elevator_id].scheCloseTime = tmp_time
            
            return True
        else:
            return False

    def check_in(self, tmp_output):
        # 解析输出字符串以获取乘客ID、电梯ID和楼层
        parts = tmp_output.split('-')
        if len(parts) == 4:
            person_id, floor, elevator_id = int(parts[1]), self.floor_to_int(parts[2]), int(parts[3])
            # 检查电梯是否正在重置
            if self.elevators[elevator_id].scheduling:
                print(f"Elevator{elevator_id}: person in when scheduling.")
                return False
            if person_id not in self.elevators[elevator_id].received:
                print(f"Elevator{elevator_id}: no such person {person_id} in request.")
                return False
            # 检查电梯是否有足够的空间容纳新的乘客
            if len(self.elevators[elevator_id].passengersInside) >= self.elevators[elevator_id].capacity:
                print(f"Elevator{elevator_id}: Full at {floor}.")
                return False
            # 检查电梯是否在指定楼层
            if self.elevators[elevator_id].floor != floor:
                print(f"Elevator{elevator_id}: Not arrive at {floor}.")
                return False
            # 检查电梯门是否打开
            if not self.elevators[elevator_id].doorOpen:
                print(f"Elevator{elevator_id}: Door not open at {floor}.")
                return False
            # 将乘客添加到电梯中
            self.elevators[elevator_id].passengersInside[person_id] = self.passengers.pop(person_id)
            return True
        else:
            return False

    def check_out(self, tmp_output):
        parts = tmp_output.split('-')
        if len(parts) == 5 and parts[1] in ('S', 'F'):
            mode, person_id, floor, elevator_id = parts[1], int(parts[2]), self.floor_to_int(parts[3]), int(parts[4])
            if mode == 'S':
                return self.check_out_success(person_id, floor, elevator_id)
            elif mode == 'F':
                return self.check_out_force(person_id, floor, elevator_id)
        return False
    
    def check_out_success(self, person_id, floor, elevator_id):
        if person_id not in self.elevators[elevator_id].passengersInside:
            print(f"Elevator{elevator_id}: Person {person_id} not in elevator.")
            return False
        if not self.elevators[elevator_id].doorOpen:
            print(f"Elevator{elevator_id}: Door not open for person {person_id} to get out.")
            return False
        if self.elevators[elevator_id].floor != floor:
            print(f"Elevator{elevator_id}: Not arrive at {floor}.")
            return False
        try:
            self.elevators[elevator_id].received.remove(person_id)
        except KeyError:
            print(f"Elevator{elevator_id}: Person {person_id} No received.")
            return False
        self.passengers_id.add(person_id)
        tmp_person = self.elevators[elevator_id].passengersInside.pop(person_id)
        tmp_person[0] = floor
        self.passengers[person_id] = tmp_person
        return True

    def check_out_force(self, person_id, floor, elevator_id):
        # 临时调度强制下车，无需received匹配
        if person_id not in self.elevators[elevator_id].passengersInside:
            print(f"Elevator{elevator_id}: Person {person_id} not in elevator (FORCE OUT).")
            return False
        if not self.elevators[elevator_id].doorOpen:
            print(f"Elevator{elevator_id}: Door not open at force out.")
            return False
        if self.elevators[elevator_id].floor != floor:
            print(f"Elevator{elevator_id}: Wrong floor at force out.")
            return False
        self.passengers_id.add(person_id)
        tmp_person = self.elevators[elevator_id].passengersInside.pop(person_id)
        tmp_person[0] = floor
        self.passengers[person_id] = tmp_person
        return True

    def check_receive(self, tmp_output):
        # 解析输出字符串以获取乘客ID和电梯ID
        parts = tmp_output.split('-')
        if len(parts) == 3:
            person_id, elevator_id = int(parts[1]), int(parts[2])
            # 检查电梯是否正在重置
            if self.elevators[elevator_id].scheduling:
                print(f"Elevator{elevator_id}: cannot receive person {person_id} when scheduling.")
                return False
            # 将乘客添加到电梯的接收列表中
            try:
                self.passengers_id.remove(person_id)
            except KeyError:
                print(f"Elevator{elevator_id}: Person {person_id} already received.")
                return False
            self.elevators[elevator_id].received.add(person_id)
            return True
        else:
            return False

    def sche_config(self, tmp_output, tmp_time):
        # 解析输出字符串以获取电梯ID
        parts = tmp_output.split('-')
        if len(parts) == 5:
            elevator_id, speed, sche_floor = int(parts[2]), float(parts[3]), self.floor_to_int(parts[4])
            # 开始更改电梯配置
            self.elevators[elevator_id].toSche = True
            self.elevators[elevator_id].acceptTime = tmp_time
            self.elevators[elevator_id].moves = 0
            self.elevators[elevator_id].ScheFloor = sche_floor
            self.elevators[elevator_id].configSpeed = speed
            return True
        else:
            return False

    def check_sche_begin(self, tmp_output, tmp_time):
        # 解析输出字符串以获取电梯ID
        parts = tmp_output.split('-')
        if len(parts) == 3:
            elevator_id = int(parts[2])
            # 检查电梯是否已经接受了重置请求
            if not self.elevators[elevator_id].toSche:
                print(f"Elevator{elevator_id}: No reset request when begin reset.")
                return False
            # 检查电梯是否为空
            #if len(self.elevators[elevator_id].passengersInside) != 0:
            #    print(f"Elevator{elevator_id}: Elevator not empty when begin reset.")
            #    return False
            # 检查电梯门是否已经关闭
            if self.elevators[elevator_id].doorOpen:
                print(f"Elevator{elevator_id}: Elevator not closed when begin reset.")
                return False
            # 标记电梯开始重置
            self.elevators[elevator_id].speed = self.elevators[elevator_id].configSpeed
            self.elevators[elevator_id].scheduling = True
            self.elevators[elevator_id].ScheTime = tmp_time
            for id in self.elevators[elevator_id].received:
                self.passengers_id.add(id)
            self.elevators[elevator_id].received = set()
            return True
        else:
            return False

    def check_sche_end(self, tmp_output, tmp_time):
        parts = tmp_output.split('-')
        if len(parts) == 3:
            elevator_id = int(parts[2])
            elev = self.elevators[elevator_id]

            if not elev.scheduling or not elev.toSche:
                print(f"Elevator{elevator_id}: No resetting state when end reset.")
                return False
            if tmp_time - elev.acceptTime > 6.01:
                print(f"Elevator{elevator_id}: Response too slow.")
                return False

            # 检查是否到达目标楼层并开关门 >= 1s
            if elev.floor != elev.ScheFloor:
                print(f"Elevator{elevator_id}: Did not reach scheduled floor {elev.ScheFloor}.")
                return False
            if not hasattr(elev, 'scheDoorOpened') or not elev.scheDoorOpened:
                print(f"Elevator{elevator_id}: Did not open door at target floor during scheduling.")
                return False
            if elev.scheCloseTime - elev.scheOpenTime < 0.999:
                print(f"Elevator{elevator_id}: Door open time too short during scheduling.")
                return False

            # 必须无人且门关闭
            if len(elev.passengersInside) != 0:
                print(f"Elevator{elevator_id}: Not empty at SCHE-END.")
                return False
            if elev.doorOpen:
                print(f"Elevator{elevator_id}: Door not closed at SCHE-END.")
                return False

            elev.toSche = False
            elev.scheduling = False
            elev.speed = 0.399
            return True
        return False


    def check_all_arrived(self):
        for id, passenger in self.passengers.items():
            if passenger[0] != passenger[1]:
                print(f"Person {id} not arrived.")
                return False
        return True


def parse_time_stamp(tmp_str: str):
    to_parse = tmp_str.split(']')[0]
    try:
        ret = float(to_parse[1:])
        return ret
    except ValueError:
        return None


def check_output_string(line: str, system: ElevatorSystem, debug: bool):
    tmp_time = parse_time_stamp(line)
    if tmp_time is None:
        return False
    tmp_output = line.split(']')[1]
    if tmp_output.startswith("ARRIVE"):
        return system.check_arrive(tmp_output, tmp_time)
    if tmp_output.startswith("OPEN"):
        return system.check_open(tmp_output, tmp_time)
    if tmp_output.startswith("CLOSE"):
        return system.check_close(tmp_output, tmp_time)
    if tmp_output.startswith("IN"):
        return system.check_in(tmp_output)
    if tmp_output.startswith("OUT"):
        return system.check_out(tmp_output)
    if tmp_output.startswith("RECEIVE"):
        return system.check_receive(tmp_output)
    if tmp_output.startswith("SCHE-ACCEPT"):
        return system.sche_config(tmp_output, tmp_time)
    if tmp_output.startswith("SCHE-BEGIN"):
        return system.check_sche_begin(tmp_output, tmp_time)
    if tmp_output.startswith("SCHE-END"):
        return system.check_sche_end(tmp_output, tmp_time)
    if tmp_output.startswith(DEBUG_FLAG) and debug:
        return True
    return False


def check(input_path="stdin.txt", output_path="stdout.txt", debug=False):
    mySystem = ElevatorSystem()
    with open(input_path, "r") as fin:
        for line in fin.readlines():
            tmp_input = line.split(']')[1]
            if not tmp_input.startswith("SCHE"):
                mySystem.parse_person(tmp_input)

    with open(output_path, "r") as fout:
        for line in fout.readlines():
            if not check_output_string(line, mySystem, debug):
                print(line, end="")
                return False
        if not mySystem.check_all_arrived():
            return False

    # print("Everything's fine.")
    return True


def main():
    print(check(input_path="stdin.txt", output_path="stdout.txt", debug=False))


if __name__ == "__main__":
    main()
