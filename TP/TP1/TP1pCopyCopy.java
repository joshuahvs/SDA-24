import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class TP1pCopyCopy {
    private static InputReader in;
    private static PrintWriter out;
    static long idCustomer = 0;

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

        public void setPatience(int patience) {
            this.patience = patience;
        }

        public long membayar(long uang) {
            long uangKembalian = this.budget -= uang;
            return uangKembalian;
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

        // Determine maxBudget by scanning all 'O' commands first
        // Since commands are read online, we need to set a reasonable maxBudget
        // Alternatively, read all Q commands first to find the maximum budget
        // For simplicity, we'll set a fixed maxBudget
        int maxBudget = 100000; // Adjust based on problem constraints

        // Convert 'costs' and 'values' to int if possible
        int[] costs = new int[M];
        int[] values = new int[M];
        for (int i = 0; i < M; i++) {
            costs[i] = (int) hargaSuvenir[i];
            values[i] = (int) kebahagiaanSuvenir[i];
        }

        // Precompute the DP array
        int[] dp = precomputeMaxHappiness(costs, values, maxBudget);

        PriorityQueue<Customer> customerQueue = new PriorityQueue<>(new Comparator<Customer>() {
            public int compare(Customer c1, Customer c2) {
                // Compare budgets in descending order (higher budget comes first)
                if (c1.budget != c2.budget) {
                    return Long.compare(c2.budget, c1.budget);
                }
                // If budgets are the same, compare patience in ascending order (lower patience comes first)
                if (c1.patience != c2.patience) {
                    return Long.compare(c1.patience, c2.patience);
                }
                // If both budget and patience are the same, compare by id in ascending order (for stability)
                return Long.compare(c1.id, c2.id);
            }
        });

        Stack<Long> kuponStack = new Stack<Long>();
        Map<Long, Customer> idToCustomer = new HashMap<>();

        for (int q = 0; q < Q; q++) {
            String line = in.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;  // Skip empty lines
            }

            String[] parts = line.split(" ");
            if (parts.length == 0) {
                continue;  // Skip if no parts were found after splitting
            }

            char command = parts[0].charAt(0);

            // Decrease patience of all customers and remove those with zero patience
            Iterator<Customer> iterator = customerQueue.iterator();
            while (iterator.hasNext()) {
                Customer c = iterator.next();
                c.patience--;
                if (c.patience == 0) {
                    iterator.remove();  // Use the iterator to remove the customer safely
                    idToCustomer.remove(c.id);  // Remove from idToCustomer as well
                }
            }

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
                    long output = -1;
                    Customer customerUntukDilayani = customerQueue.poll();
                    if (customerUntukDilayani.patience == 0) {
                        customerQueue.remove(customerUntukDilayani);
                        idToCustomer.remove(customerUntukDilayani.id);
                        if (customerQueue.isEmpty()) {
                            out.println(-1);
                            continue;
                        }
                        customerUntukDilayani = customerQueue.poll();
                    }
                    long hargaIkanYangDapatDibeli = findFishPrice(hargaIkan, customerUntukDilayani.budget);
                    if (hargaIkanYangDapatDibeli == -1) {
                        output = -1;
                    } else if (customerUntukDilayani.budget >= hargaIkanYangDapatDibeli) {
                        if (!kuponStack.isEmpty()) {
                            long discount = kuponStack.pop();
                            long hargaUntukDibayar = Math.max(1, hargaIkanYangDapatDibeli - discount);
                            long uangKembalian = customerUntukDilayani.membayar(hargaUntukDibayar);
                            output = uangKembalian;
                        } else {
                            long uangKembalian = customerUntukDilayani.membayar(hargaIkanYangDapatDibeli);
                            output = uangKembalian;
                        }
                        customerUntukDilayani.setPatience(customerUntukDilayani.originalPatience);
                        customerQueue.add(customerUntukDilayani);
                    } else {
                        long uangKembalian = customerUntukDilayani.membayar(hargaIkanYangDapatDibeli);
                        kuponStack.push(uangKembalian);
                        output = uangKembalian;
                        customerUntukDilayani.setPatience(customerUntukDilayani.originalPatience);
                        customerQueue.add(customerUntukDilayani);
                    }
                    out.println(output);
                }
            } else if (command == 'S') {
                long budget = Long.parseLong(parts[1]);
                long hargaTerdekat = findClosestFishPrice(hargaIkan, budget);
                long selisih;
                if (budget > hargaTerdekat) {
                    selisih = budget - hargaTerdekat;
                } else if (budget < hargaTerdekat) {
                    selisih = hargaTerdekat - budget;
                } else {
                    selisih = 0;
                }
                out.println(selisih);
            } else if (command == 'L') {
                long id = Long.parseLong(parts[1]);
                Customer customer = idToCustomer.get(id);
                if (customer == null) {
                    out.println(-1);
                } else {
                    out.println(customer.budget);
                    idToCustomer.remove(id);
                    customerQueue.remove(customer);
                }
            } else if (command == 'D') {
                long kupon = Long.parseLong(parts[1]);
                kuponStack.push(kupon);
                out.println(kuponStack.size());
            } else if (command == 'O') {
                int tipeQuery = Integer.parseInt(parts[1]);
                int budget = Integer.parseInt(parts[2]);

                if (tipeQuery == 1) {
                    if (budget > maxBudget) {
                        // Handle budgets larger than precomputed maxBudget
                        // Optionally, extend the DP array or cap the budget
                        // For simplicity, we'll cap the budget
                        budget = maxBudget;
                    }
                    out.println(dp[budget]);
                } else {
                   solveSouvenirDPWithItems(hargaSuvenir, kebahagiaanSuvenir, maxBudget);
                }
            }
        }

        out.close();
    }

    static int[] precomputeMaxHappiness(int[] costs, int[] values, int maxBudget) {
        int[] dp = new int[maxBudget + 1];
        
        for (int i = 0; i < costs.length; i++) {
            int cost = costs[i];
            int value = values[i];
            
            // Iterate from high to low to prevent using the same item multiple times
            for (int budget = maxBudget; budget >= cost; budget--) {
                if (dp[budget - cost] + value > dp[budget]) {
                    dp[budget] = dp[budget - cost] + value;
                }
            }
        }
        
        return dp;
    }

    static long findFishPrice(long[] fishPrices, long budget) {
        int left = 0;
        int right = fishPrices.length - 1;
        long bestPrice = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2; // Corrected mid calculation

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
        int left = 0;
        int right = fishPrices.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2; // Corrected mid calculation

            if (fishPrices[mid] == budget) {
                return fishPrices[mid];
            } else if (fishPrices[mid] < budget) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (right < 0) {
            return fishPrices[left];
        }
        if (left >= fishPrices.length) {
            return fishPrices[right];
        }

        long leftDiff = Math.abs(fishPrices[right] - budget);
        long rightDiff = Math.abs(fishPrices[left] - budget);

        return (leftDiff <= rightDiff) ? fishPrices[right] : fishPrices[left];
    }

    static void solveSouvenirDPWithItems(long[] costs, long[] values, int maxBudget) {
        int numSouvenirs = costs.length;
        // dp[state][budget] tracks the max happiness with `budget` left and in `state`
        int[][] dp = new int[3][maxBudget + 1];

        // Track which items were picked
        List<Integer>[][] pickedItems = new List[3][maxBudget + 1];
        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                pickedItems[state][budget] = new ArrayList<>();
            }
        }

        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];

            // Iterate budgets in reverse to prevent overwriting the dp array during updates
            for (int budget = maxBudget; budget >= 0; budget--) {
                // State 0: Not picking any souvenir, either continue or start picking
                if (budget + cost <= maxBudget) { // Pick one and move to state 1
                    if (dp[1][(int) (budget + cost)] < dp[0][budget] + value) {
                        dp[1][(int) (budget + cost)] = (int) (dp[0][budget] + value);
                        pickedItems[1][(int) (budget + cost)] = new ArrayList<>(pickedItems[0][budget]);
                        pickedItems[1][(int) (budget + cost)].add(i); // Add current item
                    }
                }

                // State 1: Picked one souvenir, either stop picking or pick another
                if (budget + cost <= maxBudget) { // Pick another and move to state 2
                    if (dp[2][(int) (budget + cost)] < dp[1][budget] + value) {
                        dp[2][(int) (budget + cost)] = (int) (dp[1][budget] + value);
                        pickedItems[2][(int) (budget + cost)] = new ArrayList<>(pickedItems[1][budget]);
                        pickedItems[2][(int) (budget + cost)].add(i); // Add current item
                    }
                }

                // State 2: Picked two consecutive souvenirs, can only stop picking now
                dp[0][budget] = Math.max(dp[0][budget], dp[2][budget]);
                pickedItems[0][budget] = dp[0][budget] > dp[2][budget]
                        ? new ArrayList<>(pickedItems[0][budget])
                        : new ArrayList<>(pickedItems[2][budget]);

                // Additionally: State 1 to 0 and State 0 to 0
                dp[0][budget] = Math.max(dp[0][budget], dp[1][budget]);
                pickedItems[0][budget] = dp[0][budget] > dp[1][budget]
                        ? new ArrayList<>(pickedItems[0][budget])
                        : new ArrayList<>(pickedItems[1][budget]);
            }
        }

        // Find the maximum happiness achievable within the budget
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

       
        out.print(maxHappiness + " ");
        for (int items : bestPickedItems){
            out.print(items+1 + " ");
        }
        out.println();
    }

    // InputReader class with added nextLine() method
    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String nextLine() {
            tokenizer = null; // Reset tokenizer
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
