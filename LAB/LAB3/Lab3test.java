import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

public class Lab3test{
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        int N = in.nextInteger();
        long[] heightArray = new long[N];

        // Process inputs
        for (int i = 0; i < N; i++) {
            int height = in.nextInteger();
            heightArray[i] = height;
        }
        long[] result = new long[N];
        Stack<Long> stack = new Stack<>();

        //Reversed array
        long[] heightArrayReversed = new long[N];
        int currentIndex = 0;
        for (int i=N-1; i>=0;i--){
            heightArrayReversed[currentIndex] = heightArray[i];
            currentIndex++;
        }
        
        stack.push(heightArrayReversed[N-1]);
        for (int i = N-1; i>= 0; i--){
            int counter = 0;
            while(!stack.isEmpty() && stack.peek()<heightArrayReversed[i]){
                stack.pop();
                counter++;
            }
            if (stack.isEmpty()){
                result[i] = 0;
            } else{
                result[i]= counter+1;
            }
            stack.push(heightArrayReversed[i]);
        }
        result[0] = 0;

        // Iterator<Long> iterator = stack.iterator();
        // while (iterator.hasNext()) {
        //     System.out.println(iterator.next());
        // }
        // Output the result
        for (long ans : result) {
            System.out.print(ans + " ");
        }
        System.out.println();

        // don't forget to close/flush the output
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

        public int nextInteger() {
            return Integer.parseInt(next());
        }
    }
}