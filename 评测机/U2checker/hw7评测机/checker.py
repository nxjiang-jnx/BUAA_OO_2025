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
        
        # --- UPDATE 双轿厢 ---
        self.updatePending = False          # <<< UPDATE‑MOD
        self.updating = False               # <<< UPDATE‑MOD (BEGIN~END)
        self.updateAcceptTime = 0.0         # <<< UPDATE‑MOD
        self.updateBeginTime = 0.0          # <<< UPDATE‑MOD
        self.convertedType = '0'            # '0'| 'A' | 'B'  <<< UPDATE‑MOD
        self.transferFloor = 0              # <<< UPDATE‑MOD 目标楼层

    def is_converted(self):
        return self.convertedType in ('A', 'B')

    def in_valid_range(self, new_floor: int):
        """双轿厢运行范围合法性"""
        if self.convertedType == 'A':        # 上轿厢：transfer ~ 11
            return new_floor >= self.transferFloor
        if self.convertedType == 'B':        # 下轿厢：1 ~ transfer
            return new_floor <= self.transferFloor
        return True

    pass

class UpdateRequest:                        # <<< UPDATE‑MOD
    """记录一次 UPDATE 改造请求的三段状态"""
    def __init__(self, a_id, b_id, target, accept_time):
        self.a_id = a_id
        self.b_id = b_id
        self.targetFloor = target
        self.acceptTime = accept_time
        self.arrCnt = {a_id: 0, b_id: 0}
        self.beginTime = None
        self.endTime = None
        self.begun = False
        self.ended = False

class ElevatorSystem:
    def __init__(self):
        self.ELEVATOR_CNT = 6
        self.passengers = dict()
        self.passengers_id = set()
        self.elevators = [0 if i == 0 else Elevator() for i in range(self.ELEVATOR_CNT + 1)]
        
        # --- UPDATE 全局 ---
        self.updateReqs = dict()        # key: (a,b) (a<b) -> UpdateRequest
        
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
            
    def update_accept(self, out: str, t: float):
        # 格式 UPDATE-ACCEPT-A-B-Fx
        parts = out.split('-')
        if len(parts) != 5:
            return False
        a_id, b_id = int(parts[2]), int(parts[3])
        if a_id > b_id:
            a_id, b_id = b_id, a_id
        target = self.floor_to_int(parts[4])

        # 不能重复
        if (a_id, b_id) in self.updateReqs:
            print("Duplicate UPDATE-ACCEPT.")
            return False
        # 两部电梯均未改造过且未在调度
        for eid in (a_id, b_id):
            if self.elevators[eid].is_converted():
                print(f"Elevator{eid} already converted.")
                return False
            if self.elevators[eid].updatePending or self.elevators[eid].updating:
                print(f"Elevator{eid} already in UPDATE.")
                return False

        # 记录请求
        req = UpdateRequest(a_id, b_id, target, t)
        self.updateReqs[(a_id, b_id)] = req
        for eid in (a_id, b_id):
            elev = self.elevators[eid]
            elev.updatePending = True
            elev.updateAcceptTime = t
        return True

    def update_begin(self, out: str, t: float):
        # 格式 UPDATE-BEGIN-A-B
        parts = out.split('-')
        if len(parts) != 4:
            return False
        a_id, b_id = int(parts[2]), int(parts[3])
        if a_id > b_id:
            a_id, b_id = b_id, a_id
        key = (a_id, b_id)
        if key not in self.updateReqs:
            print("No corresponding UPDATE-ACCEPT.")
            return False
        req = self.updateReqs[key]
        if req.begun:
            print("UPDATE already begun.")
            return False
        # 响应时限
        if t - req.acceptTime > 6.0 + 1e-3:
            print("UPDATE response time >6s.")
            return False
        # 每部电梯 ARRIVE 次数 ≤2
        if req.arrCnt[a_id] > 2 or req.arrCnt[b_id] > 2:
            print("Too many ARRIVE before UPDATE-BEGIN.")
            return False
        # 电梯静止、门关、人清空
        for eid in (a_id, b_id):
            elev = self.elevators[eid]
            if elev.doorOpen:
                print(f"Elevator{eid}: door not closed at UPDATE-BEGIN.")
                return False
            if len(elev.passengersInside) != 0:
                print(f"Elevator{eid}: not empty at UPDATE-BEGIN.")
                return False

        # ---------- 释放未上车的 RECEIVE 乘客 ----------  # <<< RELEASE‑MOD
        for eid in (a_id, b_id):
            elev = self.elevators[eid]
            for pid in elev.received:
                self.passengers_id.add(pid)      # 重新回到候乘表
            elev.received.clear()                # 清空接收列表
            
        # 设置状态
        req.begun = True
        req.beginTime = t
        for eid in (a_id, b_id):
            elev = self.elevators[eid]
            elev.updating = True
            elev.updateBeginTime = t
        return True
   
    def update_end(self, out: str, t: float):
        # 格式 UPDATE-END-A-B
        parts = out.split('-')
        if len(parts) != 4:
            return False
        a_id, b_id = int(parts[2]), int(parts[3])
        if a_id > b_id:
            a_id, b_id = b_id, a_id
        key = (a_id, b_id)
        if key not in self.updateReqs:
            print("No UPDATE record when END.")
            return False
        req = self.updateReqs[key]
        if not req.begun or req.ended:
            print("UPDATE-END order error.")
            return False
        # 时间约束
        if t - req.beginTime < 0.999:
            print("UPDATE duration <1s.")
            return False
        if t - req.acceptTime > 6.0 + 1e-3:
            print("UPDATE total time >6s.")
            return False
        # 电梯仍应静默且空 & 门关
        for eid in (a_id, b_id):
            elev = self.elevators[eid]
            if elev.doorOpen or len(elev.passengersInside) != 0:
                print(f"Elevator{eid}: not closed/empty at UPDATE-END.")
                return False

        # 完成改造：设置 convertedType / 速度 / 位置
        target = req.targetFloor
        self.elevators[a_id].convertedType = 'A'
        self.elevators[b_id].convertedType = 'B'
        for eid in (a_id, b_id):
            elev = self.elevators[eid]
            elev.transferFloor = target
            elev.speed = 0.199
            elev.updatePending = False
            elev.updating = False
        # 初始位置
        self.elevators[a_id].floor = target + 1
        self.elevators[b_id].floor = target - 1

        req.ended = True
        req.endTime = t
        return True
    
    def _invalidate_if_updating(self, elevator_id: int, action: str):
        elev = self.elevators[elevator_id]
        if elev.updating:
            print(f"Elevator{elevator_id}: {action} during UPDATE.")
            return True
        return False
     
    def check_arrive(self, tmp_output, tmp_time):
        parts = tmp_output.split('-')
        if len(parts) != 3:
            return False

        floor, elevator_id = self.floor_to_int(parts[1]), int(parts[2])
        elev = self.elevators[elevator_id]

        # ---------- 若正在 UPDATE‑BEGIN~END，任何 ARRIVE 都非法 ----------
        if self._invalidate_if_updating(elevator_id, "ARRIVE"):
            return False

        # ---------- 双轿厢范围 / 轿厢碰撞检查 ----------
        if elev.is_converted():
            if not elev.in_valid_range(floor):
                print(f"Elevator{elevator_id}: out of range after conversion.")
                return False
            mate_id = self._find_mate(elevator_id)
            mate_floor = self.elevators[mate_id].floor
            if floor == mate_floor:
                print(f"Elevator{elevator_id}: collision same floor with mate.")
                return False
            if elev.convertedType == 'B' and floor >= mate_floor:
                print(f"Elevator{elevator_id}: B above A (collision).")
                return False

        # ---------- 运动合法性 ----------
        if abs(floor - elev.floor) != 1 or floor < 1 or floor > 11:
            print(f"Elevator{elevator_id}: Illegal move to {floor}.")
            return False
        if elev.doorOpen:
            print(f"Elevator{elevator_id}: Door not closed.")
            return False
        if tmp_time - elev.arriveTime < elev.speed - 0.001:
            print(f"Elevator{elevator_id}: Moved too fast (>{1/elev.speed:.2f}f/s).")
            return False

        # ---------- “无 RECEIVE 移动” 检查 ----------
        allow_leave_transfer = False                         # <<< UPDATE‑MOD
        if elev.is_converted():
            # 允许在没有 RECEIVE 的情况下，仅从 transferFloor 离开 1 层
            if elev.floor == elev.transferFloor and \
               abs(floor - elev.transferFloor) == 1:
                allow_leave_transfer = True

        if len(elev.received) == 0 and not elev.scheduling and not allow_leave_transfer:
            print(f"Elevator{elevator_id}: moved when no receives.")
            return False

        # ---------- SCHE 期间移动计数 ----------
        if elev.toSche and not elev.scheduling:
            elev.moves += 1
        elev.arriveTime = tmp_time
        elev.floor = floor
        return True


    def check_open(self, tmp_output, tmp_time):
        parts = tmp_output.split('-')
        if len(parts) == 3:
            floor, elevator_id = self.floor_to_int(parts[1]), int(parts[2])
            elev = self.elevators[elevator_id]
            
            # 更新期间不能开门
            if self._invalidate_if_updating(elevator_id, "OPEN"):
                return False

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
            
            if self._invalidate_if_updating(elevator_id, "CLOSE"):
                return False
        
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
            
            if self._invalidate_if_updating(elevator_id, "IN"):
                return False
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
            if self._invalidate_if_updating(elevator_id, "OUT"):
                return False
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
            
            if self._invalidate_if_updating(elevator_id, "RECEIVE"):
                return False
        
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
        
    def _count_arrive_for_update(self, eid: int):
        elev = self.elevators[eid]
        if not elev.updatePending or elev.updating:
            return
        # 找对应请求
        for req in self.updateReqs.values():
            if eid == req.a_id or eid == req.b_id:
                req.arrCnt[eid] += 1

    def _find_mate(self, eid: int):
        # 遍历updateReqs字典中的所有值
        for req in self.updateReqs.values():
            # 如果eid等于req.a_id，则返回req.b_id
            if eid == req.a_id:
                return req.b_id
            # 如果eid等于req.b_id，则返回req.a_id
            if eid == req.b_id:
                return req.a_id
        # 如果没有找到匹配的eid，则返回0
        return 0  # 不应发生

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
    if tmp_output.startswith("UPDATE-ACCEPT"):
        return system.update_accept(tmp_output, tmp_time)
    if tmp_output.startswith("UPDATE-BEGIN"):
        return system.update_begin(tmp_output, tmp_time)
    if tmp_output.startswith("UPDATE-END"):
        return system.update_end(tmp_output, tmp_time)
    if tmp_output.startswith(DEBUG_FLAG) and debug:
        return True
    return False


def check(input_path="stdin.txt", output_path="stdout.txt", debug=False):
    mySystem = ElevatorSystem()
    with open(input_path, "r") as fin:
        for line in fin.readlines():
            tmp_input = line.split(']')[1]
            if tmp_input.startswith("UPDATE"):
                continue
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
