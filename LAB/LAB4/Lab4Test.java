import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab4Test {
    private static InputReader in;
    private static PrintWriter out;
    public static int n, m;
    public static long[][] p; // Initialize the matrix to store satisfaction

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        n = in.nextInteger();
        m = in.nextInteger();
        
        // Initialize the satisfaction array
        p = new long[n][m];

        // Fill the satisfaction matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                p[i][j] = in.nextInteger();
            }
        }

        // Call the function and assign the result to ans
        long ans = maxSatisfaction(n, m, p);

        out.println(ans); // Output the result
        out.close();
    }

    public static long maxSatisfaction(int N, int M, long[][] satisfaction) {
        // Initialize the dp array
        long[][] dp = new long[N][M];

        // Initialize the first day's satisfaction values
        for (int j = 0; j < M; j++) {
            dp[0][j] = satisfaction[0][j];
        }

        // Fill the dp table
        for (int i = 1; i < N; i++) {
            for (int j = 0; j < M; j++) {
                // Start with the satisfaction of staying in the same city
                long maxPrevious = dp[i - 1][j];

                // Check if moving to the previous city is possible
                if (j > 0) {
                    maxPrevious = Math.max(maxPrevious, dp[i - 1][j - 1]);
                }

                // Check if moving to the next city is possible
                if (j < M - 1) {
                    maxPrevious = Math.max(maxPrevious, dp[i - 1][j + 1]);
                }

                // Update dp table for the current day and city
                dp[i][j] = maxPrevious + satisfaction[i][j];
            }
        }

        // Find the maximum satisfaction possible on the last day
        long maxSatisfaction = 0;
        for (int j = 0; j < M; j++) {
            maxSatisfaction = Math.max(maxSatisfaction, dp[N - 1][j]);
        }

        return maxSatisfaction;
    }

    // InputReader class for fast input
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
