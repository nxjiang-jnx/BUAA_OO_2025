package expr;

import java.math.BigInteger;

public class TriFun implements Factor {
    private BigInteger exp;
    private Factor factor;
    private String type;

    public TriFun() {
        this.exp = BigInteger.ONE;
        this.factor = null;
        this.type = "sin";
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExp(BigInteger exp) {
        this.exp = exp;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    @Override
    public Poly toPoly() {
        // 返回一个多项式，其中只有 mono 一个三角函数单项式
        Mono mono = new Mono(BigInteger.ONE, BigInteger.ZERO);

        // 三角函数括号内的因子转化为多项式。注意区分！
        Poly factorPoly = factor.toPoly();

        if (type.equals("sin") && factorPoly.toString().equals("0")
            && exp.compareTo(BigInteger.ZERO) != 0) {
            // sin(0)
            mono.setCoef(BigInteger.ZERO);
        } else if ((type.equals("cos") && factorPoly.toString().equals("0"))
            || exp.equals(BigInteger.ZERO)) {
            // cos(0) 或 三角函数的零次幂
            mono.setCoef(BigInteger.ONE);
        } else if (type.equals("cos") && factorPoly.getMonoSize().equals(BigInteger.ONE)) {
            // cos((-x)) = cos(x)，factorPoly中唯一的单项式设为正
            factorPoly.getMonos().get(0).setSign(0);
            mono.addTri(factorPoly, exp.negate());
        } else if (type.equals("sin") && factorPoly.getMonoSize().equals(BigInteger.ONE) &&
            factorPoly.getMonos().get(0).getSign() == 1) {
            // sin((-x)) = -sin(x)，factorPoly中唯一的单项式仍设为正，但mono单项式符号取决于指数
            factorPoly.getMonos().get(0).setSign(0);
            mono.addTri(factorPoly, exp);
            mono.setSign(exp.intValue() % 2);
        } else if (type.equals("sin")) {
            // sin(表达式因子，含多个单项式)
            mono.addTri(factorPoly, exp);
        } else {
            // cos(表达式因子，含多个单项式)
            mono.addTri(factorPoly, exp.negate());
        }

        Poly poly = new Poly();
        poly.addMono(mono);
        return poly;
    }
}
