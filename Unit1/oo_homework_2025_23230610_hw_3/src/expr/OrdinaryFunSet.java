package expr;

public class OrdinaryFunSet {
    // 定义一个自定义普通函数模板
    private String name;
    private String var1;
    private String var2;
    private String funExpr;

    public OrdinaryFunSet() {
    }

    public String getFunExpr() {
        return funExpr;
    }

    public void setFunExpr(String funExpr) {
        this.funExpr = funExpr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
