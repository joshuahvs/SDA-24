import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab2p {
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        String kata = in.next();
        long result = solve(kata);
        out.println(result);
        out.close();
    }

    public static long solve(String kata) {
        int n = kata.length();
        long countSDA = 0;

        int[] prefixS = new int[n];
        int[] suffixA = new int[n];

        for (int i = 0; i < n; i++) {
            if (i > 0) {
                prefixS[i] = prefixS[i - 1];
            }
            if (kata.charAt(i) == 's') {
                prefixS[i]++;
            }
        }

        for (int i = n - 1; i >= 0; i--) {
            if (i < n - 1) {
                suffixA[i] = suffixA[i + 1];
            }
            if (kata.charAt(i) == 'a') {
                suffixA[i]++;
            }
        }

        for (int i = 0; i < n; i++) {
            if (kata.charAt(i) == 'd') {
                int sLeft = (i > 0) ? prefixS[i - 1] : 0;
                if (i > 0 && kata.charAt(i - 1) == 's') {
                    sLeft--; 
                }

                int aRight = (i < n - 1) ? suffixA[i + 1] : 0;
                if (i < n - 1 && kata.charAt(i + 1) == 'a') {
                    aRight--;  
                }
                countSDA += (long) sLeft * aRight;
            }
        }

        return countSDA;
    }

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