import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab4p {
    private static InputReader in;
    private static PrintWriter out;
    public static int n, m;
    public static long p[][];

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        n = in.nextInteger();
        m = in.nextInteger();
        p = new long[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                p[i][j] = in.nextInteger();
            }
        }
        System.out.println("------------");
        long ans = 0;
        out.println(solve(ans));
        out.close();
    }

    public static long solve(long maxSatisfaction) {
        long[][] newp = new long[n][m];
        for (int j = 0; j < m; j++) {
            newp[0][j] = p[0][j];
        }
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < m; j++) {
                long maxPrevious = newp[i - 1][j];
                if (j > 0) {
                    maxPrevious = Math.max(maxPrevious, newp[i - 1][j - 1]);
                }
                if (j < m - 1) {
                    maxPrevious = Math.max(maxPrevious, newp[i - 1][j + 1]);
                }
                newp[i][j] = maxPrevious + p[i][j];

                for (long[] ans : newp) {
                    for (long print : ans) {
                        System.out.print(print + " ");
                    }
                    System.out.println();
                }
            }
        }
        for (int j = 0; j < m; j++) {
            maxSatisfaction = Math.max(maxSatisfaction, newp[n - 1][j]);
        }
        return maxSatisfaction;
    }

    // taken from https://codeforces.com/submissions/Petr
    // together with PrintWriter, these input-output (IO) is much faster than the
    // usual Scanner(System.in) and System.out
    // please use these classes to avoid your fast algorithm gets Time Limit
    // Exceeded caused by slow input-output (IO)
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInteger() {
            return Integer.parseInt(next());
        }
    }
}