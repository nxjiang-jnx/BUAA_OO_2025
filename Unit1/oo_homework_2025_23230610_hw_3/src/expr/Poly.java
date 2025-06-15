package expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Poly {
    private final ArrayList<Mono> monos;

    public Poly() {
        this.monos = new ArrayList<>();
    }

    public BigInteger getMonoSize() {
        // 返回多项式中单项式的数量
        return BigInteger.valueOf(monos.size());
    }

    public ArrayList<Mono> getMonos() {
        return monos;
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
            // 合并同类项，found记录能否直接合并
            boolean found = false;
            for (Mono mono2 : result.monos) {
                int triCmp = compareTriMap(mono1.getTriMap(), mono2.getTriMap());
                if (mono1.getExp().equals(mono2.getExp())) {
                    // 指数相同，比较三角函数判断是否能合并
                    if (triCmp != -1) {
                        BigInteger newCoef = (triCmp == 0) ? mono1.getCoef().add(mono2.getCoef()) :
                            mono1.getCoef().subtract(mono2.getCoef());
                        mono2.setCoef(newCoef);
                        found = true;
                        break;
                    } else {
                        mergeSquaredTrigon(mono1, mono2);
                    }
                }
            }
            if (!found) {
                result.addMono(mono1);
            }
        }
        // 再尝试一次能否合并同类项
        for (int i = 0; i < result.monos.size(); i++) {
            for (int j = i + 1; j < result.monos.size(); j++) {
                int triCmp = compareTriMap(result.monos.get(i).getTriMap(),
                    result.monos.get(j).getTriMap());
                if (result.monos.get(i).getExp().equals(result.monos.get(j).getExp())) {
                    if (triCmp != -1) {
                        BigInteger newCoef = (triCmp == 0) ?
                            result.monos.get(i).getCoef().add(result.monos.get(j).getCoef()) :
                            result.monos.get(i).getCoef().subtract(result.monos.get(j).getCoef());
                        result.monos.get(i).setCoef(newCoef);
                        result.monos.remove(j);
                        j--;
                    }
                }
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
                // 设置正负，仅有两个 mono 异号时乘法结果为负
                newMono.setSign((mono1.getSign() != mono2.getSign()) ? 1 : 0);

                // 把 mono2 中所有三角函数加入结果中
                for (HashMap.Entry<Poly, BigInteger> entry : mono2.getTriMap().entrySet()) {
                    newMono.addTri(entry.getKey(), entry.getValue());
                }

                for (HashMap.Entry<Poly, BigInteger> e1 : mono1.getTriMap().entrySet()) {
                    // 遍历当前 this 的三角函数 HashMap
                    boolean found = false;
                    for (HashMap.Entry<Poly, BigInteger> e2 : newMono.getTriMap().entrySet()) {
                        // 遍历结果中已经存在的三角函数 HashMap，本质上就是 other 的三角函数 HashMap
                        if ((e1.getKey().equals(e2.getKey()) && e1.getValue().
                            multiply(e2.getValue()).compareTo(BigInteger.ZERO) > 0) ||
                            (e1.getKey().inverseEquals(e2.getKey()) && e1.getValue().
                            compareTo(BigInteger.ZERO) < 0 && e2.getValue().
                            compareTo(BigInteger.ZERO) < 0)) {
                            // 同名三角函数，且三角函数括号内表达式完全相同；或 cos(-Poly) = cos(Poly)
                            found = true;
                            e2.setValue(e1.getValue().add(e2.getValue()));
                            break;
                        } else if (e1.getKey().inverseEquals(e2.getKey()) &&
                            e1.getValue().compareTo(BigInteger.ZERO) > 0 &&
                            e2.getValue().compareTo(BigInteger.ZERO) > 0) {
                            // 同名三角函数，且三角函数括号内表达式完全相反；或 sin(-Poly) = -sin(Poly)
                            found = true;
                            e2.setValue(e1.getValue().add(e2.getValue()));
                            newMono.setSign((newMono.getSign() + e1.getValue().intValue() % 2) % 2);
                            break;
                        }
                    }
                    if (!found) {
                        newMono.addTri(e1.getKey(), e1.getValue());
                    }
                }
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
        // 对当前 Poly 进行深克隆
        Poly polyClone = this.clone();
        StringBuilder sb = new StringBuilder();

        // 正项提前
        for (Mono mono : polyClone.monos) {
            if (mono.getCoef().compareTo(BigInteger.ZERO) > 0) {
                sb.append("+");
                formatMono(mono, sb);
                polyClone.monos.remove(mono);
                break;
            }
        }

        for (Mono mono : polyClone.monos) {
            if (mono.getCoef().compareTo(BigInteger.ZERO) > 0) {
                sb.append("+");
            }
            if (mono.getCoef().compareTo(BigInteger.ZERO) != 0) {
                formatMono(mono, sb);
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

    // 以下是辅助函数
    public void formatMono(Mono mono, StringBuilder sb) {
        // 功能为生成单项式的格式化字符串表示
        sb.append(mono.getCoef());

        if (mono.getExp().compareTo(BigInteger.ZERO) > 0) {
            sb.append("*x");
            if (mono.getExp().compareTo(BigInteger.ONE) > 0) {
                // 指数大于1，需要幂表示
                sb.append("^").append(mono.getExp());
            }
        }
        formatTriMap(mono, sb);
    }

    public void formatTriMap(Mono mono, StringBuilder sb) {
        // 功能为生成单项式中的三角函数的格式化字符串表示
        for (HashMap.Entry<Poly, BigInteger> entry : mono.getTriMap().entrySet()) {
            if (entry.getValue().compareTo(BigInteger.ZERO) != 0) {
                // 指数不为0，正为 sin 负为 cos
                if (entry.getValue().compareTo(BigInteger.ZERO) > 0) {
                    sb.append("*sin(");
                } else {
                    sb.append("*cos(");
                }

                // 添加三角函数包含的因子
                String polyStr = entry.getKey().toString();
                if (judgeNotExprFactor(entry)) {
                    sb.append(polyStr);
                } else {
                    sb.append("(").append(polyStr).append(")");
                }
                sb.append(")");

                // 添加幂次
                if (entry.getValue().abs().compareTo(BigInteger.ONE) > 0) {
                    sb.append("^").append(entry.getValue().abs());
                }
            }
        }
    }

    public boolean judgeNotExprFactor(HashMap.Entry<Poly, BigInteger> entry) {
        BigInteger i = BigInteger.ZERO;
        BigInteger size = BigInteger.ZERO;
        for (Mono mono : entry.getKey().getMonos()) {
            if (mono.getCoef().compareTo(BigInteger.ZERO) != 0) {
                size = size.add(BigInteger.ONE);
                if (mono.notExprFactor()) {
                    i = i.add(BigInteger.ONE);
                }
            }
        }
        return i.equals(BigInteger.ONE) && size.equals(BigInteger.ONE);
    }

    @Override
    public Poly clone() {
        // 深克隆 Poly 对象
        Poly result = new Poly();
        for (Mono mono : this.monos) {
            result.addMono(mono.clone());
        }
        return result;
    }

    public int compareTriMap(HashMap<Poly, BigInteger> map1, HashMap<Poly, BigInteger> map2) {
        if (map1.size() != map2.size()) {
            return -1;
        }

        int sign = 0;
        // 深克隆，防止改变原数据
        HashMap<Poly, BigInteger> map1Clone = new HashMap<>();
        HashMap<Poly, BigInteger> map2Clone = new HashMap<>();
        for (HashMap.Entry<Poly, BigInteger> entry : map1.entrySet()) {
            map1Clone.put(entry.getKey().clone(), entry.getValue());
        }
        for (HashMap.Entry<Poly, BigInteger> entry : map2.entrySet()) {
            map2Clone.put(entry.getKey().clone(), entry.getValue());
        }

        // 遍历 map1Cone
        for (Iterator<HashMap.Entry<Poly, BigInteger>> it1 =
            map1Clone.entrySet().iterator(); it1.hasNext();) {
            HashMap.Entry<Poly, BigInteger> e1 = it1.next();
            // 获取当前三角函数的多项式和指数
            Poly poly1 = e1.getKey();
            BigInteger exp1 = e1.getValue();

            // 遍历 map2Clone
            for (Iterator<HashMap.Entry<Poly, BigInteger>> it2 =
                map2Clone.entrySet().iterator(); it2.hasNext();) {
                HashMap.Entry<Poly, BigInteger> e2 = it2.next();
                Poly poly2 = e2.getKey();
                BigInteger exp2 = e2.getValue();

                int result = checkTriMatch(poly1, exp1, poly2, exp2);
                if (result != -1) {
                    if (result == 1) {
                        sign = (sign + 1) % 2;
                    }
                    it1.remove();
                    it2.remove();
                    break;
                }
            }
        }
        if (map1Clone.size() == 0 && map2Clone.size() == 0) {
            return sign;
        } else {
            return -1;
        }
    }

    public boolean equals(Poly other) {
        // 删项法判断相等

        // 深克隆
        ArrayList<Mono> monos1Clone = new ArrayList<>();
        ArrayList<Mono> monos2Clone = new ArrayList<>();
        for (Mono mono : this.getMonos()) {
            if (mono.getCoef().compareTo(BigInteger.ZERO) != 0) {
                monos1Clone.add(mono.clone());
            }
        }
        for (Mono mono : other.getMonos()) {
            if (mono.getCoef().compareTo(BigInteger.ZERO) != 0) {
                monos2Clone.add(mono.clone());
            }
        }

        if (monos1Clone.size() != monos2Clone.size()) {
            return false;
        }

        for (Iterator<Mono> it1 = monos1Clone.iterator(); it1.hasNext();) {
            Mono mono1 = it1.next();
            for (Iterator<Mono> it2 = monos2Clone.iterator(); it2.hasNext();) {
                Mono mono2 = it2.next();
                if (mono1.equals(mono2)) {
                    it1.remove();
                    it2.remove();
                    break;
                }
            }
        }
        return monos1Clone.size() == 0 && monos2Clone.size() == 0;
    }

    public boolean inverseEquals(Poly other) {
        // 为了防止直接调用 toggleSign 出现浅克隆问题，直接定义判断取反相等的方法
        Poly poly1 = this.clone();
        Poly poly2 = other.clone();
        poly1.toggleSign();
        return poly1.equals(poly2);
    }

    private int checkTriMatch(Poly poly1, BigInteger exp1, Poly poly2, BigInteger exp2) {
        // 判断两个三角函数是否相等
        // -1：不相等；0：完全相等；1：相反数
        if (!exp1.equals(exp2)) {
            return -1;
        }

        // 下面都是指数相同且同类（均为sin或cos）
        // cos
        if (exp1.compareTo(BigInteger.ZERO) < 0 &&
            (poly1.equals(poly2) || poly1.inverseEquals(poly2))) {
            // cos(...)
            return 0;
        }

        // sin 的偶次幂
        if (exp1.compareTo(BigInteger.ZERO) > 0 && exp1.intValue() % 2 == 0 &&
            (poly1.equals(poly2) || poly1.inverseEquals(poly2))) {
            return 0;
        }

        // sin 的奇次幂
        if (exp1.compareTo(BigInteger.ZERO) > 0 && exp1.intValue() % 2 == 1) {
            if (poly1.equals(poly2)) {
                return 0;
            }
            else if (poly1.inverseEquals(poly2)) {
                return 1;
            }
        }
        return -1;
    }

    public void mergeSquaredTrigon(Mono mono1, Mono mono2) {
        // 不克隆，直接原数据修改
        // sin^2(x) + cos^2(x) = 1
        for (HashMap.Entry<Poly, BigInteger> e1 : mono1.getTriMap().entrySet()) {
            for (HashMap.Entry<Poly, BigInteger> e2 : mono2.getTriMap().entrySet()) {
                Poly poly1 = e1.getKey();
                Poly poly2 = e2.getKey();
                BigInteger exp1 = e1.getValue();
                BigInteger exp2 = e2.getValue();

                if (((exp1.equals(BigInteger.valueOf(2)) && exp2.equals(BigInteger.valueOf(-2))) ||
                    (exp1.equals(BigInteger.valueOf(-2)) && exp2.equals(BigInteger.valueOf(2)))) &&
                    (poly1.equals(poly2) || poly1.inverseEquals(poly2))) {
                    // 获取除此以外的其他项
                    HashMap<Poly, BigInteger> triMap1Clone =
                        cloneAndRemove(mono1.getTriMap(), e1);
                    HashMap<Poly, BigInteger> triMap2Clone =
                        cloneAndRemove(mono2.getTriMap(), e2);

                    int triCmp = compareTriMap(triMap1Clone, triMap2Clone);
                    if (triCmp == 0 && mono1.getSign() == mono2.getSign() ||
                        triCmp == 1 && mono1.getSign() != mono2.getSign()) {
                        // 根据系数大小合并和做差
                        if (mono1.getCoef().abs().compareTo(mono2.getCoef().abs()) >= 0) {
                            // mono1 系数绝对值更大，应减去 mono2 系数，然后把 mono2 的当前三角函数因子删除
                            mono1.setCoef(mono1.getCoef().subtract(mono2.getCoef()));
                            mono2.getTriMap().remove(poly2);
                        } else {
                            mono2.setCoef(mono2.getCoef().subtract(mono1.getCoef()));
                            mono1.getTriMap().remove(poly1);
                        }
                        return;
                    }
                }
            }
        }
    }

    private HashMap<Poly, BigInteger> cloneAndRemove(HashMap<Poly, BigInteger> source,
        HashMap.Entry<Poly, BigInteger> entryToRemove) {
        HashMap<Poly, BigInteger> result = new HashMap<>();
        for (HashMap.Entry<Poly, BigInteger> e : source.entrySet()) {
            if (!e.equals(entryToRemove)) {
                result.put(e.getKey().clone(), e.getValue());
            }
        }
        return result;
    }
}
