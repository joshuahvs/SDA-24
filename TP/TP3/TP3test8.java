import java.io.*;
import java.util.*;

public class TP3test8{
    private static InputReader in;
    private static PrintWriter out;

    static int V, E;
    static int[] adjList[];
    static int[] adjWeight[];
    static int[] PiNumbers;

    static int currentCity = 1;
    static String currentPassword = "0000";

    // Drastically reduce memory usage by using primitive arrays
    static int[][] passwordDistances = new int[50][10000]; // Limited cache
    static String[] passwordKeys = new String[50];
    static int passwordCacheSize = 0;

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        out = new PrintWriter(System.out);
        in = new InputReader(inputStream);

        V = in.nextInt();
        E = in.nextInt();

        // Use primitive int arrays instead of List<Edge>
        adjList = new int[V + 1][V + 1];
        adjWeight = new int[V + 1][V + 1];
        for (int i = 0; i <= V; i++) {
            Arrays.fill(adjList[i], -1);
        }

        // Read roads more memory efficiently
        for (int i = 0; i < E; i++) {
            int vi = in.nextInt();
            int vj = in.nextInt();
            int li = in.nextInt();
            addEdge(vi, vj, li);
            addEdge(vj, vi, li);
        }

        // Read Pi Numbers
        int P = in.nextInt();
        PiNumbers = new int[P];
        for (int i = 0; i < P; i++) {
            PiNumbers[i] = in.nextInt();
        }

        // Process queries
        int Q = in.nextInt();
        for (int q = 0; q < Q; q++) {
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                q--;
                continue;
            }

            String[] parts = line.split(" ");
            switch (parts[0].charAt(0)) {
                case 'R': handleRCommand(Integer.parseInt(parts[1])); break;
                case 'F': handleFCommand(Integer.parseInt(parts[1])); break;
                case 'M': handleMCommand(Integer.parseInt(parts[1]), parts[2]); break;
                case 'J': handleJCommand(Integer.parseInt(parts[1])); break;
            }
        }

        out.flush();
    }

    private static void addEdge(int from, int to, int weight) {
        for (int i = 0; i < V + 1; i++) {
            if (adjList[from][i] == -1) {
                adjList[from][i] = to;
                adjWeight[from][i] = weight;
                break;
            }
        }
    }

    private static void handleRCommand(int energy) {
        int[] queue = new int[V + 1];
        int front = 0, rear = 0;
        boolean[] visited = new boolean[V + 1];
        
        queue[rear++] = currentCity;
        visited[currentCity] = true;
        int reachableCities = 0;

        while (front < rear) {
            int city = queue[front++];
            for (int i = 0; adjList[city][i] != -1; i++) {
                int nextCity = adjList[city][i];
                int distance = adjWeight[city][i];

                if (distance <= energy && !visited[nextCity]) {
                    visited[nextCity] = true;
                    reachableCities++;
                    queue[rear++] = nextCity;
                }
            }
        }

        out.println(reachableCities > 0 ? reachableCities : -1);
    }

    private static void handleFCommand(int targetCity) {
        int[] dist = new int[V + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        
        dist[currentCity] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(
            (a, b) -> Integer.compare(a[1], b[1])
        );
        pq.offer(new int[]{currentCity, 0});
        boolean[] visited = new boolean[V + 1];
    
        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            
            if (visited[u]) continue;
            visited[u] = true;
    
            for (int i = 0; adjList[u][i] != -1; i++) {
                int v = adjList[u][i];
                int w = adjWeight[u][i];
    
                if (!visited[v] && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }
    
        out.println(dist[targetCity] == Integer.MAX_VALUE ? -1 : dist[targetCity]);
    }

    private static void handleMCommand(int targetCity, String password) {
        int result = findPasswordDistance(password);
        
        if (result != -1) {
            currentPassword = password;
        }
        currentCity = targetCity;
        out.println(result);
    }

    private static int findPasswordDistance(String password) {
        for (int i = 0; i < passwordCacheSize; i++) {
            if (passwordKeys[i].equals(currentPassword)) {
                int passwordInt = Integer.parseInt(password);
                return passwordInt >= 0 && passwordInt < 10000 ? 
                    passwordDistances[i][passwordInt] : -1;
            }
        }

        int[] dist = bfsPassword(currentPassword);
        
        // Manage limited cache
        if (passwordCacheSize < 50) {
            passwordKeys[passwordCacheSize] = currentPassword;
            passwordDistances[passwordCacheSize] = dist;
            passwordCacheSize++;
        }

        int passwordInt = Integer.parseInt(password);
        return passwordInt >= 0 && passwordInt < 10000 ? dist[passwordInt] : -1;
    }

    private static void handleJCommand(int startCity) {
        int[] parent = new int[V + 1];
        for (int i = 1; i <= V; i++) parent[i] = i;
        
        long totalDistance = 0;
        int[] roads = new int[E * 3];
        int roadCount = 0;

        // Collect direct connected roads for startCity
        for (int i = 0; adjList[startCity][i] != -1; i++) {
            int to = adjList[startCity][i];
            int weight = adjWeight[startCity][i];
            if (find(parent, startCity) != find(parent, to)) {
                union(parent, startCity, to);
                totalDistance += weight;
            }
        }

        // Collect all other roads
        for (int from = 1; from <= V; from++) {
            if (from == startCity) continue;
            for (int i = 0; adjList[from][i] != -1; i++) {
                int to = adjList[from][i];
                int weight = adjWeight[from][i];
                if (from < to) {
                    roads[roadCount++] = from;
                    roads[roadCount++] = to;
                    roads[roadCount++] = weight;
                }
            }
        }

        // Sort roads by weight
        sortRoads(roads, roadCount);

        // Build Minimum Spanning Tree
        for (int i = 0; i < roadCount; i += 3) {
            int from = roads[i], to = roads[i+1], weight = roads[i+2];
            if (find(parent, from) != find(parent, to)) {
                union(parent, from, to);
                totalDistance += weight;
            }
        }

        // Check connectivity
        int uniqueSets = 0;
        for (int i = 1; i <= V; i++) {
            if (parent[i] == i) {
                uniqueSets++;
                if (uniqueSets > 1) break;
            }
        }

        out.println(uniqueSets == 1 ? totalDistance : -1);
    }

    private static void sortRoads(int[] roads, int roadCount) {
        // Quick sort for roads
        quickSort(roads, 0, roadCount/3 - 1);
    }

    private static void quickSort(int[] roads, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(roads, low, high);
            quickSort(roads, low, pivotIndex - 1);
            quickSort(roads, pivotIndex + 1, high);
        }
    }

    private static int partition(int[] roads, int low, int high) {
        int pivot = roads[high * 3 + 2];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (roads[j * 3 + 2] < pivot) {
                i++;
                swapRoads(roads, i, j);
            }
        }
        swapRoads(roads, i + 1, high);
        return i + 1;
    }

    private static void swapRoads(int[] roads, int i, int j) {
        int tempFrom = roads[i * 3];
        int tempTo = roads[i * 3 + 1];
        int tempWeight = roads[i * 3 + 2];
        
        roads[i * 3] = roads[j * 3];
        roads[i * 3 + 1] = roads[j * 3 + 1];
        roads[i * 3 + 2] = roads[j * 3 + 2];
        
        roads[j * 3] = tempFrom;
        roads[j * 3 + 1] = tempTo;
        roads[j * 3 + 2] = tempWeight;
    }

    private static int[] bfsPassword(String start) {
        int[] dist = new int[10000];
        Arrays.fill(dist, -1);
        
        int[] queue = new int[10000];
        int front = 0, rear = 0;
        
        int startInt = Integer.parseInt(start);
        dist[startInt] = 0;
        queue[rear++] = startInt;

        while (front < rear) {
            int currInt = queue[front++];
            String curr = String.format("%04d", currInt);
            int currDist = dist[currInt];

            for (int pi : PiNumbers) {
                String next = addPasswords(curr, String.format("%04d", pi));
                int nextInt = Integer.parseInt(next);
                
                if (nextInt >= 0 && nextInt < 10000 && 
                    (dist[nextInt] == -1 || dist[nextInt] > currDist + 1)) {
                    dist[nextInt] = currDist + 1;
                    queue[rear++] = nextInt;
                }
            }
        }
        return dist;
    }

    private static String addPasswords(String a, String b) {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int digitSum = (a.charAt(i) - '0') + (b.charAt(i) - '0');
            sb.append(digitSum % 10);
        }
        return sb.toString();
    }

    private static int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }

    private static void union(int[] parent, int x, int y) {
        int rootX = find(parent, x);
        int rootY = find(parent, y);
        if (rootX != rootY) {
            parent[rootY] = rootX;
        }
    }

    private static int extractMin(int[] heap, int[] dist, int heapSize) {
        int minIndex = 0;
        for (int i = 1; i < heapSize; i++) {
            if (dist[heap[i]] < dist[heap[minIndex]]) {
                minIndex = i;
            }
        }
        int result = heap[minIndex];
        heap[minIndex] = heap[heapSize - 1];
        return result;
    }

    private static void insertHeap(int[] heap, int val, int heapSize, int[] dist) {
        heap[heapSize] = val;
    }

    static class InputReader {
        private final InputStream stream;
        private final byte[] buf = new byte[1 << 16];
        private int curChar, numChars;

        public InputReader(InputStream stream) {
            this.stream = stream;
        }

        public String nextLine() throws IOException {
            byte[] buf = new byte[1 << 16];
            int cnt = 0;
            byte c;
            while ((c = (byte) read()) != -1) {
                if (c == '\n') break;
                buf[cnt++] = c;
            }
            return new String(buf, 0, cnt);
        }

        public int nextInt() throws IOException {
            int c = read(), x = 0, sign = 1;
            while (isWhitespace(c)) c = read();
            if (c == '-') { sign = -1; c = read(); }
            do { x = x * 10 + (c - '0'); } while ((c = read()) >= '0' && c <= '9');
            return x * sign;
        }

        private boolean isWhitespace(int c) {
            return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
        }

        private int read() throws IOException {
            if (numChars == -1) throw new IOException();
            if (curChar >= numChars) {
                curChar = 0;
                numChars = stream.read(buf);
                if (numChars <= 0) return -1;
            }
            return buf[curChar++];
        }
    }
}