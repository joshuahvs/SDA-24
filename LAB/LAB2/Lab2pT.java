import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab2pT {
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read inputs
        String word = in.next();
        System.out.println(solve(word));
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

    public static long solve(String kata) {
        long countSDA = 0;

        long countSS = 0;
        long countDD = 0;
        long countAA = 0;

        for (long i = 0; i < kata.length(); i++) {
            if (kata.charAt((int) i) == 'a') {
                countAA++;
            } else if (kata.charAt((int) i) == 'd') {
                countDD++;
            } else if (kata.charAt((int) i) == 's') {
                countSS++;
            }
        }

        long tempcountSS = 0;
        long tempcountAA = 0;
        long countSS2 = 0;
        long aDiKanan = 0;
        for (long i = 0; i < kata.length(); i++) {
            if (kata.charAt((int) i) == 'a') {
                tempcountAA++;
            } else if (kata.charAt((int) i) == 'd') {
                aDiKanan = countAA - tempcountAA;
                tempcountSS = countSS2;
                if (i != 0) {
                    if (kata.charAt((int) i - 1) == 's') {
                        tempcountSS--;
                    }
                    if (kata.charAt((int) i + 1) == 'a') {
                        aDiKanan--;
                    }
                }
                countSDA += tempcountSS * aDiKanan;
            } else if (kata.charAt((int) i) == 's') {
                countSS2++;
            }
        }
        return countSDA;
    }

}