import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;

public class TP3test5{
    private static InputReader in;
    private static PrintWriter out;

    static int V;
    static int E;
    static List<Edge>[] adjList;
    static int[] PiNumbers;

    static int currentCity = 1;
    static String currentPassword = "0000";

    static Map<String, int[]> passwordDistances = new HashMap<>();
    static int[] uniquePasswords = new int[5];
    static int uniquePasswordCount = 0;
    static Map<String, Integer> passwordIndex = new HashMap<>();
    static int passwordCounter = 0;


    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);


        V = in.nextInt();
        E = in.nextInt();

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
            adjList[vi].add(new Edge(vj, li));
            adjList[vj].add(new Edge(vi, li)); // Assuming undirected graph
        }

        // Read P and the digits
        int P = in.nextInt();
        PiNumbers = new int[P];
        for (int i = 0; i < P; i++) {
            PiNumbers[i] = in.nextInt();
        }


        // Read Q and the activities
        int Q = in.nextInt();
        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(" ");
            if (parts.length == 0) {
                continue;
            }

            char command = parts[0].charAt(0);

            if (command=='R'){
                int energy = Integer.parseInt(parts[1]);
                boolean[] visited = new boolean[V + 1];
                visited[currentCity] = true;

                // Queue for BFS: stores (city, remaining energy)
                List<int[]> queue = new ArrayList<>();
                queue.add(new int[]{currentCity, energy});
                int reachableCities = 0;

                while (!queue.isEmpty()) {
                    
                    int[] current = queue.remove(0);
                    int city = current[0];
                    int remainingEnergy = current[1];

                    for (Edge edge : adjList[city]) {
                        int nextCity = edge.to;
                        int distance = edge.weight;

                        if (distance <= remainingEnergy && !visited[nextCity]) {
                            visited[nextCity] = true;
                            reachableCities++;
                            // Add next city with full energy since Sofita recharges upon arrival
                            queue.add(new int[]{nextCity, energy});
                        }
                    }
                }
                // Output the result
                if (reachableCities == 0) {
                    out.println(-1);
                } else {
                    out.println(reachableCities);
                }

            } else if (command=='F'){
                int targetCity = Integer.parseInt(parts[1]);
                int[] dist = new int[V + 1];
                Arrays.fill(dist, Integer.MAX_VALUE);
                dist[currentCity] = 0;

                boolean[] visitedF = new boolean[V + 1];
                MinHeap heap = new MinHeap(V * 10);
                heap.insert(new Node(currentCity, 0));

                while (!heap.isEmpty()) {
                    Node node = heap.extractMin();
                    int u = node.id;
                    if (visitedF[u]) continue;
                    visitedF[u] = true;

                    for (Edge edge : adjList[u]) {
                        int v = edge.to;
                        int w = edge.weight;

                        if (!visitedF[v] && dist[v] > dist[u] + w) {
                            dist[v] = dist[u] + w;
                            heap.insert(new Node(v, dist[v]));
                        }
                    }
                }
                out.println(dist[targetCity]);

            } else if (command =='M'){
                int targetCity = Integer.parseInt(parts[1]);
                String password = parts[2];
                int result;
                if (passwordIndex.containsKey(currentPassword)) {
                    int idx = passwordIndex.get(currentPassword);
                    int[] dist = passwordDistances.get(currentPassword);
                    result = dist[Integer.parseInt(password)];
                } else {
                    int[] dist = bfsPassword(currentPassword, PiNumbers);
                    passwordDistances.put(currentPassword, dist);
                    passwordIndex.put(currentPassword, passwordCounter++);
                    result = dist[Integer.parseInt(password)];
                }
                if (result == -1) {
                    // Do not change the password
                } else {
                    currentPassword = password;
                }
                currentCity = targetCity;
                out.println(result);
            } else if (command == 'J'){
                int startCity = Integer.parseInt(parts[1]);
                long totalDistance = 0;
                boolean valid = true;

                // Initialize Disjoint Set
                DisjointSet ds = new DisjointSet(V + 1);

                // List to store all roads
                List<Road> allRoads = new ArrayList<>();

                // First, collect all roads directly connected to startCity
                List<Road> connectedRoads = new ArrayList<>();
                for (Edge edge : adjList[startCity]) {
                    int to = edge.to;
                    int weight = edge.weight;
                    connectedRoads.add(new Road(startCity, to, weight));
                }

                // Add connected roads to MST first
                for (Road road : connectedRoads) {
                    if (ds.find(road.from) != ds.find(road.to)) {
                        ds.union(road.from, road.to);
                        totalDistance += road.weight;
                    }
                }

                // Now, collect all other roads (avoid duplicates)
                for (int i = 1; i <= V; i++) {
                    for (Edge edge : adjList[i]) {
                        int j = edge.to;
                        int w = edge.weight;
                        if (i < j) { // To avoid duplicate roads
                            if (i != startCity && j != startCity) {
                                allRoads.add(new Road(i, j, w));
                            }
                        }
                    }
                }

                // Sort allRoads by weight
                Collections.sort(allRoads, new Comparator<Road>() {
                    @Override
                    public int compare(Road r1, Road r2) {
                        return Integer.compare(r1.weight, r2.weight);
                    }
                });

                // Add roads to MST
                for (Road road : allRoads) {
                    if (ds.find(road.from) != ds.find(road.to)) {
                        ds.union(road.from, road.to);
                        totalDistance += road.weight;
                    }
                }

                // Check if all cities are connected
                // Count unique parents
                int uniqueSets = 0;
                for (int i = 1; i <= V; i++) {
                    if (ds.parent[i] == i) {
                        uniqueSets++;
                    }
                }

                if (uniqueSets == 1) {
                    out.println(totalDistance);
                } else {
                    out.println(-1); // Or any other indication that not all cities are connected
                }
            }
                
        }

        out.close();
    }

     static int[] bfsPassword(String start, int[] PiNumbers) {
        int[] dist = new int[10000];
        Arrays.fill(dist, -1);
        Queue<String> queue = new LinkedList<>();
        dist[Integer.parseInt(start)] = 0;
        queue.add(start);
        while (!queue.isEmpty()) {
            String curr = queue.poll();
            int currDist = dist[Integer.parseInt(curr)];
            for (int pi : PiNumbers) {
                String next = addPasswords(curr, String.format("%04d", pi));
                int nextInt = Integer.parseInt(next);
                if (dist[nextInt] == -1 || dist[nextInt] > currDist + 1) {
                    dist[nextInt] = currDist + 1;
                    queue.add(next);
                }
            }
        }
        return dist;
    }

    static String addPasswords(String a, String b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int digitSum = (a.charAt(i) - '0') + (b.charAt(i) - '0');
            sb.append(digitSum % 10);
        }
        return sb.toString();
    }

    static class Edge implements Comparable<Edge> {
        int from; // Optional: can be omitted if not needed
        int to;
        int weight;

        // Constructor used for adjacency list
        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

        // Constructor used for roads list
        Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge o) {
            return this.weight - o.weight;
        }
    }

    static class Road {
        int from;
        int to;
        int weight;

        Road(int from, int to, int weight){
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    static class Node {
        int id;
        int dist;

        Node(int id, int dist) {
            this.id = id;
            this.dist = dist;
        }
    }

    static class MinHeap {
        Node[] heap;
        int size;

        MinHeap(int capacity) {
            heap = new Node[capacity + 1];
            size = 0;
        }

        void insert(Node node) {
            heap[++size] = node;
            int pos = size;
            while (pos > 1 && heap[pos].dist < heap[pos / 2].dist) {
                swap(pos, pos / 2);
                pos = pos / 2;
            }
        }

        Node extractMin() {
            Node min = heap[1];
            heap[1] = heap[size--];
            heapify(1);
            return min;
        }

        void heapify(int pos) {
            int smallest = pos;
            int left = 2 * pos;
            int right = 2 * pos + 1;
            if (left <= size && heap[left].dist < heap[smallest].dist) {
                smallest = left;
            }
            if (right <= size && heap[right].dist < heap[smallest].dist) {
                smallest = right;
            }
            if (smallest != pos) {
                swap(pos, smallest);
                heapify(smallest);
            }
        }

        void swap(int a, int b) {
            Node temp = heap[a];
            heap[a] = heap[b];
            heap[b] = temp;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    // Disjoint Set (Union-Find) Implementation
    static class DisjointSet {
        int[] parent;
        int[] rank;

        DisjointSet(int size){
            parent = new int[size];
            rank = new int[size];
            for(int i=0;i<size;i++){
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int x){
            if(parent[x] != x){
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        void union(int x, int y){
            int xRoot = find(x);
            int yRoot = find(y);
            if(xRoot == yRoot){
                return;
            }
            // Union by rank
            if(rank[xRoot] < rank[yRoot]){
                parent[xRoot] = yRoot;
            }
            else if(rank[xRoot] > rank[yRoot]){
                parent[yRoot] = xRoot;
            }
            else{
                parent[yRoot] = xRoot;
                rank[xRoot]++;
            }
        }
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
