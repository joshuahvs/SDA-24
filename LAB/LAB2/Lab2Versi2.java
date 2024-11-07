import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab2Versi2 {
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read inputs
        String input = in.next();
        int sda = 0;
        int currentIndex = 0;
        int maxIndex = input.length();

        while (currentIndex != maxIndex) {
            if (input.charAt(currentIndex) == 's') {
                for (int i = currentIndex + 1; i < maxIndex; i++) {
                    if (input.charAt(i) == 'd') {
                        for (int j = i + 1; j < maxIndex; j++) {
                            if (input.charAt(j) == 'a') {
                                sda += 1;
                            }
                        }
                    }
                }
            }
            currentIndex++;
        }
        System.out.println(sda);
       //TODO
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
        //TODO
        return 0;
    }
}