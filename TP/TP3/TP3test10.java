import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public class TP3test10{
    private static InputReader in;
    private static PrintWriter out;

    static int V;
    static int E;
    static List<Edge>[] adjList;
    static int[] PiNumbers;

    static int currentCity = 1;
    static String currentPassword = "0000";

    static Map<String, int[]> passwordDistances = new HashMap<>();
    static Map<String, Integer> passwordIndex = new HashMap<>();
    static int passwordCounter = 0;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        out = new PrintWriter(System.out);
        in = new InputReader(inputStream);

        try {
            V = in.nextInt();
            E = in.nextInt();

            // Validate input bounds
            if (V <= 0 || E < 0) {
                out.println(-1);
                out.flush();
                return;
            }

            // Initialize adjacency list
            adjList = new ArrayList[V + 1];
            for (int i = 0; i <= V; i++) {
                adjList[i] = new ArrayList<>();
            }

            // Read E lines of roads
            for (int i = 0; i < E; i++) {
                int vi = in.nextInt();
                int vj = in.nextInt();
                int li = in.nextInt();
                
                // Validate city indices
                if (vi < 1 || vi > V || vj < 1 || vj > V) {
                    out.println(-1);
                    out.flush();
                    return;
                }
                
                adjList[vi].add(new Edge(vj, li));
                adjList[vj].add(new Edge(vi, li)); // Assuming undirected graph
            }

            // Read P and the digits
            int P = in.nextInt();
            
            // Validate P
            if (P < 0) {
                out.println(-1);
                out.flush();
                return;
            }
            
            PiNumbers = new int[P];
            for (int i = 0; i < P; i++) {
                PiNumbers[i] = in.nextInt();
                
                // Validate Pi digits
                if (PiNumbers[i] < 0 || PiNumbers[i] > 9999) {
                    out.println(-1);
                    out.flush();
                    return;
                }
            }

            // Read Q and the activities
            int Q = in.nextInt();
            
            // Validate Q
            if (Q < 0) {
                out.println(-1);
                out.flush();
                return;
            }

            int processed = 0;
            while (processed < Q) {
                String line = in.nextLine();
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.trim().split(" ");
                if (parts.length == 0) {
                    continue;
                }

                char command = parts[0].charAt(0);

                try {
                    switch (command) {
                        case 'R':
                            handleRCommand(parts);
                            break;
                        case 'F':
                            handleFCommand(parts);
                            break;
                        case 'M':
                            handleMCommand(parts);
                            break;
                        case 'J':
                            handleJCommand(parts);
                            break;
                        default:
                            out.println(-1);
                            break;
                    }
                } catch (Exception e) {
                    out.println(-1);
                }
                processed++;
            }
        } catch (Exception e) {
            out.println(-1);
        } finally {
            out.flush();
            out.close();
        }
    }

    private static void handleRCommand(String[] parts) {
        if (parts.length < 2) {
            out.println(-1);
            return;
        }
        
        int energy;
        try {
            energy = Integer.parseInt(parts[1]);
            if (energy < 0) {
                out.println(-1);
                return;
            }
        } catch (NumberFormatException e) {
            out.println(-1);
            return;
        }

        boolean[] visited = new boolean[V + 1];
        visited[currentCity] = true;

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(currentCity);
        int reachableCities = 0;

        while (!queue.isEmpty()) {
            int city = queue.poll();
            for (Edge edge : adjList[city]) {
                int nextCity = edge.to;
                int distance = edge.weight;

                if (distance <= energy && !visited[nextCity]) {
                    visited[nextCity] = true;
                    reachableCities++;
                    queue.add(nextCity);
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
        
        int targetCity;
        try {
            targetCity = Integer.parseInt(parts[1]);
            if (targetCity < 1 || targetCity > V) {
                out.println(-1);
                return;
            }
        } catch (NumberFormatException e) {
            out.println(-1);
            return;
        }

        int[] dist = new int[V + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[currentCity] = 0;

        boolean[] visitedF = new boolean[V + 1];
        PriorityQueue<Node> heap = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
        heap.add(new Node(currentCity, 0));

        while (!heap.isEmpty()) {
            Node node = heap.poll();
            int u = node.id;

            if (visitedF[u]) continue;
            visitedF[u] = true;

            for (Edge edge : adjList[u]) {
                int v = edge.to;
                int w = edge.weight;

                if (!visitedF[v] && dist[v] > dist[u] + w) {
                    dist[v] = dist[u] + w;
                    heap.add(new Node(v, dist[v]));
                }
            }
        }

        out.println(dist[targetCity] == Integer.MAX_VALUE ? -1 : dist[targetCity]);
    }

    private static void handleMCommand(String[] parts) {
        if (parts.length < 3) {
            out.println(-1);
            return;
        }
        
        int targetCity;
        String password = parts[2];
        
        try {
            targetCity = Integer.parseInt(parts[1]);
            
            // Validate input
            if (targetCity < 1 || targetCity > V || 
                password.length() != 4 || !password.matches("\\d{4}")) {
                out.println(-1);
                return;
            }
        } catch (NumberFormatException e) {
            out.println(-1);
            return;
        }

        int result;
        if (passwordDistances.containsKey(currentPassword)) {
            int[] dist = passwordDistances.get(currentPassword);
            int pwdInt = Integer.parseInt(password);
            result = (pwdInt >= 0 && pwdInt < dist.length) ? dist[pwdInt] : -1;
        } else {
            int[] dist = bfsPassword(currentPassword, PiNumbers);
            passwordDistances.put(currentPassword, dist);
            passwordIndex.put(currentPassword, passwordCounter++);
            int pwdInt = Integer.parseInt(password);
            result = (pwdInt >= 0 && pwdInt < dist.length) ? dist[pwdInt] : -1;
        }

        if (result != -1) {
            currentPassword = password;
            currentCity = targetCity;
        }
        out.println(result);
    }

    private static void handleJCommand(String[] parts) {
        if (parts.length < 2) {
            out.println(-1);
            return;
        }
        
        int startCity;
        try {
            startCity = Integer.parseInt(parts[1]);
            if (startCity < 1 || startCity > V) {
                out.println(-1);
                return;
            }
        } catch (NumberFormatException e) {
            out.println(-1);
            return;
        }

        long totalDistance = 0;
        DisjointSet ds = new DisjointSet(V + 1);

        List<Road> allRoads = new ArrayList<>();

        for (Edge edge : adjList[startCity]) {
            int to = edge.to;
            int weight = edge.weight;
            if (ds.find(startCity) != ds.find(to)) {
                ds.union(startCity, to);
                totalDistance += weight;
            }
        }

        for (int i = 1; i <= V; i++) {
            if (i == startCity) continue;
            for (Edge edge : adjList[i]) {
                int j = edge.to;
                int w = edge.weight;
                if (i < j) {
                    allRoads.add(new Road(i, j, w));
                }
            }
        }

        allRoads.sort(Comparator.comparingInt(r -> r.weight));

        for (Road road : allRoads) {
            if (ds.find(road.from) != ds.find(road.to)) {
                ds.union(road.from, road.to);
                totalDistance += road.weight;
            }
        }

        int uniqueSets = 0;
        for (int i = 1; i <= V; i++) {
            if (ds.parent[i] == i) {
                uniqueSets++;
                if (uniqueSets > 1) break;
            }
        }

        out.println(uniqueSets == 1 ? totalDistance : -1);
    }

    // BFS for password operations
    static int[] bfsPassword(String start, int[] PiNumbers) {
        int[] dist = new int[10000];
        Arrays.fill(dist, -1);
        ArrayDeque<String> queue = new ArrayDeque<>();
        int startInt = Integer.parseInt(start);
        dist[startInt] = 0;
        queue.add(start);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            int currInt = Integer.parseInt(curr);
            int currDist = dist[currInt];

            for (int pi : PiNumbers) {
                String next = addPasswords(curr, String.format("%04d", pi));
                int nextInt = Integer.parseInt(next);
                if (nextInt >= 0 && nextInt < dist.length && (dist[nextInt] == -1 || dist[nextInt] > currDist + 1)) {
                    dist[nextInt] = currDist + 1;
                    queue.add(next);
                }
            }
        }
        return dist;
    }

    // Helper method to add passwords
    static String addPasswords(String a, String b) {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int digitSum = (a.charAt(i) - '0') + (b.charAt(i) - '0');
            sb.append(digitSum % 10);
        }
        return sb.toString();
    }

    // Edge class for adjacency list
    static class Edge {
        int to;
        int weight;

        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    // Road class for Krusky's algorithm
    static class Road {
        int from;
        int to;
        int weight;

        Road(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    // Node class for Dijkstra's algorithm
    static class Node {
        int id;
        int dist;

        Node(int id, int dist) {
            this.id = id;
            this.dist = dist;
        }
    }

    // Disjoint Set (Union-Find) Implementation with Path Compression and Union by Rank
    static class DisjointSet {
        int[] parent;
        int[] rank;

        DisjointSet(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        void union(int x, int y) {
            int xRoot = find(x);
            int yRoot = find(y);
            if (xRoot == yRoot) {
                return;
            }
            // Union by rank
            if (rank[xRoot] < rank[yRoot]) {
                parent[xRoot] = yRoot;
            } else if (rank[xRoot] > rank[yRoot]) {
                parent[yRoot] = xRoot;
            } else {
                parent[yRoot] = xRoot;
                rank[xRoot]++;
            }
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