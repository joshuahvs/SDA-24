import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class TP3test1{
    private static InputReader in;
    private static PrintWriter out;

    static int V;
    static int E;
    static List<Edge>[] adjList;
    static List<Integer> passwordDigits = new ArrayList<>();

    static int currentCity = 1;
    static String currentPassword = "0000";
    

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
        passwordDigits = new ArrayList<>();
        for (int i = 0; i < P; i++) {
            passwordDigits.add(in.nextInt());
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


                // out.println("BFS Queue:");
                // for (int[] item : queue) {
                //     out.println("City: " + item[0] + ", Remaining Energy: " + item[1]);
                // }
                // out.println("Visited Cities:");
                // for (int i = 1; i <= V; i++) {
                //     if (visited[i]) {
                //         out.println("City " + i + " is visited.");
                //     }
                // }

            } else if (command=='F'){
                int tujuan = Integer.parseInt(parts[1]);
            } else if (command =='M'){
                int id = Integer.parseInt(parts[1]);
                String password = parts[2];

            } else if (command == 'J'){

            }
        }

        // Debugging output (can be removed in the final implementation)
        // out.println();
        // out.println("Adjacency List:");
        // for (int i = 1; i <= V; i++) {
        //     out.print(i + ": ");
        //     for (Edge edge : adjList[i]) {
        //         out.print("(" + edge.to + ", " + edge.weight + ") ");
        //     }
        //     out.println();
        // }
        out.close();
    }


    // static int[] dijkstra(int src) {
    //     int[] dist = new int[V + 1];
    //     Arrays.fill(dist, Integer.MAX_VALUE);
    //     dist[src] = 0;
    //     boolean[] visited = new boolean[V + 1];
    //     MinHeap heap = new MinHeap(V * 10);
    //     heap.insert(new Node(src, 0));
    //     while (!heap.isEmpty()) {
    //         Node node = heap.extractMin();
    //         int u = node.id;
    //         if (visited[u]) continue;
    //         visited[u] = true;
    //         for (Edge edge : adjList[u]) {
    //             int v = edge.to;
    //             int w = edge.weight;
    //             if (!visited[v] && dist[v] > dist[u] + w) {
    //                 dist[v] = dist[u] + w;
    //                 heap.insert(new Node(v, dist[v]));
    //             }
    //         }
    //     }
    //     return dist;
    // }

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