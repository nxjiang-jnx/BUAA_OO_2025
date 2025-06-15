package expr;

import java.util.ArrayList;
import java.util.Iterator;

public class Term {
    private final ArrayList<Factor> factors;
    private final int sign; // 0为正，1为负

    public Term(int sign) {
        this.factors = new ArrayList<>();
        this.sign = sign;
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    @Override
    public String toString() {
        Iterator<Factor> iter = factors.iterator();
        StringBuilder sb = new StringBuilder();

        sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append("*").append(iter.next().toString());
        }
        return sb.toString();
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        for (Factor factor : factors) {
            // 项的乘法
            poly = poly.mulPoly(factor.toPoly());
        }
        if (sign == 1) {
            poly.toggleSign();
        }
        return poly;
    }
}
