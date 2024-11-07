import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Stack;

public class Lab3p {
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read inputs
        long N = in.nextLong();
        long[] numbers = new long[(int) N];
        long[] answer = new long[(int) N];
        Stack<Long> stack = new Stack<Long>();
        
        for (int i = 0; i < N; i++) {
            numbers[i] = in.nextLong();
        }

        for (int i = 0; i < N; i++){
            long langkah = 0;
            while (!stack.isEmpty() && numbers[i] >= stack.peek()){
                stack.pop();
                langkah += 1;
            }
            if (stack.isEmpty()){
                langkah = 0;
            }
            else{
                langkah += 1;
            }
            answer[i] = langkah;
            stack.push(numbers[i]);
        }
        
        for (int i = 0; i<N; i++){
            out.print(answer[i] + " ");
        }
        out.close();
    }
    // TODO: Implement the logic here as required (e.g., a method to calculate result)

    // Example:
    // public static <ReturnType> yourMethodName(<Parameters>) {
    //    // Implement your logic here
    // }

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

        public long nextLong() {
            return Long.parseLong(next());
        }
    }
}