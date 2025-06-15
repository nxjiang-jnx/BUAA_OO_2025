from random import randint, random

MAX_FLOOR = 11
MIN_FLOOR = 1
MAX_ELEVATOR = 6
MIN_ELEVATOR = 1
MAX_PRIORITY = 100
MIN_PRIORITY = 1
INTENSIVE = True if randint(0, 1) == 0 else False
COMPRESSED = True if randint(0, 1) == 0 else False
MAX_TIME = 50 if not COMPRESSED else 10


def chooseFloor(_min, _max):
    start, end = randint(_min, _max), randint(_min, _max)
    while start == end:
        start, end = randint(_min, _max), randint(_min, _max)
    if (start == 1) :
        start = "B4"
    elif (start == 2) :
        start = "B3"
    elif (start == 3) :
        start = "B2"
    elif (start == 4) :
        start = "B1"
    else :
        start = "F" + str(start - 4)
        
    if (end == 1) :
        end = "B4"
    elif (end == 2) :
        end = "B3"
    elif (end == 3) :
        end = "B2"
    elif (end == 4) :
        end = "B1"
    else :
        end = "F" + str(end - 4)
    
    return start, end


def chooseTime():
    if INTENSIVE:
        return MAX_TIME * random() + 1
    else:
        return MAX_TIME


def chooseBy():
    return randint(MIN_ELEVATOR, MAX_ELEVATOR)


def choosePriority():
    return randint(MIN_PRIORITY, MAX_PRIORITY)


def genData(length=70):
    print("Generating data...")
    length = min(length, 30 * (MAX_ELEVATOR - MIN_ELEVATOR + 1))
    ans = []
    requests_by_elevator = [0 for _ in range(MAX_ELEVATOR + 1)]
    SPECIAL_START, SPECIAL_END = "F5", "B4"
    # ans is a list of time, id, start, end, by

    for i in range(length):
        time = chooseTime()
        if randint(0, 1) == 0:
            start, end = chooseFloor(MIN_FLOOR, MAX_FLOOR)
        else:
            start, end = SPECIAL_START, SPECIAL_END
            if randint(0, 1) == 0:
                SPECIAL_START, SPECIAL_END = SPECIAL_END, SPECIAL_START
        by = chooseBy()
        # 同一部电梯最多有30条相关的乘客请求
        while requests_by_elevator[by] >= 30:
            by = chooseBy()
        requests_by_elevator[by] += 1
        requests_by_elevator[0] += 1
        
        priority = choosePriority()
        ans.append((time, f"[{time:.1f}]{i+1}-PRI-{priority}-FROM-{start}-TO-{end}-BY-{by}\n"))
    ans.sort(key=lambda x: x[0])

    # for i in range(length):
    #     print(ans[i][1], end="")

    print("Data generated.")
    return [item[1] for item in ans]


if __name__ == "__main__":
    ans = genData(70)
