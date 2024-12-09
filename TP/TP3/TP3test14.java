import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;

public class TP3test14 {
    private static InputReader in;
    private static PrintWriter out;

    static int V;
    static int E;
    static List<Edge>[] adjList;

    static int currentCity = 1;
    static int currentPassword = 0; // Menggunakan integer untuk menyimpan password, awalnya 0000

    // Cache untuk menyimpan hasil precompute
    static Map<Integer, int[]> shortestPathCache = new HashMap<>();
    static Map<Integer, int[]> minimaxPathCache = new HashMap<>();
    static Map<Integer, int[]> sortedMinMaxCache = new HashMap<>();

    // Cache untuk MST dengan ID tertentu
    static Map<Integer, Integer> mstWithIDCache = new HashMap<>();

    // Sistem angka yang digunakan untuk membentuk password
    static List<Integer> systemNumbers = new ArrayList<>();

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

        // Membaca P dan angka sistem untuk password
        int P = in.nextInt();
        for (int i = 0; i < P; i++) {
            int num = in.nextInt();
            num = padNumber(num);
            systemNumbers.add(num);
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

                // Precompute jika diperlukan
                precompute(currentCity);

                // Ambil sorted min max edge weights
                int[] sortedMinMax = sortedMinMaxCache.get(currentCity);

                // Lakukan binary search untuk menemukan jumlah kota dengan minMax <= energy
                int count = upperBound(sortedMinMax, energy);

                // Kurangi 1 karena currentCity selalu memiliki minMax = 0
                count = count > 0 ? count - 1 : 0;

                // Output hasil
                if (count == 0) {
                    out.println(-1);
                } else {
                    out.println(count);
                }

            } else if (command == 'F') {
                int targetCity = Integer.parseInt(parts[1]);

                // Precompute jika diperlukan
                precompute(currentCity);

                // Ambil jarak terpendek ke targetCity
                int[] distances = shortestPathCache.get(currentCity);
                if (distances[targetCity] == Integer.MAX_VALUE) {
                    out.println(-1);
                } else {
                    out.println(distances[targetCity]);
                }

            } else if (command == 'M') {
                if (parts.length < 3) {
                    // Tidak ada ID atau PASSWORD, abaikan perintah
                    out.println(-1);
                    continue;
                }
                int id = Integer.parseInt(parts[1]);
                String passwordStr = parts[2];
                int targetPassword = parsePassword(passwordStr);

                // Implementasi perintah M
                // 1. Mengubah currentCity
                currentCity = id;

                // 2. Menghitung jumlah minimum kombinasi untuk mencapai targetPassword
                int steps = bfsMinimumSteps(currentPassword, targetPassword);

                // 3. Output hasil
                out.println(steps);

                // 4. Memperbarui currentPassword jika berhasil
                if (steps != -1) {
                    currentPassword = targetPassword;
                }

            } else if (command == 'J') {
                if (parts.length < 2) {
                    // Tidak ada ID, abaikan perintah
                    out.println(-1);
                    continue;
                }
                int id = Integer.parseInt(parts[1]);

                // Cek apakah hasil MST untuk ID ini sudah ada di cache
                if (!mstWithIDCache.containsKey(id)) {
                    int totalMST = computeMSTWithID(id);
                    mstWithIDCache.put(id, totalMST);
                }

                // Ambil dari cache dan cetak
                out.println(mstWithIDCache.get(id));
            }
        }

        out.close();
    }

    // Helper method untuk mem-padded angka menjadi 4-digit
    static int padNumber(int num) {
        if (num >= 0 && num <= 9999) {
            return num;
        }
        // Jika angka lebih dari 4-digit, ambil 4 digit terakhir
        return num % 10000;
    }

    // Method untuk mengonversi string password menjadi integer 4-digit
    static int parsePassword(String passwordStr) {
        // Pastikan passwordStr adalah 4 karakter dengan padding nol di depan jika perlu
        while (passwordStr.length() < 4) {
            passwordStr = "0" + passwordStr;
        }
        if (passwordStr.length() > 4) {
            passwordStr = passwordStr.substring(passwordStr.length() - 4);
        }
        return Integer.parseInt(passwordStr);
    }

    // Method untuk melakukan precompute untuk currentCity
    static void precompute(int source) {
        if (shortestPathCache.containsKey(source) && minimaxPathCache.containsKey(source)
                && sortedMinMaxCache.containsKey(source)) {
            return; // Sudah di-precompute
        }

        // Hitung jarak terpendek menggunakan Dijkstra
        int[] distances = dijkstra(source);
        shortestPathCache.put(source, distances);

        // Hitung minimax path menggunakan Dijkstra yang dimodifikasi
        int[] minMax = dijkstraMinMax(source);
        minimaxPathCache.put(source, minMax);

        // Salin dan urutkan minimax untuk binary search
        int[] sortedMinMax = minMax.clone();
        Arrays.sort(sortedMinMax);
        sortedMinMaxCache.put(source, sortedMinMax);
    }

    // Algoritma Dijkstra standar untuk menghitung jarak terpendek
    static int[] dijkstra(int source) {
        int[] dist = new int[V + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        MinHeap heap = new MinHeap(V); // Kapasitas awal dapat diubah
        heap.insert(new Node(source, 0));

        boolean[] visited = new boolean[V + 1];

        while (!heap.isEmpty()) {
            Node current = heap.extractMin();
            int u = current.id;

            if (visited[u])
                continue;
            visited[u] = true;

            for (Edge edge : adjList[u]) {
                int v = edge.to;
                int weight = edge.weight;

                if (!visited[v] && dist[v] > dist[u] + weight) {
                    dist[v] = dist[u] + weight;
                    heap.insert(new Node(v, dist[v]));
                }
            }
        }

        return dist;
    }

    // Algoritma Dijkstra yang dimodifikasi untuk menghitung minimax path
    static int[] dijkstraMinMax(int source) {
        int[] minMax = new int[V + 1];
        Arrays.fill(minMax, Integer.MAX_VALUE);
        minMax[source] = 0;

        MinHeap heap = new MinHeap(V); // Kapasitas awal dapat diubah
        heap.insert(new Node(source, 0));

        boolean[] visited = new boolean[V + 1];

        while (!heap.isEmpty()) {
            Node current = heap.extractMin();
            int u = current.id;

            if (visited[u])
                continue;
            visited[u] = true;

            for (Edge edge : adjList[u]) {
                int v = edge.to;
                int weight = edge.weight;

                int newMinMax = Math.max(minMax[u], weight);
                if (!visited[v] && minMax[v] > newMinMax) {
                    minMax[v] = newMinMax;
                    heap.insert(new Node(v, minMax[v]));
                }
            }
        }

        return minMax;
    }

    // Binary search untuk menemukan indeks terbesar dengan nilai <= target
    static int upperBound(int[] sortedArray, int target) {
        int left = 0;
        int right = sortedArray.length; // Exclusive

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (mid >= sortedArray.length) {
                break;
            }
            if (sortedArray[mid] <= target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    // Kelas Edge
    static class Edge {
        int to;
        int weight;

        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    // Kelas EdgeFull untuk MST
    static class EdgeFull {
        int from;
        int to;
        int weight;

        EdgeFull(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    // Metode untuk menemukan akar dari himpunan dengan path compression
    static int find(int[] parent, int u) {
        if (parent[u] != u) {
            parent[u] = find(parent, parent[u]);
        }
        return parent[u];
    }

    static int computeMSTWithID(int ID) {
        // Kumpulkan semua edge
        List<EdgeFull> allEdges = new ArrayList<>();
        for (int u = 1; u <= V; u++) {
            for (Edge edge : adjList[u]) {
                if (u < edge.to) {
                    allEdges.add(new EdgeFull(u, edge.to, edge.weight));
                }
            }
        }
    
        // Pisahkan edge yang terhubung ke ID
        List<EdgeFull> idEdges = new ArrayList<>();
        List<EdgeFull> otherEdges = new ArrayList<>();
    
        for (EdgeFull e : allEdges) {
            if (e.from == ID || e.to == ID) {
                idEdges.add(e);
            } else {
                otherEdges.add(e);
            }
        }
    
        // Urutkan sesuai bobot
        idEdges.sort(Comparator.comparingInt(e -> e.weight));
        otherEdges.sort(Comparator.comparingInt(e -> e.weight));
    
        // Gabungkan dengan prioritas idEdges
        List<EdgeFull> combined = new ArrayList<>(idEdges);
        combined.addAll(otherEdges);
    
        // Union-Find
        int[] parent = new int[V + 1];
        for (int i = 1; i <= V; i++) {
            parent[i] = i;
        }
    
        int total = 0;
        int edgesUsed = 0;
    
        // Kruskal
        for (EdgeFull e : combined) {
            int pu = find(parent, e.from);
            int pv = find(parent, e.to);
    
            if (pu != pv) {
                parent[pu] = pv;
                total += e.weight;
                edgesUsed++;
                if (edgesUsed == V - 1) {
                    break;
                }
            }
        }
    
        return total;
    }    
    
    // Method BFS untuk menghitung jumlah minimum langkah dari start ke targetPassword
    static int bfsMinimumSteps(int start, int targetPassword) {
        // Jika targetPassword sama dengan start, tidak perlu langkah
        if (start == targetPassword) {
            return 0;
        }

        boolean[] visited = new boolean[10000];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        visited[start] = true;
        int steps = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            steps++;
            for (int i = 0; i < size; i++) {
                int current = queue.poll();
                for (int systemNum : systemNumbers) {
                    int next = digitWiseAdd(current, systemNum);
                    if (next == targetPassword) {
                        return steps;
                    }
                    if (!visited[next]) {
                        visited[next] = true;
                        queue.add(next);
                    }
                }
            }
        }

        // Jika tidak ditemukan
        return -1;
    }

    // Helper method untuk melakukan penjumlahan digit-wise modulo 10
    static int digitWiseAdd(int a, int b) {
        int a_d0 = (a / 1000) % 10;
        int a_d1 = (a / 100) % 10;
        int a_d2 = (a / 10) % 10;
        int a_d3 = a % 10;

        int b_d0 = (b / 1000) % 10;
        int b_d1 = (b / 100) % 10;
        int b_d2 = (b / 10) % 10;
        int b_d3 = b % 10;

        int new_d0 = (a_d0 + b_d0) % 10;
        int new_d1 = (a_d1 + b_d1) % 10;
        int new_d2 = (a_d2 + b_d2) % 10;
        int new_d3 = (a_d3 + b_d3) % 10;

        return new_d0 * 1000 + new_d1 * 100 + new_d2 * 10 + new_d3;
    }

    // Kelas Node untuk heap
    static class Node {
        int id;
        int dist;

        Node(int id, int dist) {
            this.id = id;
            this.dist = dist;
        }
    }

    // Implementasi MinHeap kustom dengan dukungan resizing dinamis
    static class MinHeap {
        private Node[] heap;
        private int size;
        private int capacity;

        MinHeap(int initialCapacity) {
            this.capacity = initialCapacity;
            this.size = 0;
            heap = new Node[capacity + 1]; // 1-based indexing
        }

        void insert(Node node) {
            if (size >= capacity) {
                resize();
            }
            heap[++size] = node;
            siftUp(size);
        }

        Node extractMin() {
            if (size == 0) {
                throw new RuntimeException("Heap is empty");
            }
            Node min = heap[1];
            heap[1] = heap[size--];
            siftDown(1);
            return min;
        }

        boolean isEmpty() {
            return size == 0;
        }

        private void siftUp(int idx) {
            while (idx > 1 && heap[idx].dist < heap[idx / 2].dist) {
                swap(idx, idx / 2);
                idx = idx / 2;
            }
        }

        private void siftDown(int idx) {
            while (2 * idx <= size) {
                int j = 2 * idx;
                if (j < size && heap[j + 1].dist < heap[j].dist) {
                    j++;
                }
                if (heap[idx].dist <= heap[j].dist) {
                    break;
                }
                swap(idx, j);
                idx = j;
            }
        }

        private void swap(int i, int j) {
            Node temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
        }

        // Metode untuk memperbesar kapasitas heap saat penuh
        private void resize() {
            int newCapacity = capacity * 2;
            Node[] newHeap = new Node[newCapacity + 1];
            System.arraycopy(heap, 1, newHeap, 1, size);
            heap = newHeap;
            capacity = newCapacity;
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
                    if (line == null)
                        return null;
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
