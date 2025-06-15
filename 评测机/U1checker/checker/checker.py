import sympy as sp
import random

def load_expr(filename):
    with open(filename, 'r', encoding='utf-8') as f:
        lines = [line.strip() for line in f if line.strip()]
        content = ' '.join(lines)
        # 替换 ^ 为 **
        content = content.replace('^', '**')
    return content

def parse_expr(expr_str):
    x = sp.symbols('x')
    try:
        return sp.sympify(expr_str, locals={
            'x': x,
            'sin': sp.sin,
            'cos': sp.cos,
            'exp': sp.exp,
            'pi': sp.pi,
            'E': sp.E
        })
    except Exception as e:
        print(f"解析表达式出错: {expr_str}\n错误信息: {e}")
        raise
    
def compare_expr(expr1, expr2, trials=10):
    x = sp.symbols('x')
    diff = expr1 - expr2
    for _ in range(trials):
        # 随机生成测试数据
        val = random.uniform(-100, 100)
        subs_val = diff.subs(x, val).evalf()
        if abs(subs_val) > 1e-6:
            return False, val, subs_val
    return True, None, None

def main():
    expr_str1 = load_expr('expr1.txt')
    expr_str2 = load_expr('expr2.txt')

    if not expr_str1 or not expr_str2:
        print("表达式文件不能为空，请检查文件内容。")
        return

    expr1 = parse_expr(expr_str1)
    expr2 = parse_expr(expr_str2)

    equal, val, diff = compare_expr(expr1, expr2)

    if equal:
        print("表达式在随机测试点下相等。")
    else:
        print(f"表达式不相等。\n测试点x = {val}\n表达式差值为：{diff}")

if __name__ == '__main__':
    main()
