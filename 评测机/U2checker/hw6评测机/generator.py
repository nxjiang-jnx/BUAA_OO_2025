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


def chooseTime(base: float = 0.0):
    if base != 0.0:
        return max(base + 5, MAX_TIME * random() + 1)
    else:
        if INTENSIVE:
            return min(MAX_TIME * random() + 1, 50.0)
        else:
            return MAX_TIME


def chooseElevator():
    return randint(MIN_ELEVATOR, MAX_ELEVATOR)

def chooseSpeed():
    tmp = ["0.2", "0.3", "0.4", "0.5"]
    return tmp[randint(0, len(tmp) - 1)]

def choosePriority():
    return randint(MIN_PRIORITY, MAX_PRIORITY)

def genData(length=70, mutualTest=True):
    print("Generating data...")
    length = min(length, 30 * (MAX_ELEVATOR - MIN_ELEVATOR + 1))
    ans = []
    SPECIAL_START, SPECIAL_END = "F5", "B4"
    sche_cnt = 0
    last_sche = dict()
    for i in range(1, MAX_ELEVATOR + 1):
        last_sche[i] = 0.0
    alreadySche = dict()
    for i in range(1, MAX_ELEVATOR + 1):
        alreadySche[i] = False
    # ans is a list of time, id, start, end, by

    for i in range(length):
        if sche_cnt < 20 and randint(1, 10) < 4:  # 37分出sche
            id = chooseElevator()
            if (mutualTest and alreadySche[id]) or last_sche[id] >= MAX_TIME - 5:
                time = chooseTime()
                if randint(0, 1) == 0:
                    start, end = chooseFloor(MIN_FLOOR, MAX_FLOOR)
                else:
                    start, end = SPECIAL_START, SPECIAL_END
                    if randint(0, 1) == 0:
                        SPECIAL_START, SPECIAL_END = SPECIAL_END, SPECIAL_START
                priority = choosePriority()
                ans.append((time, f"[{time:.1f}]{i+1}-PRI-{priority}-FROM-{start}-TO-{end}\n"))

                continue

            if mutualTest:
                alreadySche[id] = True
            time = chooseTime(last_sche[id])
            last_sche[id] = time
            speed = chooseSpeed()
            sche_cnt += 1
            sche_floor = chooseFloor(3, 9)
            ans.append((time, f"[{time:.1f}]SCHE-{id}-{speed}-{sche_floor[0]}\n"))
        else:
            time = chooseTime()
            if randint(0, 1) == 0:
                start, end = chooseFloor(MIN_FLOOR, MAX_FLOOR)
            else:
                start, end = SPECIAL_START, SPECIAL_END
                if randint(0, 1) == 0:
                    SPECIAL_START, SPECIAL_END = SPECIAL_END, SPECIAL_START
            priority = choosePriority()
            ans.append((time, f"[{time:.1f}]{i+1}-PRI-{priority}-FROM-{start}-TO-{end}\n"))
    ans.sort(key=lambda x: x[0])
    return [item[1] for item in ans]


if __name__ == "__main__":
    print(genData(70))
