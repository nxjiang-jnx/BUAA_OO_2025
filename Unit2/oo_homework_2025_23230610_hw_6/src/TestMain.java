import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {
    public static void main(String[] args) {
        System.setIn(new TimableInputStream(System.in));
        MainClass.main(args);
    }

    private static class TimableInputStream extends InputStream {
        private static final Pattern pattern = Pattern.compile("\\[(.*?)](.*)");
        private final Queue<Integer> cache = new ArrayDeque<>();
        private final long initTime;
        private final Scanner scanner;

        TimableInputStream(InputStream is) {
            scanner = new Scanner(is);
            scanner.hasNext(); // make sure at least one character in the InputStream,
            // and block this thread if necessary.
            initTime = System.currentTimeMillis() + 10; // some corrections can be added here.
        }

        @Override
        public int read() throws IOException {
            if (cache.isEmpty()) {
                if (!scanner.hasNextLine()) { return -1; }
                String line = scanner.nextLine();
                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) {
                    throw new IOException("Invalid input: " + line);
                }
                try {
                    long msgTime = (long) (Double.parseDouble(matcher.group(1)) * 1000 + 0.5);
                    String content = matcher.group(2);
                    long time = initTime + msgTime - System.currentTimeMillis();
                    if (time > 0) { Thread.sleep(time); }
                    content.chars().forEach(cache::add);
                    cache.add(10);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
            return Objects.requireNonNull(cache.poll());
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int c = read();
            if (c == -1) { return -1; }
            b[off] = (byte) c;

            int i = 1;
            for (; i < len; i++) {
                if (cache.isEmpty()) { break; }
                c = read();
                if (c == -1) { break; }
                b[off + i] = (byte) c;
            }
            return i;
        }
    }
}