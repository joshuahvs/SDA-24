import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP3 {
    private static InputReader in;
    private static PrintWriter out;

    static int V;
    static int E;
    static List<Edge>[] adjList;
    static List<Integer> passwordDigits = new ArrayList<>();

    static int currentCity = 1;
    static String currentPassword = "0000";

    // Cache to store results of R queries
    static Map<Integer, Integer> reachableCitiesCache = new HashMap<>();
    static Map<String, Integer> shortestPathCache = new HashMap<>();

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

            if (command == 'R') {
                int energy = Integer.parseInt(parts[1]);

                // Check if result is already cached
                if (reachableCitiesCache.containsKey(energy)) {
                    out.println(reachableCitiesCache.get(energy));
                } else {
                    boolean[] visited = new boolean[V + 1];
                    visited[currentCity] = true;

                    Queue<int[]> queue = new LinkedList<>();
                    queue.offer(new int[]{currentCity, energy});
                    int reachableCities = 0;

                    while (!queue.isEmpty()) {
                        int[] current = queue.poll();
                        int city = current[0];
                        int remainingEnergy = current[1];

                        for (Edge edge : adjList[city]) {
                            int nextCity = edge.to;
                            int distance = edge.weight;

                            if (distance <= remainingEnergy && !visited[nextCity]) {
                                visited[nextCity] = true;
                                reachableCities++;
                                queue.offer(new int[]{nextCity, energy});
                            }
                        }
                    }

                    // Cache the result
                    reachableCitiesCache.put(energy, reachableCities);

                    // Output the result
                    if (reachableCities == 0) {
                        out.println(-1);
                    } else {
                        out.println(reachableCities);
                    }
                }

            } else if (command == 'F') {
                int targetCity = Integer.parseInt(parts[1]);

                // Check if result is already cached
                String cacheKey = currentCity + "-" + targetCity;
                if (shortestPathCache.containsKey(cacheKey)) {
                    out.println(shortestPathCache.get(cacheKey));
                } else {
                    int[] dist = new int[V + 1];
                    Arrays.fill(dist, Integer.MAX_VALUE);
                    dist[currentCity] = 0;

                    boolean[] visitedF = new boolean[V + 1];
                    FibonacciHeap<Integer> heap = new FibonacciHeap<>(); // Use FibonacciHeap
                    Map<Integer, FibonacciHeap.Entry<Integer>> entries = new HashMap<>();

                    for (int i = 1; i <= V; i++) {
                        entries.put(i, heap.enqueue(i, dist[i]));
                    }

                    while (!heap.isEmpty()) {
                        int u = heap.dequeueMin().getValue();
                        if (visitedF[u]) continue;
                        visitedF[u] = true;

                        for (Edge edge : adjList[u]) {
                            int v = edge.to;
                            int w = edge.weight;

                            if (!visitedF[v] && dist[v] > dist[u] + w) {
                                dist[v] = dist[u] + w;
                                heap.decreaseKey(entries.get(v), dist[v]); // Update distance in the heap
                            }
                        }
                    }

                    int result = (dist[targetCity] == Integer.MAX_VALUE) ? -1 : dist[targetCity];

                    // Cache the result
                    shortestPathCache.put(cacheKey, result);

                    out.println(result);
                }


            } else if (command == 'M') {
                int id = Integer.parseInt(parts[1]);
                String password = parts[2];

            } else if (command == 'J') {

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

    static class FibonacciHeap<T> {
        private Entry<T> minNode;
        private int size;

        public FibonacciHeap() {
            minNode = null;
            size = 0;
        }

        public Entry<T> enqueue(T value, int priority) {
            Entry<T> newNode = new Entry<>(value, priority);
            minNode = mergeLists(minNode, newNode);
            size++;
            return newNode;
        }

        public Entry<T> dequeueMin() {
            if (isEmpty()) {
                return null;
            }
            size--;
            Entry<T> min = minNode;
            if (minNode.next == minNode) {
                minNode = null;
            } else {
                minNode.prev.next = minNode.next;
                minNode.next.prev = minNode.prev;
                minNode = minNode.next; // Choose an arbitrary node from the root list
            }
            if (min.child != null) {
                Entry<T> curr = min.child;
                do {
                    curr.parent = null;
                    curr = curr.next;
                } while (curr != min.child);
                minNode = mergeLists(minNode, curr);
            }
            consolidate();
            return min;
        }

        public void decreaseKey(Entry<T> entry, int newPriority) {
            if (newPriority > entry.priority) {
                throw new IllegalArgumentException("New priority cannot be greater than current priority.");
            }
            entry.priority = newPriority;
            if (entry.parent != null && entry.priority < entry.parent.priority) {
                cut(entry);
                cascadingCut(entry.parent);
            }
            if (entry.priority < minNode.priority) {
                minNode = entry;
            }
        }

        public boolean isEmpty() {
            return minNode == null;
        }

        private void consolidate() {
            if (isEmpty()) {
                return;
            }
            // Use an array instead of a List to avoid potential overhead
            Entry<T>[] aux = new Entry[(int) (Math.log(size) / Math.log(2)) + 1]; // Adjust the size as needed
            Entry<T> curr = minNode;
            int maxDegree = 0;
            do {
                int degree = curr.degree;
                while (aux[degree] != null) {
                    Entry<T> other = aux[degree];
                    if (curr.priority > other.priority) {
                        Entry<T> temp = curr;
                        curr = other;
                        other = temp;
                    }
                    link(other, curr);
                    aux[degree] = null;
                    degree++;
                }
                aux[degree] = curr;
                maxDegree = Math.max(maxDegree, degree);
                curr = curr.next;
            } while (curr != minNode);

            minNode = null;
            for (int i = 0; i <= maxDegree; i++) {
                if (aux[i] != null) {
                    if (minNode == null) {
                        minNode = aux[i];
                        minNode.next = minNode;
                        minNode.prev = minNode;
                    } else {
                        minNode = mergeLists(minNode, aux[i]);
                    }
                }
            }
        }


        private void link(Entry<T> y, Entry<T> x) {
            // Removes y from the root list and makes it a child of x.
            y.prev.next = y.next;
            y.next.prev = y.prev;
            y.parent = x;
            if (x.child == null) {
                x.child = y;
                y.next = y;
                y.prev = y;
            } else {
                x.child = mergeLists(x.child, y);
            }
            x.degree++;
            y.marked = false;
        }

        private void cut(Entry<T> x) {
            // Removes x from the child list of its parent and adds it to the root list.
            x.parent.degree--;
            if (x.next == x) {
                x.parent.child = null;
            } else {
                x.prev.next = x.next;
                x.next.prev = x.prev;
                if (x.parent.child == x) {
                    x.parent.child = x.next;
                }
            }
            x.parent = null;
            minNode = mergeLists(minNode, x);
            x.marked = false;
        }

        private void cascadingCut(Entry<T> y) {
            Entry<T> z = y.parent;
            if (z != null) {
                if (!y.marked) {
                    y.marked = true;
                } else {
                    cut(y);
                    cascadingCut(z);
                }
            }
        }

        private Entry<T> mergeLists(Entry<T> a, Entry<T> b) {
            if (a == null && b == null) {
                return null;
            } else if (a == null) {
                return b;
            } else if (b == null) {
                return a;
            } else {
                Entry<T> aNext = a.next;
                a.next = b.next;
                a.next.prev = a;
                b.next = aNext;
                b.next.prev = b;
                return a.priority < b.priority ? a : b;
            }
        }

        public static class Entry<T> {
            private T value;
            private int priority;
            private int degree;
            private boolean marked;
            private Entry<T> parent;
            private Entry<T> child;
            private Entry<T> prev;
            private Entry<T> next;

            public Entry(T value, int priority) {
                this.value = value;
                this.priority = priority;
                this.degree = 0;
                this.marked = false;
                this.parent = null;
                this.child = null;
                this.prev = this;
                this.next = this;
            }

            public T getValue() {
                return value;
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