import java.math.BigInteger;

public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = removeConsecutiveSigns(input);
        this.next();
    }

    private String removeConsecutiveSigns(String input) {
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

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        // 删去前导0
        while (pos < input.length() && input.charAt(pos) == '0') {
            ++pos;
        }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }

        if (sb.length() == 0) {
            // 该数字为一个或多个0，需要保留，以防为指数
            return "0";
        } else {
            return sb.toString();
        }
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }

        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else if (c == '*' || c == '+' || c == '-' || c == '(' ||
            c == ')' || c == '^' || c == 'x') {
            pos += 1;
            curToken = String.valueOf(c);
        }
    }

    public String peek() {
        return this.curToken;
    }
}