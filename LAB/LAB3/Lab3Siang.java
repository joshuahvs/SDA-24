import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Lab3Siang {
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);
        // Read inputs
        int N = in.nextInteger();
        
        ArrayList<Long> heightList = new ArrayList<>();
        ArrayList<Long> theAnswer = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            long height = in.nextInteger();
            heightList.add(height);
        }

        // Loop through each person
        for (int i = 0; i < N; i++) {
            int count = 0;
            long currentHeight = heightList.get(i);
            ArrayList<Long> tempList = new ArrayList<>();

            // Check to the right of the current person
            for (int j = i + 1; j < N; j++) {
                // If the next person is shorter, they can be seen
                if (heightList.get(j) < currentHeight) {
                    tempList.add(heightList.add){
                    
                    }
                    count++;
                } else {
                    break;
                }
            }
            
            // Add result for this person
            theAnswer.add((long) count);
        }

        // Print the results
        for (long ans : theAnswer) {
            System.out.print(ans + " ");
        }
        System.out.println();

        // // Read inputs
        // int N = in.nextInteger();

        // ArrayList<Long> heightList = new ArrayList<>();
        // ArrayList<Long> theAnswer = new ArrayList<>();
        

        // for (int i = 0; i < N; i++) {
        //     long height = in.nextInteger();
        //     heightList.add(height);
        // }
        // int currentIndex = 0;
        // ArrayList<Long> tempList = new ArrayList<>();
        // for (int i = currentIndex + 1; i < N; i++) { 
        //     if (heightList.get(i) < heightList.get(currentIndex)) {
        //         tempList.add(heightList.get(i)); 
        //     } else {
        //         break; 
        //     }
        //     long count = 1;
        //     for (int j= 0;j<tempList.size()-1;j++){
        //         if (tempList.get(j)<tempList.get(j+1)){
        //             count++;
        //         }else{
        //             break;
        //         }
        //     }
        //     count+=1;
        //     System.out.println(count);
        //     theAnswer.add(count);
        //     tempList.clear();
        //     currentIndex++;
        // }

        // // System.out.println(tempList.toString());
        // for (long i = 0; i < theAnswer.size(); i++){
        //     out.print(theAnswer.get((int)i) + " ");
        // }
        // out.println();
        // out.close();
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