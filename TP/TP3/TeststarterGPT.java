import java.util.*;

public class TeststarterGPT {
    static int V, E;
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
        Scanner sc = new Scanner(System.in);
        V = sc.nextInt();
        E = sc.nextInt();
        adjList = new ArrayList[V + 1];
        for (int i = 1; i <= V; i++) {
            adjList[i] = new ArrayList<>();
        }
        for (int i = 0; i < E; i++) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            int w = sc.nextInt();
            adjList[u].add(new Edge(v, w));
            adjList[v].add(new Edge(u, w));
        }

        int P = sc.nextInt();
        int[] PiNumbers = new int[P];
        for (int i = 0; i < P; i++) {
            PiNumbers[i] = sc.nextInt();
        }

        int Q = sc.nextInt();
        sc.nextLine();
        for (int i = 0; i < Q; i++) {
            String[] parts = sc.nextLine().split(" ");
            String cmd = parts[0];
            if (cmd.equals("R")) {
                int energy = Integer.parseInt(parts[1]);
                int count = commandR(energy);
                System.out.println(count);
            } else if (cmd.equals("F")) {
                int destination = Integer.parseInt(parts[1]);
                int distance = commandF(destination);
                System.out.println(distance);
            } else if (cmd.equals("M")) {
                int id = Integer.parseInt(parts[1]);
                String password = parts[2];
                int result = commandM(id, password, PiNumbers);
                System.out.println(result);
            } else if (cmd.equals("J")) {
                int id = Integer.parseInt(parts[1]);
                int totalWeight = commandJ(id);
                System.out.println(totalWeight);
            }
        }
    }

    static int commandR(int energy) {
        int[] dist = dijkstra(currentCity);
        int count = 0;
        for (int i = 1; i <= V; i++) {
            if (i != currentCity && dist[i] <= energy) {
                count++;
            }
        }
        if (count == 0) {
            return -1;
        }
        return count;
    }

    static int commandF(int destination) {
        int[] dist = dijkstra(currentCity);
        return dist[destination];
    }

    static int commandM(int id, String password, int[] PiNumbers) {
        currentCity = id;
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
        return result;
    }

    static int commandJ(int id) {
        return kruskalWithPreIncludedEdges(id);
    }

    static int[] dijkstra(int src) {
        int[] dist = new int[V + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        boolean[] visited = new boolean[V + 1];
        MinHeap heap = new MinHeap(V * 10);
        heap.insert(new Node(src, 0));
        while (!heap.isEmpty()) {
            Node node = heap.extractMin();
            int u = node.id;
            if (visited[u]) continue;
            visited[u] = true;
            for (Edge edge : adjList[u]) {
                int v = edge.to;
                int w = edge.weight;
                if (!visited[v] && dist[v] > dist[u] + w) {
                    dist[v] = dist[u] + w;
                    heap.insert(new Node(v, dist[v]));
                }
            }
        }
        return dist;
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

    static int kruskalWithPreIncludedEdges(int start) {
        UnionFind uf = new UnionFind(V + 1);
        int totalWeight = 0;
        // Include all edges directly connected to start
        for (Edge edge : adjList[start]) {
            int u = start;
            int v = edge.to;
            if (uf.union(u, v)) {
                totalWeight += edge.weight;
            }
        }
        // Collect all edges
        List<Edge> edges = new ArrayList<>();
        for (int u = 1; u <= V; u++) {
            for (Edge edge : adjList[u]) {
                if (u < edge.to) { // Avoid duplicates
                    edges.add(new Edge(u, edge.to, edge.weight));
                }
            }
        }
        // Sort edges by weight
        Collections.sort(edges);
        // Kruskal's algorithm
        for (Edge edge : edges) {
            int u = edge.from;
            int v = edge.to;
            if (uf.union(u, v)) {
                totalWeight += edge.weight;
            }
        }
        return totalWeight;
    }

    static class Edge implements Comparable<Edge> {
        int from;
        int to;
        int weight;

        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

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

    static class UnionFind {
        int[] parent;

        UnionFind(int n) {
            parent = new int[n];
            for (int i = 1; i < n; i++) {
                parent[i] = i;
            }
        }

        int find(int u) {
            if (parent[u] != u) {
                parent[u] = find(parent[u]);
            }
            return parent[u];
        }

        boolean union(int u, int v) {
            int pu = find(u);
            int pv = find(v);
            if (pu != pv) {
                parent[pu] = pv;
                return true;
            }
            return false;
        }
    }
}
