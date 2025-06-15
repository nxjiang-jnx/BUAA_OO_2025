import expr.Factor;
import expr.Poly;
import expr.OrdinaryFunSet;

public class OrdinaryFun extends SelfDefFun implements Factor {
    private String name;

    public OrdinaryFun() {
        super();
        name = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String[] initialFormalParams(OrdinaryFunSet ordinaryFunSet) {
        if (ordinaryFunSet.getVar2() == null) {
            return new String[]{ordinaryFunSet.getVar1()};
        }
        return new String[]{ordinaryFunSet.getVar1(), ordinaryFunSet.getVar2()};
    }

    private String expandFully(String[] actualParams, String[] formalParams,
        OrdinaryFunSet ordinaryFunSet) {
        String expr = ordinaryFunSet.getFunExpr();

        // 将当前形参换为当前实参
        if (formalParams.length == 2 && formalParams[0].equals("y")) {
            expr = expr.replaceAll("\\b" + formalParams[1] + "\\b", "(" + actualParams[1] + ")");
            expr = expr.replaceAll("\\b" + formalParams[0] + "\\b", "(" + actualParams[0] + ")");
        } else if (formalParams.length == 2 && formalParams[1].equals("y")) {
            expr = expr.replaceAll("\\b" + formalParams[0] + "\\b", "(" + actualParams[0] + ")");
            expr = expr.replaceAll("\\b" + formalParams[1] + "\\b", "(" + actualParams[1] + ")");
        } else {
            expr = expr.replaceAll("\\b" + formalParams[0] + "\\b", "(" + actualParams[0] + ")");
        }

        return expr;
    }

    @Override
    public Poly toPoly() {
        OrdinaryFunSet ordinaryFun = null;
        for (OrdinaryFunSet fun : MainClass.getOrdinaryFunList()) {
            if (fun.getName().equals(name)) {
                ordinaryFun = fun;
                break;
            }
        }

        String[] actualParams = {super.getFactor1().toPoly().toString(),
                super.getFactor2() != null ? super.getFactor2().toPoly().toString() : null};

        String expr = expandFully(actualParams, initialFormalParams(ordinaryFun), ordinaryFun);

        Lexer lexer = new Lexer(expr);
        Parser parser = new Parser(lexer);
        return parser.parseExpr().toPoly();
    }
}
