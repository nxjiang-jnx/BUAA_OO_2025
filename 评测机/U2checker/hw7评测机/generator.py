from random import randint, random

# ---------------- 全局常量 ----------------
MAX_FLOOR = 11
MIN_FLOOR = 1
MAX_ELEVATOR = 6
MIN_ELEVATOR = 1
MAX_PRIORITY = 100
MIN_PRIORITY = 1
INTENSIVE = True if randint(0, 1) == 0 else False
COMPRESSED = True if randint(0, 1) == 0 else False
MAX_TIME = 30 if not COMPRESSED else 10
UPPER_LIMIT = MAX_TIME

# ---------------- 基础随机工具 ----------------
def chooseFloor(_min, _max):
    start, end = randint(_min, _max), randint(_min, _max)
    while start == end:
        start, end = randint(_min, _max), randint(_min, _max)

    def idx2str(idx):
        if idx == 1:
            return "B4"
        elif idx == 2:
            return "B3"
        elif idx == 3:
            return "B2"
        elif idx == 4:
            return "B1"
        else:
            return "F" + str(idx - 4)

    return idx2str(start), idx2str(end)


def chooseTime(base: float = 0.0, upper: float = UPPER_LIMIT):
    if base != 0.0:
        t = max(base + 5, MAX_TIME * random() + 1)
    else:
        t = min(MAX_TIME * random() + 1, 50.0) if INTENSIVE else MAX_TIME
    return min(t, upper)


def chooseElevator():
    return randint(MIN_ELEVATOR, MAX_ELEVATOR)


def choosePriority():
    return randint(MIN_PRIORITY, MAX_PRIORITY)


def chooseSpeed():
    return ["0.2", "0.3", "0.4", "0.5"][randint(0, 3)]


# ---------------- 数据生成主函数 ----------------
def genData(length=70, mutualTest=True):
    print("Generating data ...")
    length = min(length, 30 * (MAX_ELEVATOR - MIN_ELEVATOR + 1))

    ans = []
    passenger_id = 1
    first_time = round(1 + 4 * random(), 1)
    start, end = chooseFloor(MIN_FLOOR, MAX_FLOOR)
    ans.append((first_time,
                f"[{first_time:.1f}]{passenger_id}-PRI-{choosePriority()}-FROM-{start}-TO-{end}\n"))
    passenger_id += 1
    current_len = 1

    SPECIAL_START, SPECIAL_END = "F5", "B4"
    last_sche_time = {i: -100.0 for i in range(1, MAX_ELEVATOR + 1)}
    sche_used = set()
    converted = set()
    already_update_sent = set()
    pending_update_time = dict()  # 新增：记录未来即将 UPDATE 的电梯及时间
    last_important_time = 0.0
    sche_cnt = 0
    update_cnt = 0
    i = 0

    while current_len < length:
        upd_prob = 4 if i < length * 0.4 else 1
        can_generate_update = (
            update_cnt < 10 and
            len(converted) <= MAX_ELEVATOR - 2 and
            randint(1, 10) <= upd_prob
        )

        if can_generate_update:
            free_elevators = [e for e in range(1, MAX_ELEVATOR + 1)
                              if e not in converted and e not in already_update_sent]
            if len(free_elevators) >= 2:
                A = free_elevators[randint(0, len(free_elevators) - 1)]
                B = free_elevators[randint(0, len(free_elevators) - 1)]
                while A == B:
                    B = free_elevators[randint(0, len(free_elevators) - 1)]
                if A > B:
                    A, B = B, A

                base = max(last_important_time,
                           last_sche_time[A] + 8,
                           last_sche_time[B] + 8)
                time = round(base + 0.1 + 2 * random(), 1)
                if time <= UPPER_LIMIT:
                    target_floor, _ = chooseFloor(3, 9)
                    ans.append((time, f"[{time:.1f}]UPDATE-{A}-{B}-{target_floor}\n"))
                    already_update_sent.update([A, B])
                    converted.update([A, B])
                    sche_used.update([A, B])
                    pending_update_time[A] = time
                    pending_update_time[B] = time
                    last_important_time = time
                    update_cnt += 1
                    current_len += 1
                    i += 1
                    continue

        can_generate_sche = (
            sche_cnt < 20 and randint(1, 10) <= 2
        )
        if can_generate_sche:
            # ✅ 修复点：筛选合法电梯
            legal_elevators = []
            for eid in range(1, MAX_ELEVATOR + 1):
                if eid in converted or eid in sche_used:
                    continue
                base = max(last_important_time, last_sche_time[eid] + 8)
                time = chooseTime(base=base, upper=UPPER_LIMIT)
                if eid in pending_update_time and pending_update_time[eid] <= time:
                    continue
                legal_elevators.append((eid, time, base))  # 保存合法选项

            if legal_elevators:
                eid, time, base = legal_elevators[randint(0, len(legal_elevators) - 1)]
                if time <= UPPER_LIMIT:
                    last_sche_time[eid] = time
                    sche_used.add(eid)
                    speed = chooseSpeed()
                    sche_floor = chooseFloor(3, 9)[0]
                     #ans.append((time, f"[{time:.1f}]SCHE-{eid}-{speed}-{sche_floor}\n"))
                    last_important_time = time
                    sche_cnt += 1
                    current_len += 1
                    i += 1
                    continue



        time = chooseTime(upper=UPPER_LIMIT)
        if time <= UPPER_LIMIT:
            if randint(0, 1) == 0:
                start, end = chooseFloor(MIN_FLOOR, MAX_FLOOR)
            else:
                start, end = SPECIAL_START, SPECIAL_END
                if randint(0, 1) == 0:
                    SPECIAL_START, SPECIAL_END = SPECIAL_END, SPECIAL_START
            ans.append((time,
                        f"[{time:.1f}]{passenger_id}-PRI-{choosePriority()}-FROM-{start}-TO-{end}\n"))
            passenger_id += 1
            current_len += 1
        i += 1

    if update_cnt == 0:
        cand = [e for e in range(1, MAX_ELEVATOR + 1)]
        A = cand[0]; B = cand[1]
        if A > B:
            A, B = B, A
        base = max(last_sche_time[A] + 8, last_sche_time[B] + 8, 6.0)
        time = min(base + 0.1, UPPER_LIMIT - 0.1)
        target_floor, _ = chooseFloor(3, 9)
        ans.append((time, f"[{time:.1f}]UPDATE-{A}-{B}-{target_floor}\n"))
        converted.update([A, B])
        already_update_sent.update([A, B])
        sche_used.update([A, B])
        pending_update_time[A] = time
        pending_update_time[B] = time
        update_cnt += 1
        current_len += 1

    ans.sort(key=lambda x: x[0])
    return [item[1] for item in ans]


if __name__ == "__main__":
    print(genData(70))