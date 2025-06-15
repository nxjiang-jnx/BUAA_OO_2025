import expr.Expr;
import expr.OrdinaryFunSet;
import expr.RecursiveFunSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    private static ArrayList<OrdinaryFunSet> ordinaryFunList = new ArrayList<>();
    private static RecursiveFunSet recursiveFunSet = new RecursiveFunSet();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取自定义普通函数
        int n = scanner.nextInt();
        scanner.nextLine();
        for (int i = 0; i < n; i++) {
            String def = removeConsecutiveSigns(scanner.nextLine().replaceAll("\\s+", ""));
            ordinaryFunList.add(parseOrdinaryDefs(def));
        }

        // 读取自定义递推函数
        int m = scanner.nextInt();
        scanner.nextLine();
        for (int i = 0; i < m; i++) {
            String def1 = removeConsecutiveSigns(scanner.nextLine().replaceAll("\\s+", ""));
            String def2 = removeConsecutiveSigns(scanner.nextLine().replaceAll("\\s+", ""));
            String def3 = removeConsecutiveSigns(scanner.nextLine().replaceAll("\\s+", ""));
            recursiveFunSet = parseRecursiveDefs(def1, def2, def3);
        }

        // 读取输入表达式
        String input = scanner.nextLine().replaceAll("\\s+", "");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        System.out.println(expr.toPoly().toString());
    }

    private static RecursiveFunSet parseRecursiveDefs(String def1, String def2, String def3) {
        RecursiveFunSet recursiveFunSet = new RecursiveFunSet();
        String[] defs = {def1, def2, def3};

        for (String def : defs) {
            // 解析定义的左半边和右半边
            int equalIndex = def.indexOf('=');
            String leftPart = def.substring(0, equalIndex);
            String rightPart = def.substring(equalIndex + 1);

            // 提取大括号内的数字或n
            int leftBraceIdx = leftPart.indexOf('{');
            int rightBraceIdx = leftPart.indexOf('}');
            String idxStr = leftPart.substring(leftBraceIdx + 1, rightBraceIdx);
            int index = idxStr.equals("n") ? 2 : Integer.parseInt(idxStr);

            // 提取形参
            int leftParenIdx = leftPart.indexOf('(', rightBraceIdx);
            int rightParenIdx = findMatchingBracket(leftPart, leftParenIdx);
            String paramsStr = leftPart.substring(leftParenIdx + 1, rightParenIdx);
            String[] params = paramsStr.split(",");

            recursiveFunSet.setVar1(params[0]);
            recursiveFunSet.setVar2(params.length == 2 ? params[1] : null);

            recursiveFunSet.setFunExpr(index, rightPart);
        }

        return recursiveFunSet;
    }

    private static OrdinaryFunSet parseOrdinaryDefs(String def) {
        OrdinaryFunSet ordinaryFunSet = new OrdinaryFunSet();

        int equalIndex = def.indexOf('=');
        String leftPart = def.substring(0, equalIndex);
        String rightPart = def.substring(equalIndex + 1);

        int leftParenIdx = leftPart.indexOf('(');
        int rightParenIdx = findMatchingBracket(leftPart, leftParenIdx);
        String paramsStr = leftPart.substring(leftParenIdx + 1, rightParenIdx);
        String[] params = paramsStr.split(",");

        ordinaryFunSet.setName(leftPart.substring(0, leftParenIdx));
        ordinaryFunSet.setVar1(params[0]);
        ordinaryFunSet.setVar2(params.length == 2 ? params[1] : null);

        ordinaryFunSet.setFunExpr(rightPart);
        return ordinaryFunSet;
    }

    public static int findMatchingBracket(String expr, int leftIdx) {
        // 找到匹配的右括号
        int cnt = 1;
        int i = leftIdx + 1;
        while (i < expr.length() && cnt != 0) {
            if (expr.charAt(i) == '(') {
                cnt++;
            }
            else if (expr.charAt(i) == ')') {
                cnt--;
            }
            i++;
        }
        return i - 1;
    }

    public static RecursiveFunSet getRecursiveFunSet() {
        return recursiveFunSet;
    }

    public static ArrayList<OrdinaryFunSet> getOrdinaryFunList() {
        return ordinaryFunList;
    }

    public static String removeConsecutiveSigns(String input) {
        BigInteger len = BigInteger.valueOf(input.length());
        StringBuilder sb = new StringBuilder();
        int flag = 0;

        BigInteger i = BigInteger.ZERO;
        while (i.compareTo(len) < 0) {
            char c = input.charAt(i.intValue());
            if (c == '+' || c == '-') {
                while (i.compareTo(len) < 0 && (input.charAt(i.intValue()) == '+'
                        || input.charAt(i.intValue()) == '-')) {
                    if (input.charAt(i.intValue()) == '-') {
                        flag++;
                    }
                    i = i.add(BigInteger.ONE);
                }
                sb.append(flag % 2 == 0 ? '+' : '-');
                flag = 0;
            } else {
                sb.append(c);
                i = i.add(BigInteger.ONE);
            }
        }
        return sb.toString();
    }
}