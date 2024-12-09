import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class TemplateLab{
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        // Membaca ukuran grid (baris B dan kolom K)
        int B = in.nextInt();
        int K = in.nextInt();

        // Membaca peta grid (Silakan modifikasi sesuai kebutuhan)
        char[][] map = new char[B][K];
        for (int i = 0; i < B; i++) {
            String inputRow = in.next();
            for (int j = 0; j < inputRow.length(); j++) {
                map[i][j] = inputRow.charAt(j);
            }
        }

        // TODO: Implementasi solusi Anda di sini
        int result = solve();

        // Cetak hasil
        out.println(result);
        out.close();
    }

    // TODO: Implementasi metode solve (Silakan modifikasi sesuai kebutuhan)
    private static int solve() {
        return 0;
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

        public int nextInt() {
            return Integer.parseInt(next());
        }
    }
}