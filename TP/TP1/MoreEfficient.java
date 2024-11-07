import java.io.*;
import java.util.*;

public class MoreEfficient {
    private static InputReader in;
    private static PrintWriter out;
    static long idCustomer = 0;

    public static class Result {
        int maxHappiness;
        List<Integer> itemsPicked;

        public Result(int maxHappiness, List<Integer> itemsPicked) {
            this.maxHappiness = maxHappiness;
            this.itemsPicked = itemsPicked;
        }
    }

    public static class Customer {
        long id;
        long budget;
        int patience;
        int originalPatience;

        public Customer(long id, long budget, int patience) {
            this.id = id;
            this.budget = budget;
            this.patience = patience;
            this.originalPatience = patience;
        }

        public long membayar(long uang) {
            this.budget -= uang;
            return this.budget;
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        // Read inputs
        int N = in.nextInteger();
        int M = in.nextInteger();
        int Q = in.nextInteger();

        long hargaIkan[] = new long[N];
        long hargaSuvenir[] = new long[M];
        long kebahagiaanSuvenir[] = new long[M];

        for (int i = 0; i < N; i++) {
            hargaIkan[i] = in.nextInteger();
        }
        Arrays.sort(hargaIkan);

        for (int i = 0; i < M; i++) {
            hargaSuvenir[i] = in.nextInteger();
        }
        for (int i = 0; i < M; i++) {
            kebahagiaanSuvenir[i] = in.nextInteger();
        }

        PriorityQueue<Customer> customerQueue = new PriorityQueue<>(Comparator
                .comparingLong((Customer c) -> c.budget).reversed()
                .thenComparingInt(c -> c.patience)
                .thenComparingLong(c -> c.id));

        Stack<Long> kuponStack = new Stack<>();
        Map<Long, Customer> idToCustomer = new HashMap<>();

        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(" ");
            char command = parts[0].charAt(0);

            if (command == 'A') {
                long budget = Long.parseLong(parts[1]);
                int patience = Integer.parseInt(parts[2]);
                Customer customer = new Customer(idCustomer, budget, patience);
                idToCustomer.put(idCustomer, customer);
                idCustomer++;
                customerQueue.add(customer);
                out.println(customer.id);
            } else if (command == 'B') {
                if (customerQueue.isEmpty()) {
                    out.println(-1);
                } else {
                    serveCustomer(hargaIkan, kuponStack, customerQueue, idToCustomer);
                }
            } else if (command == 'S') {
                long budget = Long.parseLong(parts[1]);
                out.println(Math.abs(budget - findClosestFishPrice(hargaIkan, budget)));
            } else if (command == 'L') {
                long id = Long.parseLong(parts[1]);
                Customer customer = idToCustomer.get(id);
                if (customer == null) {
                    out.println(-1);
                } else {
                    out.println(customer.budget);
                    customerQueue.remove(customer);
                    idToCustomer.remove(id);
                }
            } else if (command == 'D') {
                long kupon = Long.parseLong(parts[1]);
                kuponStack.push(kupon);
                out.println(kuponStack.size());
            } else if (command == 'O') {
                int tipeQuery = Integer.parseInt(parts[1]);
                int budget = Integer.parseInt(parts[2]);
                if (tipeQuery == 1) {
                    out.println(solveSouvenirDP(hargaSuvenir, kebahagiaanSuvenir, budget));
                } else {
                    Result result = solveSouvenirDPWithItems(hargaSuvenir, kebahagiaanSuvenir, budget);
                    out.print(result.maxHappiness + " ");
                    for (int items : result.itemsPicked) {
                        out.print(items + 1 + " ");
                    }
                    out.println();
                }
            }
        }

        out.close();
    }

    static void serveCustomer(long[] hargaIkan, Stack<Long> kuponStack, PriorityQueue<Customer> customerQueue, Map<Long, Customer> idToCustomer) {
        Customer customer = customerQueue.poll();
        if (customer.patience == 0) {
            idToCustomer.remove(customer.id);
        } else {
            long fishPrice = findFishPrice(hargaIkan, customer.budget);
            if (fishPrice == customer.budget && !kuponStack.isEmpty()) {
                long discount = kuponStack.pop();
                long finalPrice = Math.max(1, fishPrice - discount);
                long change = customer.membayar(finalPrice);
                customer.patience = customer.originalPatience;
                customerQueue.add(customer);
                out.println(change);
            } else {
                long change = customer.membayar(fishPrice);
                kuponStack.push(change);
                customer.patience = customer.originalPatience;
                customerQueue.add(customer);
                out.println(change);
            }
        }
    }

    static long findFishPrice(long[] fishPrices, long budget) {
        int left = 0, right = fishPrices.length - 1;
        long bestPrice = -1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (fishPrices[mid] <= budget) {
                bestPrice = fishPrices[mid];
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return bestPrice;
    }

    static long findClosestFishPrice(long[] fishPrices, long budget) {
        int left = 0, right = fishPrices.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (fishPrices[mid] == budget) {
                return fishPrices[mid];
            } else if (fishPrices[mid] < budget) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        if (right < 0) return fishPrices[left];
        if (left >= fishPrices.length) return fishPrices[right];
        return (Math.abs(fishPrices[right] - budget) <= Math.abs(fishPrices[left] - budget)) ? fishPrices[right] : fishPrices[left];
    }

    static int solveSouvenirDP(long[] costs, long[] values, int maxBudget) {
        int numSouvenirs = costs.length;
        int[][] dp = new int[3][maxBudget + 1];

        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];

            for (int budget = maxBudget; budget >= cost; budget--) {
                dp[1][(int) (budget)] = Math.max(dp[1][(int) (budget)], dp[0][(int) (budget - cost)] + (int) value);
                dp[2][(int) (budget)] = Math.max(dp[2][(int) (budget)], dp[1][(int) (budget - cost)] + (int) value);
            }

            for (int budget = 0; budget <= maxBudget; budget++) {
                dp[0][budget] = Math.max(dp[0][budget], dp[1][budget]);
                dp[0][budget] = Math.max(dp[0][budget], dp[2][budget]);
            }
        }

        int maxHappiness = 0;
        for (int[] dpState : dp) {
            for (int value : dpState) {
                maxHappiness = Math.max(maxHappiness, value);
            }
        }
        return maxHappiness;
    }

    static Result solveSouvenirDPWithItems(long[] costs, long[] values, int maxBudget) {
        int numSouvenirs = costs.length;
        int[][] dp = new int[3][maxBudget + 1];
        List<Integer>[][] pickedItems = new List[3][maxBudget + 1];

        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                pickedItems[state][budget] = new ArrayList<>();
            }
        }

        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];

            for (int budget = maxBudget; budget >= cost; budget--) {
                if (dp[1][(int) budget] < dp[0][(int) (budget - cost)] + value) {
                    dp[1][(int) budget] = (int) (dp[0][(int) (budget - cost)] + value);
                    pickedItems[1][(int) budget] = new ArrayList<>(pickedItems[0][(int) (budget - cost)]);
                    pickedItems[1][(int) budget].add(i);
                }

                if (dp[2][(int) budget] < dp[1][(int) (budget - cost)] + value) {
                    dp[2][(int) budget] = (int) (dp[1][(int) (budget - cost)] + value);
                    pickedItems[2][(int) budget] = new ArrayList<>(pickedItems[1][(int) (budget - cost)]);
                    pickedItems[2][(int) budget].add(i);
                }
            }

            for (int budget = 0; budget <= maxBudget; budget++) {
                dp[0][budget] = Math.max(dp[0][budget], dp[1][budget]);
                dp[0][budget] = Math.max(dp[0][budget], dp[2][budget]);
                pickedItems[0][budget] = dp[0][budget] == dp[1][budget] ? new ArrayList<>(pickedItems[1][budget]) : pickedItems[0][budget];
                pickedItems[0][budget] = dp[0][budget] == dp[2][budget] ? new ArrayList<>(pickedItems[2][budget]) : pickedItems[0][budget];
            }
        }

        int maxHappiness = 0;
        List<Integer> bestPickedItems = new ArrayList<>();
        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                if (dp[state][budget] > maxHappiness) {
                    maxHappiness = dp[state][budget];
                    bestPickedItems = new ArrayList<>(pickedItems[state][budget]);
                }
            }
        }

        return new Result(maxHappiness, bestPickedItems);
    }

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

        public int nextInteger() {
            return Integer.parseInt(next());
        }
    }
}
