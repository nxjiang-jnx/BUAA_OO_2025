import random
import sympy
import re

# 常量池，可自行调整以提高表达式多样性
intPool = [0,1,2,3,4,5,6,7,8,9]  

hasWhiteSpace = True    # 是否插入空白字符
hasLeadZeros = True     # 数字是否有前导零
maxTerm = 10             # 表达式中的最大项数，可调大
maxFactor = 5           # 单项中最大因子数，可调大

def rd(a,b):
    return random.randint(a,b)

def getWhiteSpace():
    if not hasWhiteSpace:
        return ""
    s = ""
    cnt = rd(0,2)  # 随机插入 0~2 个空白符
    for _ in range(cnt):
        if rd(0,1) == 0:
            s += " "
        else:
            s += "\t"
    return s

def getSymbol():
    return "+" if rd(0,1) == 1 else "-"

def getNum(positive):
    result = ""
    integer = intPool[rd(0, len(intPool)-1)]
    # 可能的前导零个数
    zero_count = rd(0,2)
    for _ in range(zero_count):
        result += "0"
    if not hasLeadZeros:
        result = ""
    result += str(integer)
    # 如果需要带符号
    if rd(0,1) == 1:
        if positive:
            result = "+" + result
        else:
            result = getSymbol() + result
    return result

def getExponent():
    exp_val = rd(0,2)  # 0, 1, 2
    s = "**" + getWhiteSpace()
    # 决定是否带显式正号
    if rd(0,1) == 1:
        s += "+"
    s += str(exp_val)
    return s

def getPower():
    result = "x"
    if rd(0,1) == 1:
        # 随机决定是否加幂
        result += getWhiteSpace() + getExponent()
    return result

def getTerm(genExpr):
    factorNum = rd(1, maxFactor)
    result = ""
    # 随机给这个项一个开头的符号
    if rd(0,1) == 1:
        result += getSymbol() + getWhiteSpace()

    for i in range(factorNum):
        factor_type = rd(0,2)
        if factor_type == 0:
            # 纯数字
            result += getNum(False)
        elif factor_type == 1:
            # x 或 x^?
            result += getPower()
        elif factor_type == 2 and genExpr:
            # 可能嵌套一个子表达式 ( )
            result += getExpr(isFactor=True)
        else:
            # 否则就给一个 0 作为 fallback
            result += "0"

        # 因子之间插入 *
        if i < factorNum - 1:
            result += getWhiteSpace() + "*" + getWhiteSpace()

    return result

def getExpr(isFactor):
    termNum = rd(1, maxTerm)
    result = getWhiteSpace()
    # 如果这个表达式是作为因子，则内部不再嵌套表达式(防止层级过深)
    genExpr = not isFactor

    for _ in range(termNum):
        # 每个 term 前面再来个 + 或 -
        result += getSymbol() + getWhiteSpace() + getTerm(genExpr) + getWhiteSpace()

    if isFactor:
        # 如果这是一个因子 ( ... )，可能再接幂
        result = "(" + result + ")"
        if rd(0,1) == 1:
            result += getWhiteSpace() + getExponent()

    return result

def remove_leading_zeros(expr):
    """
    用正则表达式去除所有数字的前导零（但保留单个零）。
    只处理非 'x' 变量的数字。
    """
    expr = re.sub(r'(?<![x0-9])0+(\d+)', r'\1', expr)  # 去除前导零
    expr = re.sub(r'\b0+(\d+)', r'\1', expr)  # 修正独立的数字
    return expr

def genData():
    expr = getExpr(False)

    expr_for_eval = remove_leading_zeros(expr)  # 先去除前导零
    
    x = sympy.Symbol('x')
    try:
        simplifed = sympy.expand(eval(expr_for_eval))  # 计算表达式
    except Exception as e:
        simplifed = f"表达式解析错误: {e}"  # 避免程序崩溃

    return expr, str(simplifed)

# 测试生成
expr, simplified_expr = genData()
print("随机生成的表达式:", expr.replace("**", "^"))
print("化简后的表达式:", simplified_expr.replace("**", "^"))