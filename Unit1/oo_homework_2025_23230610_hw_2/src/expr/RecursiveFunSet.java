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
        funExpr[idx] = expr;
    }
}
