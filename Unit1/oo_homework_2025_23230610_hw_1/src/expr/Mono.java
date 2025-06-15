package expr;

import java.math.BigInteger;

public class Mono implements Factor {
    // coef*x^exp
    private BigInteger coef;
    private BigInteger exp;

    public Mono(BigInteger coef, BigInteger exp) {
        this.coef = coef;
        this.exp = exp;
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(this);
        return poly;
    }

    public void setSign(int sign) {
        // 0为正，1为负，若为1，则 coef取反
        if (sign == 1) {
            coef = coef.negate();
        }
    }

    public void setCoef(BigInteger coef) {
        this.coef = coef;
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
}
