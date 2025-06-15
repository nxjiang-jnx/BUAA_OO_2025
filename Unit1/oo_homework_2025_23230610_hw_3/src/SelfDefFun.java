import expr.Factor;
import java.util.ArrayList;

public class SelfDefFun {
    private Factor factor1;
    private Factor factor2;

    public SelfDefFun() {
        this.factor1 = null;
        this.factor2 = null;
    }

    public void setFactor1(Factor factor1) {
        this.factor1 = factor1;
    }

    public void setFactor2(Factor factor2) {
        this.factor2 = factor2;
    }

    public Factor getFactor1() {
        return factor1;
    }

    public Factor getFactor2() {
        return factor2;
    }

    public String[] actualParamsSplit(String paramsStr) {
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
}
