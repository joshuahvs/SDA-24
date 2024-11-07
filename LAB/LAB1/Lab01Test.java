import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab01Test {
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read inputs
        long N = in.nextInteger();
        long M = in.nextInteger();
        int K = in.nextInteger();
        long X = in.nextInteger();
        long Y = in.nextInteger();

        long A[] = new long[K];
        long B[] = new long[K];

        for (int i = 0; i < K; i++) {
            A[i] = in.nextInteger();
            B[i] = in.nextInteger();
        }

        long ans = M*(X-1)+Y;

        // TODO: Write your code here
        // ! Hint:  Note that to get the full score, you might need to edit other parts of the code as well
        for (int i=0; i<K; i++){
            if(A[i]<X){
                ans -= 1;
            } else if(A[i]== X && B[i]< Y){
                ans -= 1;
            }
        }
        System.out.println(ans);
        // don't forget to close/flush the output
        out.close();
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
