package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Mono implements Factor {
    // coef*x^exp*Tri^BigInteger求和
    private BigInteger coef;
    private BigInteger exp;
    private HashMap<Poly, BigInteger> triMap;   // 值正为sin，负为cos

    public Mono(BigInteger coef, BigInteger exp) {
        this.coef = coef;
        this.exp = exp;
        this.triMap = new HashMap<>();
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(this);
        return poly;
    }

    public void setSign(int sign) {
        // 0为正，1为负
        if (coef.compareTo(BigInteger.ZERO) < 0 && sign == 0) {
            coef = coef.negate();
        } else if (coef.compareTo(BigInteger.ZERO) > 0 && sign == 1) {
            coef = coef.negate();
        }
    }

    public int getSign() {
        return coef.compareTo(BigInteger.ZERO) < 0 ? 1 : 0;
    }

    public HashMap<Poly, BigInteger> getTriMap() {
        return triMap;
    }

    public void setCoef(BigInteger coef) {
        this.coef = coef;
    }

    public void setExp(BigInteger exp) {
        this.exp = exp;
    }

    public void addTri(Poly poly, BigInteger exp) {
        triMap.put(poly, exp);
    }

    public BigInteger getExp() {
        return exp;
    }

    public BigInteger getCoef() {
        return coef;
    }

    public void toggleSign() {
        coef = coef.negate();
    }

    @Override
    public Mono clone() {
        // 对 Mono 对象进行深克隆
        Mono mono = new Mono(BigInteger.ONE, BigInteger.ZERO);
        mono.coef = coef;
        mono.exp = exp;
        for (HashMap.Entry<Poly, BigInteger> entry : triMap.entrySet()) {
            mono.addTri(entry.getKey().clone(), entry.getValue());
        }
        return mono;
    }

    public boolean equals(Mono other) {
        // 对 Mono 对象进行深克隆
        Mono mono1 = this.clone();
        Mono mono2 = other.clone();

        Poly polyHelper = new Poly();

        if (mono1.exp.equals(mono2.exp)) {
            int compareTriResult = polyHelper.compareTriMap(mono1.triMap, mono2.triMap);

            if (compareTriResult == 0 && mono1.coef.equals(mono2.coef)) {
                // 两个 Mono 对象相等，三角函数部分完全相等且 coef 相等
                return true;
            } else if (compareTriResult == 1 && mono1.coef.equals(mono2.coef.negate())) {
                // 两个 Mono 对象相等，三角函数部分为相反数，且 coef 相反
                return true;
            }
        }
        return false;
    }

    public boolean notExprFactor() {
        // 判断不是表达式因子，为了辅助三角函数输出时要不要填一层括号
        if (this.coef.equals(BigInteger.ONE) && this.triMap.size() == 0) {
            // x^exp
            return true;
        } else if (this.exp.equals(BigInteger.ZERO) && this.triMap.size() == 0) {
            // coef
            return true;
        } else if (this.coef.equals(BigInteger.ONE) &&
            this.exp.equals(BigInteger.ZERO) && this.triMap.size() == 1) {
            // 单个 sin/cos
            return true;
        }
        return false;
    }
}
