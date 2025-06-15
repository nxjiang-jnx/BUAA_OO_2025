import expr.Expr;
import expr.Factor;
import expr.Mono;
import expr.Term;
import expr.TriFun;

import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();

        // 处理首项
        if (lexer.peek().equals("+")) {
            lexer.next();
            expr.addTerm(parseTerm(0));
        } else if (lexer.peek().equals("-")) {
            lexer.next();
            expr.addTerm(parseTerm(1));
        } else {
            expr.addTerm(parseTerm(0));  // 首项无显式符号
        }

        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            // 转化为项的正负属性，表达式永远为加法
            if (lexer.peek().equals("+")) {
                lexer.next();
                expr.addTerm(parseTerm(0));
            } else {
                lexer.next();
                expr.addTerm(parseTerm(1));
            }
        }
        return expr;
    }

    public Term parseTerm(int sign) {
        Term term = new Term(sign);
        term.addFactor(parseFactor());

        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            // 表达式因子，含指数
            return parseExprFactor();
        } else if (lexer.peek().equals("x")) {
            // 变量因子或幂函数，无前导符号
            return parsePowFactor();
        } else if (lexer.peek().equals("sin") || lexer.peek().equals("cos")) {
            // 三角函数因子，含指数
            return parseTriFactor();
        } else if (lexer.peek().equals("f")) {
            // 自定义递推函数因子
            return parseRecursiveFunFactor();
        } else {
            // 常数因子，可能有先导符号
            return parseNumberFactor();
        }
    }

    // 以下是辅助方法
    public Expr parseExprFactor() {
        // 表达式因子，含指数
        lexer.next();
        Expr expr = parseExpr();
        lexer.next();   // 吃掉右括号

        // 表达式因子的指数
        if (lexer.peek().equals("^")) {
            lexer.next();
            if (lexer.peek().equals("+")) {
                lexer.next();
            }
            expr.setExponent(new BigInteger(lexer.peek()));
            lexer.next();
        }
        return expr;
    }

    public Mono parsePowFactor() {
        // 变量因子或幂函数，无前导符号，返回类型为单项式单元
        lexer.next();
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().equals("^")) {
            // 幂函数，和之前处理相同
            lexer.next();
            if (lexer.peek().equals("+")) {
                lexer.next();
            }
            exponent = new BigInteger(lexer.peek());
            lexer.next();
        }
        return new Mono(BigInteger.ONE, exponent);
    }

    public Mono parseNumberFactor() {
        // 常数因子，可能有先导符号，返回类型为单项式单元
        int sign = 0;
        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            lexer.next();
            sign = 1;
        }
        BigInteger num = new BigInteger(lexer.peek());
        lexer.next();

        Mono mono = new Mono(num, BigInteger.ZERO);
        mono.setSign(sign);
        return mono;
    }

    public TriFun parseTriFactor() {
        // 三角函数因子，含指数
        TriFun triFun = new TriFun();
        triFun.setType(lexer.peek());

        lexer.next();   // 吃掉'('
        lexer.next();
        triFun.setFactor(parseFactor());
        lexer.next();   // 吃掉')'

        if (lexer.peek().equals("^")) {
            lexer.next();
            if (lexer.peek().equals("+")) {
                lexer.next();
            }
            triFun.setExp(new BigInteger(lexer.peek()));
            lexer.next();
        }
        return triFun;
    }

    public RecursiveFun parseRecursiveFunFactor() {
        RecursiveFun recursiveFun = new RecursiveFun();
        lexer.next();   // 吃掉'{'
        lexer.next();
        recursiveFun.setDigit(lexer.peek());
        lexer.next();   // 吃掉'}'
        lexer.next();   // 吃掉'('
        lexer.next();

        recursiveFun.setFactor1(parseFactor());

        if (lexer.peek().equals(",")) {
            lexer.next();
            recursiveFun.setFactor2(parseFactor());
        }
        lexer.next();   // 吃掉')'
        return recursiveFun;
    }
}
