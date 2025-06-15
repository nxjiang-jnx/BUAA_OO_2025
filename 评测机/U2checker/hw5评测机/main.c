#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <stdlib.h>

#define MAX_INPUT_INSTR_CNT 70
#define MAX_PERSON_ELEVATOR 6
#define ELEVATOR_CNT 6

typedef struct Person
{
        int personId;
        int start;
        int dest;
        int elevatorId;
} Person;
typedef struct Elevator
{
        Person data[MAX_PERSON_ELEVATOR];
        int dlen;
        int floor;
        int doorOpen;
        int openTime;
        int arriveTime;
} Elevator;
typedef char *String;

Person persons[MAX_INPUT_INSTR_CNT];
int correct[MAX_INPUT_INSTR_CNT];
int pLen = 0;
Elevator elevators[ELEVATOR_CNT + 1];

String newString(int size);
void readInputData(FILE *src);
int checkOutputData(FILE *src);
int floorStrToInt(const char* floor) ;

int main(int argc, char *argv[])
{
        String inputName;
        String outputName;

        if (argc == 3)
        {
                inputName = argv[1];
                outputName = argv[2];
        }
        else
        {
                inputName = "stdin.txt";
                outputName = "stdout.txt";
        }


        FILE *inputFile = fopen(inputName, "r");

        readInputData(inputFile);
        fclose(inputFile);

        FILE *outputFile = fopen(outputName, "r");
        int chkRes = checkOutputData(outputFile);

        if (chkRes == 0)
        {
                printf("Oh! Some errors.\n");
        }
        else
        {
                printf("Correct.\n");
        }
        fclose(outputFile);
        if (chkRes == 0)
        {
                return -1;
        }
        return 0;
}

int checkOutputString(String src);
String getLine(FILE *src);

int checkOutputData(FILE *src)
{
        String tmp = newString(200);

        for (int i = 1; i <= ELEVATOR_CNT; i += 1)
        {
                elevators[i].dlen = 0;
                elevators[i].floor = 5;
                elevators[i].doorOpen = 0;
        }
        memset(correct, 0, sizeof(correct));

        tmp = getLine(src);

        
        while (tmp != NULL)
        {
                if (checkOutputString(tmp) == 0)
                {
                        return 0;
                }
                tmp = getLine(src);
        }

        for (int i = 0; i < pLen; i += 1)
        {
                if (correct[i] == 0)
                {
                        printf("Person %d does not enter or exit the elevator.\n", persons[i].personId);
                        return 0;
                }
        }

        return 1;
}

String getLine(FILE *src)
{
    char ch;
    String tmp = newString(200);
    int i = 0;

    while (fscanf(src, "%c", &ch) != EOF) {
        if (ch == '\n') {
            break;
        }
        tmp[i++] = ch;
    }

    tmp[i] = '\0';

    if (i == 0 && feof(src)) {
        return NULL;
    }

    return tmp;
}


int passTime(String *src);
int checkArrive(String src, int arriveTime);
int checkOpen(String src, int openTime);
int checkClose(String src, int closeTime);
int checkPersonIn(String src);
int checkPersonOut(String src);

int checkOutputString(String src)
{
        int tmpTime = passTime(&src);
        // printf("%d\n", tmpTime);
        if (strncmp(src, "ARRIVE", 6) == 0)
        {
                int chkRes = checkArrive(src, tmpTime);
                if (chkRes == 0)
                {
                        return 0;
                }
        }
        else if (strncmp(src, "OPEN", 4) == 0)
        {
                int chkRes = checkOpen(src, tmpTime);
                if (chkRes == 0)
                {
                        return 0;
                }
        }
        else if (strncmp(src, "CLOSE", 5) == 0)
        {
                int chkRes = checkClose(src, tmpTime);
                if (chkRes == 0)
                {
                        return 0;
                }
        }
        else if (strncmp(src, "IN", 2) == 0)
        {
                int chkRes = checkPersonIn(src);
                if (chkRes == 0)
                {
                        return 0;
                }
        }
        else if (strncmp(src, "OUT", 3) == 0)
        {
                int chkRes = checkPersonOut(src);
                if (chkRes == 0)
                {
                        return 0;
                }
        }
        return 1;
}

int parseNextInt(String *src);
int findPersonInElevator(Elevator *ele, int pId);
void removePersonElevator(Elevator *ele, int pId);
int chkPersonCorrect(Person *src);
int personEql(Person *p0, Person *p1);

int checkPersonOut(String src)
{
    int pId = parseNextInt(&src);

    src += 1;

    // 解析楼层字符串
    char floorStr[4];
    int i = 0;
    while (*src != '-' && *src != '\0' && i < 3) {
        floorStr[i++] = *src;
        src++;
    }
    floorStr[i] = '\0';
    int place = floorStrToInt(floorStr);  // 转换为逻辑层编号

    if (*src == '-') src++;
    int eleId = parseNextInt(&src);

    int pIndex = -1;
    Elevator *curEle = &elevators[eleId];

    if (curEle->floor != place) {
        printf("Elevator%d, 111 Person%d: Not arrive at %s\n", eleId, pId, floorStr);
        return 0;
    }

    if (curEle->doorOpen == 0) {
        printf("Elevator%d, Person%d: Door not open at %s\n", eleId, pId, floorStr);
        return 0;
    }

    pIndex = findPersonInElevator(curEle, pId);
    if (pIndex == -1) {
        printf("Elevator%d, Person%d: Person not in elevator at %s\n", eleId, pId, floorStr);
        return 0;
    }

    Person *curPerson = &curEle->data[pIndex];
    curPerson->dest = place;

    if (!chkPersonCorrect(curPerson)) {
        printf("Person %d from %d to %d by %d is incorrect.\n",
               curPerson->personId,
               curPerson->start,
               curPerson->dest,
               curPerson->elevatorId);
        return 0;
    }

    removePersonElevator(curEle, pIndex);
    return 1;
}


int findPersonById(int id);

int chkPersonCorrect(Person *src)
{
        int place = findPersonById(src->personId);

        if (place == -1)
        {
                return 0;
        }

        int res = personEql(&persons[place], src);

        if (res == 0)
        {
                return 0;
        }

        if (correct[place] == 1)
        {
                printf("Another Person %d?\n", src->personId);
                return 0;
        }

        correct[place] = 1;
        return 1;
}

int findPersonById(int id)
{
        for (int i = 0; i < pLen; i += 1)
        {
                if (persons[i].personId == id)
                {
                        return i;
                }
        }
        return -1;
}

int personEql(Person *p0, Person *p1)
{
        return p0->dest == p1->dest && p0->elevatorId == p1->elevatorId && p0->personId == p1->personId &&
               p0->start == p1->start;
}

void personCpy(Person *dest, Person *src);

void removePersonElevator(Elevator *ele, int pIndex)
{
        for (int i = pIndex; i < ele->dlen; i += 1)
        {
                personCpy(&ele->data[i], &ele->data[i + 1]);
        }
        ele->dlen -= 1;
}

void personCpy(Person *dest, Person *src)
{
        dest->dest = src->dest;
        dest->elevatorId = src->elevatorId;
        dest->start = src->start;
        dest->personId = src->personId;
}

int findPersonInElevator(Elevator *ele, int pId)
{
        for (int i = 0; i < ele->dlen; i += 1)
        {
                if (ele->data[i].personId == pId)
                {
                        return i;
                }
        }
        return -1;
}

int checkPersonIn(String src)
{
    int pId = parseNextInt(&src);
    src += 1;

    // 提取楼层字符串
    char floorStr[4];
    int i = 0;
    while (*src != '-' && *src != '\0' && i < 3) {
        floorStr[i++] = *src;
        src++;
    }
    floorStr[i] = '\0';

    int place = floorStrToInt(floorStr);  // 转换为逻辑层号

    if (*src == '-') src++;
    int eleId = parseNextInt(&src);

    Elevator *curEle = &elevators[eleId];

    // 楼层不一致
    if (curEle->floor != place) {
        printf("Elevator%d, Person%d: Not arrive at %s\n", eleId, pId, floorStr);
        return 0;
    }

    // 门未打开
    if (curEle->doorOpen == 0) {
        printf("Elevator%d, Person%d: Door not open at %s\n", eleId, pId, floorStr);
        return 0;
    }

    // 超载
    if (curEle->dlen == MAX_PERSON_ELEVATOR) {
        printf("Elevator%d, Person%d: Full at %s\n", eleId, pId, floorStr);
        return 0;
    }

    // 添加乘客信息
    int tmpDlen = curEle->dlen;
    Person *curPerson = &curEle->data[tmpDlen];
    curPerson->start = place;
    curPerson->elevatorId = eleId;
    curPerson->personId = pId;
    curEle->dlen += 1;

    return 1;
}


int checkClose(String src, int closeTime)
{
        src += 6;
        char floorStr[4];
        int i = 0;
        while (*src != '-' && *src != '\0' && i < 3) {
                floorStr[i++] = *src;
                src++;
        }
        floorStr[i] = '\0';
        int place = floorStrToInt(floorStr);  // 转换为逻辑层

        if (*src == '-') src++;
        int eleId = parseNextInt(&src);

        if (elevators[eleId].doorOpen == 0)
        {
                printf("Elevator%d: Door close twice at %d.\n", eleId, place);
                return 0;
        }
        if (elevators[eleId].floor != place) {
                printf("Elevator%d: Incorrect floor.\n", eleId, place);
        		return 0;
		} 
        if (closeTime - elevators[eleId].openTime < 4000) {
        		printf("Elevator%d: Door close too fast.\n", eleId, place);
                return 0;
		}
        elevators[eleId].doorOpen = 0;

        return 1;
}

int checkOpen(String src, int openTime)
{
        src += 5;
        char floorStr[4];
        int i = 0;
        while (*src != '-' && *src != '\0' && i < 3) {
                floorStr[i++] = *src;
                src++;
        }
        floorStr[i] = '\0';
        int place = floorStrToInt(floorStr);  // 转换为逻辑层

        if (*src == '-') src++;
        int eleId = parseNextInt(&src);

        if (elevators[eleId].doorOpen == 1)
        {
                printf("Elevator%d: Door open twice at %d.\n", eleId, place);
                return 0;
        }
        if (elevators[eleId].floor != place) {
                printf("Elevator%d: Incorrect floor %s .\n", eleId, floorStr);
        		return 0;
		} 
        elevators[eleId].doorOpen = 1;
        elevators[eleId].openTime = openTime;

        return 1;
}

int floorStrToInt(const char* floor) {
        if (floor[0] == 'B') {
            int val = atoi(floor + 1);
            return 5 - val;  // B4=1, B3=2, B2=3, B1=4
        } else if (floor[0] == 'F') {
            int val = atoi(floor + 1);
            return 4 + val;  // F1=5, F2=6, ..., F7=11
        }
        return -1;  // invalid
}
    
    
int checkArrive(String src, int arriveTime)
{
    src += 7;
    // 提取楼层字符串（如 B3 或 F4）
    char floorStr[4];
    int i = 0;
    while (*src != '-' && *src != '\0' && i < 3) {
        floorStr[i++] = *src;
        src++;
    }
    floorStr[i] = '\0';

    int place = floorStrToInt(floorStr);  // 转换为逻辑楼层编号（1~11）

    // 提取电梯编号
    if (*src == '-') src++;
    int eleId = parseNextInt(&src);

    // 判断楼层是否合法
    if (place < 1 || place > 11) {
        printf("Elevator%d: Invalid floor %s.\n", eleId, floorStr);
        return 0;
    }

    // 判断是否只移动一层
    if (abs(place - elevators[eleId].floor) != 1) {
        printf("Elevator%d: Incorrectly moved from %d to %d.\n", eleId,
               elevators[eleId].floor, place);
        return 0;
    }

    // 判断门是否关闭
    if (elevators[eleId].doorOpen == 1) {
        printf("Elevator%d: Door not closed at floor %s.\n", eleId, floorStr);
        return 0;
    }

    // 判断移动时间 ≥ 4000ms（即 400ms）
    if (arriveTime - elevators[eleId].arriveTime < 4000) {
        printf("Elevator%d: Moved too fast to %s.\n", eleId, floorStr);
        return 0;
    }

    // 更新状态
    elevators[eleId].arriveTime = arriveTime;
    elevators[eleId].floor = place;
    return 1;
}


Person parsePerson(String src);

void readInputData(FILE *src)
{
        String tmp = newString(100);

        while (fscanf(src, "%s", tmp) != -1)
        {
                persons[pLen] = parsePerson(tmp);
                pLen += 1;
        }
}


Person parsePerson(String src)
{
    Person res;

    passTime(&src);                          // 跳过时间戳
    res.personId = parseNextInt(&src);      // 读取 ID

    // 跳过 -PRI-xxx
    while (strncmp(src, "PRI-", 4) != 0 && *src != '\0') src++;
    if (strncmp(src, "PRI-", 4) == 0) {
        src += 4;
        parseNextInt(&src);  // 跳过优先级值
    }

    // 解析 FROM-XXX
    while (strncmp(src, "FROM-", 5) != 0 && *src != '\0') src++;
    src += 5;
    char floorStr[4];
    int i = 0;
    while (*src != '-' && *src != '\0' && i < 3) {
        floorStr[i++] = *src;
        src++;
    }
    floorStr[i] = '\0';
    res.start = floorStrToInt(floorStr);

    // 解析 TO-XXX
    while (strncmp(src, "TO-", 3) != 0 && *src != '\0') src++;
    src += 3;
    i = 0;
    while (*src != '-' && *src != '\0' && i < 3) {
        floorStr[i++] = *src;
        src++;
    }
    floorStr[i] = '\0';
    res.dest = floorStrToInt(floorStr);

    // 解析 BY-x
    res.elevatorId = parseNextInt(&src);

    return res;
}


int isDigit(char ch) {
    return ch >= '0' && ch <= '9';
}

int passTime(String *src)
{
	int number = 0;
    int foundDigits = 0;
        while (**src != ']')
        {
	        if (!isDigit(**src)) {
	            (*src)++;
	            continue;
	        }
	        if (!foundDigits) {
	            while (!isDigit(**src) && **src != ']') {
	                (*src)++;
	            }
	            foundDigits = 1;
        	}
        	if (isDigit(**src)) {
            number = number * 10 + (**src - '0');
            (*src)++;
	        } else {
	            break;
	        }
        }
        *src += 1;
        
        return number;
}

int parseNextInt(String *src)
{
        int res = 0;
        while (!(**src <= '9' && **src >= '0'))
        {
                *src += 1;
        }
        while (**src <= '9' && **src >= '0')
        {
                res = res * 10 + **src - '0';
                *src += 1;
        }
        return res;
}

String newString(int size)
{
        return (String)malloc(size);
}
