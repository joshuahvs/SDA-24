import java.io.*;
import java.util.*;

public class TP01GPT {
    static class Customer {
        int id;
        long budget;
        int patience;
        int originalPatience;
        int arriveTime;
        int leaveTime;

        public Customer(int id, long budget, int patience, int arriveTime) {
            this.id = id;
            this.budget = budget;
            this.patience = patience;
            this.originalPatience = patience;
            this.arriveTime = arriveTime;
            this.leaveTime = arriveTime + patience;
        }
    }

    public static void main(String[] args) throws IOException {
        // Use BufferedReader and PrintWriter for fast I/O
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

        // Read N, M, Q
        String[] nmq = br.readLine().split(" ");
        int N = Integer.parseInt(nmq[0]);
        int M = Integer.parseInt(nmq[1]);
        int Q = Integer.parseInt(nmq[2]);

        // Read fish prices
        String[] fishPricesStr = br.readLine().split(" ");
        int[] fishPrices = new int[N];
        for (int i = 0; i < N; i++) {
            fishPrices[i] = Integer.parseInt(fishPricesStr[i]);
        }

        // Read souvenir prices
        String[] souvenirPricesStr = br.readLine().split(" ");
        int[] souvenirPrices = new int[M];
        for (int i = 0; i < M; i++) {
            souvenirPrices[i] = Integer.parseInt(souvenirPricesStr[i]);
        }

        // Read souvenir happiness values
        String[] souvenirValuesStr = br.readLine().split(" ");
        int[] souvenirValues = new int[M];
        for (int i = 0; i < M; i++) {
            souvenirValues[i] = Integer.parseInt(souvenirValuesStr[i]);
        }

        // Initialize data structures
        int currentTime = 0;
        int customerCount = 0;

        // Customers ordered by priority (budget descending, patience ascending, ID ascending)
        PriorityQueue<Customer> customerQueue = new PriorityQueue<>(new Comparator<Customer>() {
            public int compare(Customer c1, Customer c2) {
                if (c1.budget != c2.budget) {
                    return Long.compare(c2.budget, c1.budget);
                }
                if (c1.patience != c2.patience) {
                    return Integer.compare(c1.patience, c2.patience);
                }
                return Integer.compare(c1.id, c2.id);
            }
        });

        // Customers ordered by leave time (patience expiration)
        PriorityQueue<Customer> patienceQueue = new PriorityQueue<>(new Comparator<Customer>() {
            public int compare(Customer c1, Customer c2) {
                return Integer.compare(c1.leaveTime, c2.leaveTime);
            }
        });

        // Map to keep track of customers by ID
        Map<Integer, Customer> idToCustomer = new HashMap<>();

        // Set to keep track of customers who have left
        Set<Integer> leftCustomers = new HashSet<>();

        // Stack for discount coupons
        Stack<Long> discountStack = new Stack<>();

        // Read and process activities
        for (int q = 0; q < Q; q++) {
            currentTime++;

            // Remove customers whose patience has expired
            while (!patienceQueue.isEmpty() && patienceQueue.peek().leaveTime <= currentTime) {
                Customer c = patienceQueue.poll();
                if (!leftCustomers.contains(c.id)) {
                    leftCustomers.add(c.id);
                    // Remove from customer queue (will be handled during polling)
                }
            }

            String line = br.readLine();
            String[] parts = line.split(" ");
            char command = parts[0].charAt(0);

            if (command == 'A') {
                // New customer arrives
                long budget = Long.parseLong(parts[1]);
                int patience = Integer.parseInt(parts[2]);
                Customer customer = new Customer(customerCount, budget, patience, currentTime);
                customerQueue.add(customer);
                patienceQueue.add(customer);
                idToCustomer.put(customerCount, customer);
                out.println(customerCount);
                customerCount++;
            } else if (command == 'S') {
                // Customer asks for minimum price difference
                long hargaDicari = Long.parseLong(parts[1]);
                int idx = Arrays.binarySearch(fishPrices, (int) hargaDicari);
                if (idx >= 0) {
                    // Exact price found
                    out.println(0);
                } else {
                    idx = -idx - 1;
                    long minDiff = Long.MAX_VALUE;
                    if (idx < fishPrices.length) {
                        minDiff = Math.min(minDiff, Math.abs(fishPrices[idx] - hargaDicari));
                    }
                    if (idx - 1 >= 0) {
                        minDiff = Math.min(minDiff, Math.abs(fishPrices[idx - 1] - hargaDicari));
                    }
                    out.println(minDiff);
                }
            } else if (command == 'L') {
                // Customer leaves
                int id = Integer.parseInt(parts[1]);
                Customer customer = idToCustomer.get(id);
                if (customer == null || leftCustomers.contains(id)) {
                    out.println(-1);
                } else {
                    out.println(customer.budget);
                    leftCustomers.add(id);
                    // Remove from patienceQueue and customerQueue handled during polling
                }
            } else if (command == 'D') {
                // Add discount coupon
                long diskon = Long.parseLong(parts[1]);
                discountStack.push(diskon);
                out.println(discountStack.size());
            } else if (command == 'B') {
                // Serve the customer
                // Remove customers who have left from the customerQueue
                while (!customerQueue.isEmpty() && leftCustomers.contains(customerQueue.peek().id)) {
                    customerQueue.poll();
                }
                if (customerQueue.isEmpty()) {
                    out.println(-1);
                } else {
                    Customer customer = customerQueue.poll();
                    // Remove and re-add customer to update position in queue if needed
                    customerQueue.remove(customer);
                    patienceQueue.remove(customer);
                    // Try to find the most expensive fish they can afford
                    int fishPrice = findFishPrice(fishPrices, customer.budget);
                    if (fishPrice == -1) {
                        // Cannot buy any fish
                        out.println(customer.id);
                        leftCustomers.add(customer.id);
                    } else {
                        // Apply discount if paying exact amount
                        long priceToPay = fishPrice;
                        if (customer.budget == fishPrice) {
                            if (!discountStack.isEmpty()) {
                                long discount = discountStack.pop();
                                priceToPay = Math.max(1, fishPrice - discount);
                            }
                        }
                        long change = customer.budget - priceToPay;
                        if (change > 0) {
                            // Add change as a discount coupon
                            discountStack.push(change);
                        }
                        customer.budget -= (customer.budget - change);
                        // Reset patience and leave time
                        customer.patience = customer.originalPatience;
                        customer.leaveTime = currentTime + customer.patience;
                        // Re-add to queues if customer still has money
                        if (customer.budget >= 1) {
                            customerQueue.add(customer);
                            patienceQueue.add(customer);
                        } else {
                            leftCustomers.add(customer.id);
                        }
                        out.println(change);
                    }
                }
            } else if (command == 'O') {
                // Process 'O' queries
                int tipeQuery = Integer.parseInt(parts[1]);
                int X = Integer.parseInt(parts[2]);
                if (tipeQuery == 1) {
                    // Output maximum happiness value
                    int maxHappiness = solveSouvenirDP(souvenirPrices, souvenirValues, X);
                    out.println(maxHappiness);
                } else if (tipeQuery == 2) {
                    // Output maximum happiness and souvenir indices
                    int maxHappiness = solveSouvenirDPWithItems(souvenirPrices, souvenirValues, X, out);
                    // The method writes the output directly
                }
            }
        }

        System.out.println("-------");
        for(Customer c : customerQueue){
            System.out.println(c.id + " " + c.budget + " " + c.patience);
        }
        System.out.println("-------");

        out.flush();
        out.close();
    }

    static int findFishPrice(int[] fishPrices, long budget) {
        // Find the most expensive fish the customer can afford
        int idx = Arrays.binarySearch(fishPrices, (int) Math.min(budget, Integer.MAX_VALUE));
        if (idx >= 0) {
            return fishPrices[idx];
        } else {
            idx = -idx - 1;
            if (idx == 0) {
                return -1; // Cannot afford any fish
            } else {
                return fishPrices[idx - 1];
            }
        }
    }

    static int solveSouvenirDP(int[] H, int[] V, int X) {
        int M = H.length;
        int[][] dp = new int[3][X + 1];
        for (int i = 0; i < M; i++) {
            int h = H[i];
            int v = V[i];
            int[][] newDp = new int[3][X + 1];
            for (int state = 0; state < 3; state++) {
                System.arraycopy(dp[state], 0, newDp[state], 0, X + 1);
            }
            for (int c = 0; c <= X; c++) {
                // State 0 -> State 0 (not pick)
                if (dp[0][c] > newDp[0][c]) {
                    newDp[0][c] = dp[0][c];
                }
                // State 0 -> State 1 (pick)
                if (c + h <= X && dp[0][c] + v > newDp[1][c + h]) {
                    newDp[1][c + h] = dp[0][c] + v;
                }
                // State 1 -> State 0 (not pick)
                if (dp[1][c] > newDp[0][c]) {
                    newDp[0][c] = dp[1][c];
                }
                // State 1 -> State 2 (pick)
                if (c + h <= X && dp[1][c] + v > newDp[2][c + h]) {
                    newDp[2][c + h] = dp[1][c] + v;
                }
                // State 2 -> State 0 (not pick)
                if (dp[2][c] > newDp[0][c]) {
                    newDp[0][c] = dp[2][c];
                }
            }
            dp = newDp;
        }
        int maxHappiness = 0;
        for (int state = 0; state < 3; state++) {
            for (int c = 0; c <= X; c++) {
                if (dp[state][c] > maxHappiness) {
                    maxHappiness = dp[state][c];
                }
            }
        }
        return maxHappiness;
    }

    static int solveSouvenirDPWithItems(int[] H, int[] V, int X, PrintWriter out) {
        int M = H.length;
        int[][] dp = new int[M + 1][X + 1];
        int[][][] prev = new int[M + 1][X + 1][2]; // [i][c][0]: previous i, [i][c][1]: state
        for (int i = 0; i <= M; i++) {
            for (int c = 0; c <= X; c++) {
                dp[i][c] = -1;
            }
        }
        dp[0][0] = 0;
        for (int i = 0; i < M; i++) {
            int h = H[i];
            int v = V[i];
            for (int c = 0; c <= X; c++) {
                if (dp[i][c] >= 0) {
                    // State 0 (not pick)
                    if (dp[i][c] > dp[i + 1][c]) {
                        dp[i + 1][c] = dp[i][c];
                        prev[i + 1][c][0] = i;
                        prev[i + 1][c][1] = 0;
                    }
                    // State 1 (pick)
                    if (c + h <= X && dp[i][c] + v > dp[i + 1][c + h]) {
                        dp[i + 1][c + h] = dp[i][c] + v;
                        prev[i + 1][c + h][0] = i;
                        prev[i + 1][c + h][1] = 1;
                    }
                }
            }
        }
        // Find the maximum happiness
        int maxHappiness = 0;
        int maxC = 0;
        for (int c = 0; c <= X; c++) {
            if (dp[M][c] > maxHappiness) {
                maxHappiness = dp[M][c];
                maxC = c;
            }
        }
        // Backtrack to find the items taken
        List<Integer> items = new ArrayList<>();
        int i = M;
        int c = maxC;
        int consecutive = 0;
        while (i > 0) {
            int pi = prev[i][c][0];
            int state = prev[i][c][1];
            if (state == 1) {
                items.add(i - 1);
                c -= H[i - 1];
                consecutive++;
            } else {
                consecutive = 0;
            }
            i = pi;
        }
        Collections.sort(items);
        out.println(maxHappiness);
        for (int idx : items) {
            out.print((idx + 1) + " ");
        }
        out.println();
        return maxHappiness;
    }

    
}
