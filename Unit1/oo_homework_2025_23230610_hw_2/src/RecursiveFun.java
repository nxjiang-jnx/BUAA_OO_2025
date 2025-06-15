import expr.Factor;
import expr.Poly;
import expr.RecursiveFunSet;

import java.util.ArrayList;

public class RecursiveFun implements Factor {
    private Factor factor1;
    private Factor factor2;
    private int digit;  // f{digit}()

    public RecursiveFun() {
        this.factor1 = null;
        this.factor2 = null;
    }

    public void setFactor1(Factor factor1) {
        this.factor1 = factor1;
    }

    public void setFactor2(Factor factor2) {
        this.factor2 = factor2;
    }

    public void setDigit(String digitStr) {
        this.digit = Integer.parseInt(digitStr);
    }

    private String[] actualParamsSplit(String paramsStr) {
        // 分割参数，支持嵌套括号
        ArrayList<String> res = new ArrayList<>();
        int bracket = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < paramsStr.length(); i++) {
            char c = paramsStr.charAt(i);
            if (c == ',' && bracket == 0) {
                res.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                if (c == '(') {
                    bracket++;
                }
                else if (c == ')') {
                    bracket--;
                }
                cur.append(c);
            }
        }
        res.add(cur.toString().trim());
        return res.toArray(new String[0]);
    }

    private String expandFully(int n, String[] actualParams, String[] formalParams,
        RecursiveFunSet recursiveFunSet) {
        String expr;
        if (n == 0 || n == 1) {
            expr = recursiveFunSet.getFunExpr(n);
        } else {
            expr = recursiveFunSet.getFunExpr(2);
        }

        // 先处理当前层的实参：可能有递推函数，需展开
        for (int i = 0; i < actualParams.length; i++) {
            if (actualParams[i] != null && actualParams[i].contains("f{")) {
                // 实参本身含有f{}表达式，进一步解析展开
                Lexer lexer = new Lexer(actualParams[i]);
                Parser parser = new Parser(lexer);
                Factor factor = parser.parseFactor();  // Factor可能是RecursiveFun
                actualParams[i] = factor.toPoly().toString();
            }
        }

        // 将当前形参换为当前实参
        if (formalParams.length == 2 && formalParams[0].equals("y")) {
            expr = expr.replaceAll("\\b" + formalParams[1] + "\\b", "(" + actualParams[1] + ")");
            expr = expr.replaceAll("\\b" + formalParams[0] + "\\b", "(" + actualParams[0] + ")");
        } else if (formalParams.length == 2 && formalParams[1].equals("y")) {
            expr = expr.replaceAll("\\b" + formalParams[0] + "\\b", "(" + actualParams[0] + ")");
            expr = expr.replaceAll("\\b" + formalParams[1] + "\\b", "(" + actualParams[1] + ")");
        } else {
            expr = expr.replaceAll("\\b" + formalParams[0] + "\\b", "(" + actualParams[0] + ")");
        }

        // 用栈替换
        int idx = 0;
        while ((idx = expr.indexOf("f{n-")) != -1) {
            int offsetEnd = expr.indexOf('}', idx);
            int offset = Integer.parseInt(expr.substring(idx + 4, offsetEnd));
            int startParamIdx = expr.indexOf('(', offsetEnd);
            int endParamIdx = MainClass.findMatchingBracket(expr, startParamIdx);

            String innerActualParamsStr = expr.substring(startParamIdx + 1, endParamIdx);
            String[] innerActualParams = actualParamsSplit(innerActualParamsStr);

            String recursiveExpanded = expandFully(n - offset, innerActualParams,
                formalParams, recursiveFunSet);

            expr = expr.substring(0, idx) + "(" + recursiveExpanded + ")" +
                    expr.substring(endParamIdx + 1);
        }
        return expr;
    }

    private String[] initialFormalParams(RecursiveFunSet recursiveFunSet) {
        if (recursiveFunSet.getVar2() == null) {
            return new String[]{recursiveFunSet.getVar1()};
        }
        return new String[]{recursiveFunSet.getVar1(), recursiveFunSet.getVar2()};
    }

    @Override
    public Poly toPoly() {
        RecursiveFunSet recursiveFunSet = MainClass.getRecursiveFunSet();

        // 先传入初始参数，然后递归展开
        String[] actualParams = {factor1.toPoly().toString(),
                                 factor2 != null ? factor2.toPoly().toString() : null};

        String expr = expandFully(digit, actualParams, initialFormalParams(recursiveFunSet),
            recursiveFunSet);
        Lexer lexer = new Lexer(expr);
        Parser parser = new Parser(lexer);
        return parser.parseExpr().toPoly();
    }
}