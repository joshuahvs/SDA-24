import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class FbisaRngga {
    private static InputReader in;
    private static PrintWriter out;

    static int V;
    static int E;
    static List<Edge>[] adjList;

    static int currentCity = 1;
    static String currentPassword = "0000";

    // Cache untuk menyimpan jarak terpendek dari setiap kota (untuk perintah F)
    static Map<Integer, int[]> shortestPathCache = new HashMap<>();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        in = new InputReader(inputStream);
        out = new PrintWriter(outputStream);

        V = in.nextInt();
        E = in.nextInt();

        // Inisialisasi adjacency list
        adjList = new ArrayList[V + 1];
        for (int i = 0; i <= V; i++) {
            adjList[i] = new ArrayList<>();
        }

        // Membaca E baris jalan
        for (int i = 0; i < E; i++) {
            int vi = in.nextInt();
            int vj = in.nextInt();
            int li = in.nextInt();
            adjList[vi].add(new Edge(vj, li));
            adjList[vj].add(new Edge(vi, li)); // Mengasumsikan graf tidak berarah
        }

        // Membaca P dan digit password
        int P = in.nextInt();
        List<Integer> passwordDigits = new ArrayList<>();
        for (int i = 0; i < P; i++) {
            passwordDigits.add(in.nextInt());
        }

        // Membaca Q dan aktivitas
        int Q = in.nextInt();
        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                q--; // Jika kosong, jangan hitung sebagai iterasi
                continue;
            }

            String[] parts = line.split(" ");
            if (parts.length == 0) {
                q--; // Jika tidak ada perintah, jangan hitung sebagai iterasi
                continue;
            }

            char command = parts[0].charAt(0);

            if (command == 'R') {
                int energy = Integer.parseInt(parts[1]);

                // Jalankan BFS dengan filter berat sisi <= energy
                int reachableCities = bfsReachableCities(currentCity, energy);

                // Output hasil
                if (reachableCities == 0) {
                    out.println(-1);
                } else {
                    out.println(reachableCities);
                }

            } else if (command == 'F') {
                int targetCity = Integer.parseInt(parts[1]);

                // Ambil atau hitung jarak terpendek dari currentCity
                int[] distances = getShortestPaths(currentCity);

                // Ambil jarak ke targetCity
                if (distances[targetCity] == Integer.MAX_VALUE) {
                    out.println(-1);
                } else {
                    out.println(distances[targetCity]);
                }

            } else if (command == 'M') {
                int id = Integer.parseInt(parts[1]);
                String password = parts[2];
                // Implementasi perintah M
                // Misalnya, mengubah currentCity dan currentPassword
                currentCity = id;
                currentPassword = password;
                // Tidak perlu menghapus cache karena cache disimpan per city

            } else if (command == 'J') {
                // Implementasi perintah J
                // Detail tidak diberikan, silakan tambahkan sesuai kebutuhan
            }
        }

        out.close();
    }

    // Method untuk menghitung jumlah kota yang dapat dijangkau dengan energi tertentu
    static int bfsReachableCities(int source, int energy) {
        boolean[] visited = new boolean[V + 1];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);
        visited[source] = true;
        int count = 0;

        while (!queue.isEmpty()) {
            int city = queue.poll();
            for (Edge edge : adjList[city]) {
                if (edge.weight <= energy && !visited[edge.to]) {
                    visited[edge.to] = true;
                    count++;
                    queue.offer(edge.to);
                }
            }
        }

        return count;
    }

    // Method untuk mendapatkan jarak terpendek dari source menggunakan Dijkstra
    static int[] getShortestPaths(int source) {
        if (shortestPathCache.containsKey(source)) {
            return shortestPathCache.get(source);
        }

        int[] dist = new int[V + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
        pq.add(new Node(source, 0));

        boolean[] visited = new boolean[V + 1];

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int u = node.id;

            if (visited[u]) continue;
            visited[u] = true;

            for (Edge edge : adjList[u]) {
                int v = edge.to;
                int weight = edge.weight;

                if (!visited[v] && dist[v] > dist[u] + weight) {
                    dist[v] = dist[u] + weight;
                    pq.add(new Node(v, dist[v]));
                }
            }
        }

        // Cache hasil jarak
        shortestPathCache.put(source, dist);
        return dist;
    }

    static class Edge {
        int to;
        int weight;

        Edge(int to, int weight) {
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

    // InputReader tetap sama
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
                    if (line == null) return null;
                    tokenizer = new StringTokenizer(line);
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
