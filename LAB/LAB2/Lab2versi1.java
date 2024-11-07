import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Lab2versi1{
    private static InputReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);
        System.out.println(count("ssddaa", "sda"));

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

    static int count(String a, String b){
    int m = a.length();
    int n = b.length();
 
    // Create a table to store
    // results of sub-problems
    int lookup[][] = new int[m + 1][n + 1];
 
    // If first string is empty
    for (int i = 0; i <= n; ++i)
        lookup[0][i] = 0;
 
    // If second string is empty
    for (int i = 0; i <= m; ++i)
        lookup[i][0] = 1;
 
    // Fill lookup[][] in 
    // bottom up manner
    for (int i = 1; i <= m; i++)
    {
        for (int j = 1; j <= n; j++)
        {
            // If last characters are 
            // same, we have two options -
            // 1. consider last characters 
            //    of both strings in solution
            // 2. ignore last character
            //    of first string
            if (a.charAt(i - 1) == b.charAt(j - 1))
                lookup[i][j] = lookup[i - 1][j - 1] + 
                               lookup[i - 1][j];
                 
            else
                // If last character are 
                // different, ignore last
                // character of first string
                lookup[i][j] = lookup[i - 1][j];
        }
    }
 
    return lookup[m][n];
    }
}