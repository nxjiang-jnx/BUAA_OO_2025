package expr;

public class RecursiveFunSet {
    // 记录一个自定义函数的模板
    private String var1;
    private String var2;
    private String[] funExpr = new String[3]; // f{0}, f{1}, f{n}

    public RecursiveFunSet() {
        var1 = null;
        var2 = null;
    }

    public String getVar1() {
        return var1;
    }

    public void setVar1(String var1) {
        this.var1 = var1;
    }

    public String getVar2() {
        return var2;
    }

    public void setVar2(String var2) {
        this.var2 = var2;
    }

    public String getFunExpr(int idx) {
        return funExpr[idx];
    }

    public void setFunExpr(int idx, String expr) {
        if (idx != 2) {
            funExpr[idx] = expr;
            return;
        }

        StringBuilder sb = new StringBuilder();
        int len = expr.length();
        int i = 0;

        while (i < len) {
            if (shouldSkipZeroTerm(expr, i)) {
                i = skipZeroTerm(expr, i);
            } else {
                sb.append(expr.charAt(i));
                i++;
                int bracket = 0;
                while (i < len) {
                    char ch = expr.charAt(i);
                    if ((ch == '+' || ch == '-') && bracket == 0) {
                        break;
                    }
                    if (ch == '(') {
                        bracket++;
                    } else if (ch == ')') {
                        bracket--;
                    }
                    sb.append(ch);
                    i++;
                }
            }
        }
        funExpr[idx] = sb.toString();
    }

    private boolean shouldSkipZeroTerm(String expr, int idx) {
        int len = expr.length();
        int i = idx;
        if (expr.charAt(i) == '+' || expr.charAt(i) == '-') {
            i++;
        }
        int coefStart = i;
        while (i < len && Character.isDigit(expr.charAt(i))) {
            i++;
        }

        boolean hasCoef = coefStart < i;
        boolean isZeroCoef = hasCoef && expr.substring(coefStart, i).equals("0");

        return isZeroCoef && i + 6 < len && expr.charAt(i) == '*' &&
                expr.startsWith("*f{n-", i) &&
                (expr.charAt(i + 5) == '1' || expr.charAt(i + 5) == '2') &&
                expr.charAt(i + 6) == '}';
    }

    private int skipZeroTerm(String expr, int idx) {
        int len = expr.length();
        int i = idx;
        if (expr.charAt(i) == '+' || expr.charAt(i) == '-') {
            i++;
        }
        while (i < len && Character.isDigit(expr.charAt(i))) {
            i++;
        }

        i += 7;
        if (i < len && expr.charAt(i) == '(') {
            i++;
            int bracket = 1;
            while (i < len && bracket > 0) {
                if (expr.charAt(i) == '(') {
                    bracket++;
                } else if (expr.charAt(i) == ')') {
                    bracket--;
                }
                i++;
            }
        }

        while (i < len && (expr.charAt(i) == '+' || expr.charAt(i) == '-')) {
            i++;
        }

        return i;
    }
}
