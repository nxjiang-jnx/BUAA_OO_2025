package expr;

import java.math.BigInteger;
import java.util.ArrayList;

public class Poly {
    private final ArrayList<Mono> monos;

    public Poly() {
        this.monos = new ArrayList<>();
    }

    public void addMono(Mono mono) {
        this.monos.add(mono);
    }

    public Poly addPoly(Poly other) {
        Poly result = new Poly();

        // 将当前所有单项式加入到结果中
        for (Mono mono : this.monos) {
            result.addMono(mono);
        }
        for (Mono mono1 : other.monos) {
            // 合并同类项
            boolean found = false;
            for (Mono mono2 : result.monos) {
                if (mono1.getExp().equals(mono2.getExp())) {
                    BigInteger newCoef = mono1.getCoef().add(mono2.getCoef());
                    mono2.setCoef(newCoef);
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.addMono(mono1);
            }
        }
        return result;
    }

    public Poly mulPoly(Poly other) {
        Poly result = new Poly();
        // 如果当前多项式为空，则直接返回other多项式，否则在Term类中toPoly方法的第一步会有问题
        if (this.monos.isEmpty()) {
            return other;
        }
        for (Mono mono1 : this.monos) {
            for (Mono mono2 : other.monos) {
                BigInteger newCoef = mono1.getCoef().multiply(mono2.getCoef());
                BigInteger newExp = mono1.getExp().add(mono2.getExp());

                Mono newMono = new Mono(newCoef, newExp);
                result = result.addPoly(newMono.toPoly());
            }
        }
        return result;
    }

    public Poly powPoly(BigInteger index) {
        // index 为指数，指数为0，直接返回1
        if (index.equals(BigInteger.ZERO)) {
            Poly result = new Poly();
            result.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
            return result;
        }

        Poly result = new Poly();
        result.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        for (BigInteger i = BigInteger.ONE; i.compareTo(index) <= 0; i = i.add(BigInteger.ONE)) {
            result = result.mulPoly(this);
        }
        return result;
    }

    public void toggleSign() {
        for (Mono mono : this.monos) {
            mono.toggleSign();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Mono mono : this.monos) {
            if (mono.getCoef().compareTo(BigInteger.ZERO) != 0) {
                if (!first) {
                    // 不是第一项，如果mono系数为正，须添加加号；系数为负则自带负号，不需添加
                    if (mono.getCoef().compareTo(BigInteger.ZERO) > 0) {
                        sb.append("+");
                    }
                }
                first = false;
                sb = formatMono(mono, sb);
            }
        }

        String result = sb.toString().replaceAll("\\+1\\*", "+").replaceAll("-1\\*", "-");
        if (result.length() == 0) {
            return "0";
        } else if (result.charAt(0) == '+') {
            return result.substring(1);
        } else {
            return result;
        }
    }

    public StringBuilder formatMono(Mono mono, StringBuilder sb) {
        // 功能为生成单项式的格式化字符串表示
        sb.append(mono.getCoef());

        if (mono.getExp().compareTo(BigInteger.ZERO) > 0) {
            sb.append("*x");
            if (mono.getExp().compareTo(BigInteger.ONE) > 0) {
                // 指数大于1，需要幂表示
                sb.append("^").append(mono.getExp());
            }
        }
        return sb;
    }
}
