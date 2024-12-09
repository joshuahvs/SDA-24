import java.io.*;
import java.util.*;

public class Lab8p {
    private static FastInput input;
    private static PrintWriter output;
    private static int rows, cols;
    private static char[][] grid;

    public static void main(String[] args) {
        input = new FastInput(System.in);
        output = new PrintWriter(System.out);

        rows = input.nextInt();
        cols = input.nextInt();

        grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String row = input.next();
            grid[i] = row.toCharArray();
        }

        output.println(calculateGhosts());
        output.close();
    }

    private static int calculateGhosts() {
        int startX = -1, startY = -1, endX = -1, endY = -1;

        // Find positions of start ('S') and exit ('E')
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'S') {
                    startX = i;
                    startY = j;
                } else if (grid[i][j] == 'E') {
                    endX = i;
                    endY = j;
                }
            }
        }

        int scoobyDistance = findShortestPath(startX, startY, endX, endY);
        if (scoobyDistance == Integer.MAX_VALUE) {
            return 0;
        }

        int[][] ghostDistances = computeDistances(endX, endY);
        return countThreateningGhosts(scoobyDistance, ghostDistances);
    }

    private static int findShortestPath(int startX, int startY, int endX, int endY) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] distances = initializeDistanceGrid();

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});
        distances[startX][startY] = 0;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0], y = current[1];

            if (x == endX && y == endY) {
                return distances[x][y];
            }

            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (isValidCell(newX, newY) && distances[newX][newY] == Integer.MAX_VALUE) {
                    distances[newX][newY] = distances[x][y] + 1;
                    queue.offer(new int[]{newX, newY});
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    private static int[][] computeDistances(int startX, int startY) {
        int[][] distances = initializeDistanceGrid();
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});
        distances[startX][startY] = 0;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0], y = current[1];

            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (isValidCell(newX, newY) && distances[newX][newY] == Integer.MAX_VALUE) {
                    distances[newX][newY] = distances[x][y] + 1;
                    queue.offer(new int[]{newX, newY});
                }
            }
        }

        return distances;
    }

    private static int countThreateningGhosts(int maxSteps, int[][] ghostDistances) {
        int ghostCount = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Character.isDigit(grid[i][j])) {
                    int ghostDistance = ghostDistances[i][j];
                    if (ghostDistance != Integer.MAX_VALUE && ghostDistance <= maxSteps) {
                        ghostCount += grid[i][j] - '0';
                    }
                }
            }
        }

        return ghostCount;
    }

    private static int[][] initializeDistanceGrid() {
        int[][] distances = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }
        return distances;
    }

    private static boolean isValidCell(int x, int y) {
        return x >= 0 && x < rows && y >= 0 && y < cols && grid[x][y] != 'R';
    }

    static class FastInput {
        private BufferedReader reader;
        private StringTokenizer tokenizer;

        public FastInput(InputStream stream) {
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
