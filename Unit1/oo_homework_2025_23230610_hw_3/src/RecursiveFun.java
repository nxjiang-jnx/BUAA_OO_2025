import expr.Factor;
import expr.Poly;
import expr.RecursiveFunSet;

public class RecursiveFun extends SelfDefFun implements Factor {
    private int digit;  // f{digit}()

    public RecursiveFun() {
        super();
    }

    public void setDigit(String digitStr) {
        this.digit = Integer.parseInt(digitStr);
    }

    private String expandFully(int n, String[] actualParams, String[] formalParams,
        RecursiveFunSet recursiveFunSet) {
        String expr;
        if (n == 0 || n == 1) {
            expr = recursiveFunSet.getFunExpr(n);
        } else {
            expr = recursiveFunSet.getFunExpr(2);
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
            String[] innerActualParams = super.actualParamsSplit(innerActualParamsStr);

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
        String[] actualParams = {super.getFactor1().toPoly().toString(),
                 super.getFactor2() != null ? super.getFactor2().toPoly().toString() : null};

        String expr = expandFully(digit, actualParams, initialFormalParams(recursiveFunSet),
            recursiveFunSet);
        Lexer lexer = new Lexer(expr);
        Parser parser = new Parser(lexer);
        return parser.parseExpr().toPoly();
    }
}