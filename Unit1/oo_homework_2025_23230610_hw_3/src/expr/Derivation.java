package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Derivation implements Factor {
    private Expr expr;

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public Derivation() {
        this.expr = null;
    }

    public Poly derivation(Poly poly) {
        Poly result = new Poly();
        for (Mono mono : poly.getMonos()) {
            // 对 coef*x^exp 求导
            result = result.addPoly(deriveMono(mono));

            // 对三角函数求导
            for (HashMap.Entry<Poly, BigInteger> entry : mono.getTriMap().entrySet()) {
                if (entry.getValue().compareTo(BigInteger.ZERO) != 0) {
                    result = result.addPoly(deriveTri(mono, entry));
                }
            }
        }
        return result;
    }

    public Poly deriveMono(Mono mono) {
        // 返回一个只有一个单项式的 Poly，多项式部分求导，三角函数不动
        Mono resultMono = new Mono(BigInteger.ZERO, BigInteger.ZERO);

        resultMono.setExp(mono.getExp().subtract(BigInteger.ONE));
        resultMono.setCoef(mono.getCoef().multiply(mono.getExp()));

        for (HashMap.Entry<Poly, BigInteger> entry : mono.getTriMap().entrySet()) {
            resultMono.addTri(entry.getKey().clone(), entry.getValue());
        }

        Poly result = new Poly();
        result.addMono(resultMono);
        return result;
    }

    public Poly deriveTri(Mono mono, HashMap.Entry<Poly, BigInteger> entry) {
        // 返回一个只有一个单项式的 Poly，三角函数求导，多项式部分不动
        Mono resultMono = new Mono(BigInteger.ZERO, BigInteger.ZERO);

        resultMono.setExp(mono.getExp());
        resultMono.setCoef(mono.getCoef().multiply(entry.getValue().abs()));

        // 链式法则
        if (entry.getValue().compareTo(BigInteger.ZERO) < 0) {
            // cos
            resultMono.toggleSign();
            resultMono.addTri(entry.getKey().clone(), entry.getValue().add(BigInteger.ONE));
            resultMono.addTri(entry.getKey().clone(), BigInteger.ONE);
        } else {
            // sin
            resultMono.addTri(entry.getKey().clone(), entry.getValue().subtract(BigInteger.ONE));
            resultMono.addTri(entry.getKey().clone(), BigInteger.valueOf(-1));
        }

        for (HashMap.Entry<Poly, BigInteger> otherEntry : mono.getTriMap().entrySet()) {
            if (!otherEntry.equals(entry)) {
                resultMono.addTri(otherEntry.getKey().clone(), otherEntry.getValue());
            }
        }

        Poly polyDerive = this.derivation(entry.getKey());
        Poly result = new Poly();
        result.addMono(resultMono);
        return result.mulPoly(polyDerive);
    }

    @Override
    public Poly toPoly() {
        return derivation(expr.toPoly());
    }
}
