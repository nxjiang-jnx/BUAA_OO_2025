package expr;

import java.math.BigInteger;
import java.util.ArrayList;

public class Expr implements Factor {
    private final ArrayList<Term> terms;
    private BigInteger exponent;

    public Expr() {
        this.terms = new ArrayList<>();
        this.exponent = BigInteger.ONE;
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        for (Term term : terms) {
            poly = poly.addPoly(term.toPoly());
        }
        if (exponent.compareTo(BigInteger.ONE) != 0) {
            poly = poly.powPoly(exponent);
        }
        return poly;
    }
}
