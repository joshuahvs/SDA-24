import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP3test6 {
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
            if (vi < 1 || vi > V || vj < 1 || vj > V) {
                // Skip invalid edges
                continue;
            }
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
        // in.nextLine(); // Hapus baris ini untuk memastikan semua perintah dibaca

        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                q--; // Jika baris kosong, jangan hitung sebagai iterasi
                continue;
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0) {
                q--; // Jika tidak ada perintah, jangan hitung sebagai iterasi
                continue;
            }

            char command = parts[0].charAt(0);

            if (command == 'R') {
                if (parts.length < 2) {
                    out.println(-1);
                    continue;
                }
                int energy;
                try {
                    energy = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    out.println(-1);
                    continue;
                }
                boolean[] visited = new boolean[V + 1];
                visited[currentCity] = true;

                // Gunakan Queue yang efisien
                Queue<int[]> queueR = new LinkedList<>();
                queueR.add(new int[]{currentCity, energy});
                int reachableCities = 0;

                while (!queueR.isEmpty()) {
                    int[] current = queueR.poll();
                    int city = current[0];
                    int remainingEnergy = current[1];

                    for (Edge edge : adjList[city]) {
                        int nextCity = edge.to;
                        int distance = edge.weight;

                        if (distance <= remainingEnergy && !visited[nextCity]) {
                            visited[nextCity] = true;
                            reachableCities++;
                            // Tambahkan kota berikutnya dengan energi penuh karena recharge
                            queueR.add(new int[]{nextCity, energy});
                        }
                    }
                }
                // Output hasil
                if (reachableCities == 0) {
                    out.println(-1);
                } else {
                    out.println(reachableCities);
                }

            } else if (command == 'F') {
                if (parts.length < 2) {
                    out.println(-1);
                    continue;
                }
                int targetCity;
                try {
                    targetCity = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    out.println(-1);
                    continue;
                }
                if (targetCity < 1 || targetCity > V) {
                    out.println(-1);
                    continue;
                }

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
                if (dist[targetCity] == Integer.MAX_VALUE) {
                    out.println(-1);
                } else {
                    out.println(dist[targetCity]);
                }

            } else if (command == 'M') {
                if (parts.length < 3) {
                    out.println(-1);
                    continue;
                }
                int targetCity;
                String password = parts[2];
                try {
                    targetCity = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    out.println(-1);
                    continue;
                }
                if (targetCity < 1 || targetCity > V || password.length() != 4 || !password.matches("\\d{4}")) {
                    out.println(-1);
                    continue;
                }

                int passwordInt;
                try {
                    passwordInt = Integer.parseInt(password);
                } catch (NumberFormatException e) {
                    out.println(-1);
                    continue;
                }

                int result;
                if (passwordDistances.containsKey(currentPassword)) {
                    int[] dist = passwordDistances.get(currentPassword);
                    if (passwordInt >= 0 && passwordInt < dist.length) {
                        result = dist[passwordInt];
                    } else {
                        result = -1;
                    }
                } else {
                    int[] dist = bfsPassword(currentPassword, PiNumbers);
                    passwordDistances.put(currentPassword, dist);
                    passwordIndex.put(currentPassword, passwordCounter++);
                    if (passwordInt >= 0 && passwordInt < dist.length) {
                        result = dist[passwordInt];
                    } else {
                        result = -1;
                    }
                }
                if (result != -1) {
                    currentPassword = password;
                }
                currentCity = targetCity;
                out.println(result);

            } else if (command == 'J') {
                if (parts.length < 2) {
                    out.println(-1);
                    continue;
                }
                int startCity;
                try {
                    startCity = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    out.println(-1);
                    continue;
                }
                if (startCity < 1 || startCity > V) {
                    out.println(-1);
                    continue;
                }

                long totalDistance = 0;

                // Inisialisasi Disjoint Set
                DisjointSet ds = new DisjointSet(V + 1);

                // Kumpulkan semua jalan
                List<Road> allRoads = new ArrayList<>();

                // Pertama, kumpulkan semua jalan yang terhubung langsung ke startCity
                List<Road> connectedRoads = new ArrayList<>();
                for (Edge edge : adjList[startCity]) {
                    int to = edge.to;
                    int weight = edge.weight;
                    connectedRoads.add(new Road(startCity, to, weight));
                }

                // Tambahkan jalan yang terhubung ke MST terlebih dahulu
                for (Road road : connectedRoads) {
                    if (ds.find(road.from) != ds.find(road.to)) {
                        ds.union(road.from, road.to);
                        totalDistance += road.weight;
                    }
                }

                // Sekarang, kumpulkan semua jalan lainnya (hindari duplikasi)
                for (int i = 1; i <= V; i++) {
                    for (Edge edge : adjList[i]) {
                        int j = edge.to;
                        int w = edge.weight;
                        if (i < j) { // Untuk menghindari jalan duplikat
                            if (i != startCity && j != startCity) {
                                allRoads.add(new Road(i, j, w));
                            }
                        }
                    }
                }

                // Urutkan allRoads berdasarkan berat
                Collections.sort(allRoads, Comparator.comparingInt(r -> r.weight));

                // Tambahkan jalan ke MST
                for (Road road : allRoads) {
                    if (ds.find(road.from) != ds.find(road.to)) {
                        ds.union(road.from, road.to);
                        totalDistance += road.weight;
                    }
                }

                // Periksa apakah semua kota terhubung
                // Hitung parent unik
                int uniqueSets = 0;
                for (int i = 1; i <= V; i++) {
                    if (ds.find(i) == i) {
                        uniqueSets++;
                    }
                }

                if (uniqueSets == 1) {
                    out.println(totalDistance);
                } else {
                    out.println(-1); // Indikasi bahwa tidak semua kota terhubung
                }
            }
        }

        out.close();
    }

    static int[] bfsPassword(String start, int[] PiNumbers) {
        int[] dist = new int[10000];
        Arrays.fill(dist, -1);
        Queue<String> queue = new LinkedList<>();
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

    static String addPasswords(String a, String b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int digitSum = (a.charAt(i) - '0') + (b.charAt(i) - '0');
            sb.append(digitSum % 10);
        }
        return sb.toString();
    }

    static class Edge implements Comparable<Edge> {
        int to;
        int weight;

        // Constructor used for adjacency list
        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge o) {
            return Integer.compare(this.weight, o.weight);
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
            if (size + 1 >= heap.length) {
                // Resize the heap array jika diperlukan
                heap = Arrays.copyOf(heap, heap.length * 2);
            }
            heap[++size] = node;
            int pos = size;
            while (pos > 1 && heap[pos].dist < heap[pos / 2].dist) {
                swap(pos, pos / 2);
                pos = pos / 2;
            }
        }

        Node extractMin() {
            if (size == 0) {
                throw new NoSuchElementException("Heap is empty");
            }
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

    // Fast Input Reader
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
                    String line = reader.readLine();
                    if (line == null) {
                        return null;
                    }
                    tokenizer = new StringTokenizer(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            String token = next();
            if (token == null) {
                throw new NoSuchElementException("No more tokens available");
            }
            return Integer.parseInt(token);
        }
    }
}
