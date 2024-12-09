import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;

public class Lab8ptest {
    private static InputReader in;
    private static PrintWriter out;
    private static int B;
    private static int K;
    private static char[][] map;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        // Membaca ukuran grid
        B = in.nextInt();
        K = in.nextInt();

        // Membaca peta grid
        map = new char[B][K];
        for (int i = 0; i < B; i++) {
            String inputRow = in.next();
            for (int j = 0; j < inputRow.length(); j++) {
                map[i][j] = inputRow.charAt(j);
            }
        }

        int result = solve();
        out.println(result);
        out.close();
    }

    // TODO: Implementasi metode solve (Silakan modifikasi sesuai kebutuhan)
    private static int solve() {
        int startRow = -1, startCol = -1;
        int exitRow = -1, exitCol = -1;
        
        // Mencari posisi S dan E
        for (int i = 0; i < B; i++) {
            for (int j = 0; j < K; j++) {
                if (map[i][j] == 'S') {
                    startRow = i;
                    startCol = j;
                } else if (map[i][j] == 'E') {
                    exitRow = i;
                    exitCol = j;
                }
            }
        }

        // Hitung jarak Manhattan Scooby ke exit
        int scoobySteps = Math.abs(exitRow - startRow) + Math.abs(exitCol - startCol);
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        
        // BFS dari posisi Scooby untuk mendapatkan jarak sebenarnya ke exit
        int[][] scoobyDist = new int[B][K];
        for (int i = 0; i < B; i++) {
            Arrays.fill(scoobyDist[i], Integer.MAX_VALUE);
        }
        
        Queue<int[]> scoobyQueue = new LinkedList<>();
        scoobyQueue.offer(new int[]{startRow, startCol, 0});
        scoobyDist[startRow][startCol] = 0;
        
        // Cari jarak sebenarnya Scooby ke exit
        while (!scoobyQueue.isEmpty()) {
            int[] curr = scoobyQueue.poll();
            int row = curr[0], col = curr[1], dist = curr[2];
            
            if (row == exitRow && col == exitCol) {
                scoobySteps = dist;
                break;
            }
            
            for (int[] dir : dirs) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                
                if (newRow >= 0 && newRow < B && newCol >= 0 && newCol < K 
                    && map[newRow][newCol] != 'R' 
                    && scoobyDist[newRow][newCol] == Integer.MAX_VALUE) {
                    scoobyDist[newRow][newCol] = dist + 1;
                    scoobyQueue.offer(new int[]{newRow, newCol, dist + 1});
                }
            }
        }
        
        // Jika Scooby tidak bisa mencapai exit
        if (scoobyDist[exitRow][exitCol] == Integer.MAX_VALUE) {
            return 0;
        }
        
        // BFS dari exit untuk mendapatkan jarak minimum hantu ke exit
        int[][] ghostDist = new int[B][K];
        for (int i = 0; i < B; i++) {
            Arrays.fill(ghostDist[i], Integer.MAX_VALUE);
        }
        
        Queue<int[]> ghostQueue = new LinkedList<>();
        ghostQueue.offer(new int[]{exitRow, exitCol, 0});
        ghostDist[exitRow][exitCol] = 0;
        
        while (!ghostQueue.isEmpty()) {
            int[] curr = ghostQueue.poll();
            int row = curr[0], col = curr[1], dist = curr[2];
            
            for (int[] dir : dirs) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                
                if (newRow >= 0 && newRow < B && newCol >= 0 && newCol < K 
                    && map[newRow][newCol] != 'R' 
                    && ghostDist[newRow][newCol] == Integer.MAX_VALUE) {
                    ghostDist[newRow][newCol] = dist + 1;
                    ghostQueue.offer(new int[]{newRow, newCol, dist + 1});
                }
            }
        }
        
        // Hitung total hantu yang bisa mencapai exit sebelum atau bersamaan dengan Scooby
        int ghostsAtExit = 0;
        for (int i = 0; i < B; i++) {
            for (int j = 0; j < K; j++) {
                if (Character.isDigit(map[i][j])) {
                    if (ghostDist[i][j] != Integer.MAX_VALUE && ghostDist[i][j] <= scoobySteps) {
                        ghostsAtExit += (map[i][j] - '0');
                    }
                }
            }
        }
        
        return ghostsAtExit;
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