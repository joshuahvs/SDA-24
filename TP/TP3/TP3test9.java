import java.io.*;
import java.util.*;

public class TP3test9 {
    private static InputReader in;
    private static PrintWriter out;

    static int V, E;
    static List<Edge>[] adjList;
    static int[] PiNumbers;

    static int currentCity = 1;
    static String currentPassword = "0000";

    // Compact password distance tracking
    static Map<String, int[]> passwordCache = new HashMap<>(50);

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        out = new PrintWriter(System.out);
        in = new InputReader(inputStream);

        V = in.nextInt();
        E = in.nextInt();

        // Compact adjacency list initialization
        adjList = new ArrayList[V + 1];
        for (int i = 0; i <= V; i++) {
            adjList[i] = new ArrayList<>();
        }

        // Read roads
        for (int i = 0; i < E; i++) {
            int vi = in.nextInt();
            int vj = in.nextInt();
            int li = in.nextInt();
            adjList[vi].add(new Edge(vj, li));
            adjList[vj].add(new Edge(vi, li));
        }

        // Read Pi Numbers
        int P = in.nextInt();
        PiNumbers = new int[P];
        for (int i = 0; i < P; i++) {
            PiNumbers[i] = in.nextInt();
        }

        // Process queries
        int Q = in.nextInt();
        processQueries(Q);

        out.flush();
    }

    private static void processQueries(int Q) {
        for (int q = 0; q < Q; q++) {
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(" ");
            switch (parts[0].charAt(0)) {
                case 'R': handleRCommand(parts); break;
                case 'F': handleFCommand(parts); break;
                case 'M': handleMCommand(parts); break;
                case 'J': handleJCommand(parts); break;
            }
        }
    }

    private static void handleRCommand(String[] parts) {
        if (parts.length < 2) {
            out.println(-1);
            return;
        }
        int energy = Integer.parseInt(parts[1]);
        boolean[] visited = new boolean[V + 1];
        visited[currentCity] = true;

        Queue<Integer> queue = new LinkedList<>();
        queue.add(currentCity);
        int reachableCities = 0;

        while (!queue.isEmpty()) {
            int city = queue.poll();
            for (Edge edge : adjList[city]) {
                if (!visited[edge.to] && edge.weight <= energy) {
                    visited[edge.to] = true;
                    reachableCities++;
                    queue.add(edge.to);
                }
            }
        }

        out.println(reachableCities == 0 ? -1 : reachableCities);
    }

    private static void handleFCommand(String[] parts) {
        if (parts.length < 2) {
            out.println(-1);
            return;
        }
        int targetCity = Integer.parseInt(parts[1]);
        int[] dist = dijkstraShortestPath(currentCity, targetCity);
        out.println(dist[targetCity] == Integer.MAX_VALUE ? -1 : dist[targetCity]);
    }

    private static void handleMCommand(String[] parts) {
        if (parts.length < 3) {
            out.println(-1);
            return;
        }
        int targetCity = Integer.parseInt(parts[1]);
        String password = parts[2];

        int result;
        if (!passwordCache.containsKey(currentPassword)) {
            passwordCache.put(currentPassword, compactBfsPassword(currentPassword));
        }
        
        int[] dist = passwordCache.get(currentPassword);
        int pwdInt = Integer.parseInt(password);
        result = (pwdInt >= 0 && pwdInt < dist.length) ? dist[pwdInt] : -1;

        if (result != -1) {
            currentPassword = password;
        }
        currentCity = targetCity;
        out.println(result);
    }

    private static void handleJCommand(String[] parts) {
        if (parts.length < 2) {
            out.println(-1);
            return;
        }
        int startCity = Integer.parseInt(parts[1]);
        long totalDistance = kruskalMST(startCity);
        out.println(totalDistance == -1 ? -1 : totalDistance);
    }

    private static int[] dijkstraShortestPath(int start, int end) {
        int[] dist = new int[V + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.dist));
        pq.offer(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.id == end) break;

            for (Edge edge : adjList[current.id]) {
                int newDist = current.dist + edge.weight;
                if (newDist < dist[edge.to]) {
                    dist[edge.to] = newDist;
                    pq.offer(new Node(edge.to, newDist));
                }
            }
        }
        return dist;
    }

    private static int[] compactBfsPassword(String start) {
        int[] dist = new int[10000];
        Arrays.fill(dist, -1);
        Queue<String> queue = new LinkedList<>();
        
        dist[Integer.parseInt(start)] = 0;
        queue.offer(start);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            int currInt = Integer.parseInt(curr);
            int currDist = dist[currInt];

            for (int pi : PiNumbers) {
                String next = addPasswords(curr, String.format("%04d", pi));
                int nextInt = Integer.parseInt(next);
                
                if (nextInt < dist.length && (dist[nextInt] == -1 || dist[nextInt] > currDist + 1)) {
                    dist[nextInt] = currDist + 1;
                    queue.offer(next);
                }
            }
        }
        return dist;
    }

    private static long kruskalMST(int startCity) {
        List<Road> roads = new ArrayList<>();
        for (int i = 1; i <= V; i++) {
            for (Edge edge : adjList[i]) {
                if (i < edge.to) {
                    roads.add(new Road(i, edge.to, edge.weight));
                }
            }
        }
        roads.sort(Comparator.comparingInt(r -> r.weight));

        DisjointSet ds = new DisjointSet(V + 1);
        long totalDistance = 0;

        // Add roads from start city first
        for (Edge edge : adjList[startCity]) {
            if (ds.union(startCity, edge.to)) {
                totalDistance += edge.weight;
            }
        }

        // Add remaining roads
        for (Road road : roads) {
            if (ds.union(road.from, road.to)) {
                totalDistance += road.weight;
            }
        }

        return ds.isFullyConnected() ? totalDistance : -1;
    }

    // Simplified helper methods and classes follow...
    static String addPasswords(String a, String b) {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int digitSum = (a.charAt(i) - '0') + (b.charAt(i) - '0');
            sb.append(digitSum % 10);
        }
        return sb.toString();
    }

    static class Edge {
        int to, weight;
        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    static class Road {
        int from, to, weight;
        Road(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    static class Node {
        int id, dist;
        Node(int id, int dist) {
            this.id = id;
            this.dist = dist;
        }
    }

    static class DisjointSet {
        int[] parent, rank;
        DisjointSet(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        boolean union(int x, int y) {
            int rootX = find(x), rootY = find(y);
            if (rootX == rootY) return false;

            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            return true;
        }

        boolean isFullyConnected() {
            int root = find(1);
            for (int i = 2; i < parent.length; i++) {
                if (find(i) != root) return false;
            }
            return true;
        }
    }

    // Fast Input Reader
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 1 << 16);
            tokenizer = null;
        }

        public String nextLine() {
            tokenizer = null;
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                String line = nextLine();
                if (line == null) return null;
                tokenizer = new StringTokenizer(line);
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            String token = next();
            if (token == null) throw new NoSuchElementException();
            return Integer.parseInt(token);
        }
    }
}