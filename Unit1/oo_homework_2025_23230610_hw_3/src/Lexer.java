public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = MainClass.removeConsecutiveSigns(input);
        this.next();
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
        } else if (c == 'f' || c == 'g' || c == 'h' || c == '{' ||
            c == '}' || c == ',' || c == 'd') {
            pos += 1;
            curToken = String.valueOf(c);
        } else if (c == 's' && input.charAt(pos + 1) == 'i' && input.charAt(pos + 2) == 'n') {
            pos += 3;
            curToken = "sin";
        } else if (c == 'c' && input.charAt(pos + 1) == 'o' && input.charAt(pos + 2) == 's') {
            pos += 3;
            curToken = "cos";
        }
    }

    public String peek() {
        return this.curToken;
    }
}