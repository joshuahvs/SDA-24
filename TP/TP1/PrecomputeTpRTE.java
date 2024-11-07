import java.io.*;
import java.util.*;

public class PrecomputeTpRTE {
    private static InputReader in;
    private static PrintWriter out;
    static long idCustomer = 0;

    // Precomputed DP arrays
    static long[][] dp;
    static List<Integer>[][] pickedItems;

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

        final long hargaIkan[] = new long[N];
        final long hargaSuvenir[] = new long[M];
        final long kebahagiaanSuvenir[] = new long[M];

        for (int i = 0; i < N; i++) {
            hargaIkan[i] = in.nextInteger();
        }

        for (int i = 0; i < M; i++) {
            hargaSuvenir[i] = in.nextInteger();
        }
        for (int i = 0; i < M; i++) {
            kebahagiaanSuvenir[i] = in.nextInteger();
        }

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
        //Precompute the DP for souvenir happiness for all budgets

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
            } else if (command == 'O') {
                int tipeQuery = Integer.parseInt(parts[1]);
                int budget = Integer.parseInt(parts[2]);

                int maxBudget = (int) hargaIkan[hargaIkan.length-1];  // Set a reasonable max budget (you can adjust this if needed)
                if (dp == null || pickedItems == null){
                    precomputeSouvenirDP(hargaSuvenir, kebahagiaanSuvenir, maxBudget);
                } 
                if (budget > maxBudget) {
                    out.println(0);  // If budget exceeds the max precomputed budget
                } else if (tipeQuery == 1) {
                    // Return the maximum happiness from all three states
                    out.println(Math.max(dp[0][budget], Math.max(dp[1][budget], dp[2][budget])));
                } else {
                    // Find the state with the highest happiness and print the items picked
                    long maxHappiness = Math.max(dp[0][budget], Math.max(dp[1][budget], dp[2][budget]));
                    int bestState = (dp[0][budget] == maxHappiness) ? 0 : (dp[1][budget] == maxHappiness ? 1 : 2);
                    out.print(maxHappiness + " ");
                    for (int item : pickedItems[bestState][budget]) {
                        out.print((item + 1) + " ");  // Output 1-based index
                    }
                    out.println();
                }
            } else if (command == 'B') {
                if (customerQueue.isEmpty()) {
                    out.println(-1);
                } else {
                    long output = -1;
                    Customer customerUntukDilayani = customerQueue.poll();
                    if (customerUntukDilayani.patience == 0) {
                        customerQueue.remove(customerUntukDilayani);
                        idToCustomer.remove(customerUntukDilayani.id);
                        customerUntukDilayani = customerQueue.poll();
                    }
                    long hargaIkanYangDapatDibeli = findFishPrice(hargaIkan, customerUntukDilayani.budget);
                    if (customerUntukDilayani.budget == hargaIkanYangDapatDibeli) {
                        if (!kuponStack.isEmpty()) {
                            long discount = kuponStack.pop();
                            long hargaUntukDibayar = Math.max(1, hargaIkanYangDapatDibeli - discount);
                            long uangKembalian = customerUntukDilayani.membayar(hargaUntukDibayar);
                            output = uangKembalian;
                            customerUntukDilayani.setPatience(customerUntukDilayani.originalPatience);
                            customerQueue.add(customerUntukDilayani);
                        } else {
                            long uangKembalian = customerUntukDilayani.membayar(hargaIkanYangDapatDibeli);
                            output = uangKembalian;
                            customerUntukDilayani.setPatience(customerUntukDilayani.originalPatience);
                            customerQueue.add(customerUntukDilayani);
                        }
                    } else if (hargaIkanYangDapatDibeli < 0) {
                        output = customerUntukDilayani.id;
                        customerQueue.remove(customerUntukDilayani);
                        idToCustomer.remove(customerUntukDilayani.id);
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
            }
        }

        out.close();
    }

    static long findFishPrice(long[] fishPrices, long budget) {
        long left = 0;
        long right = fishPrices.length - 1;
        long bestPrice = -1;

        // Manual binary search to find the most expensive fish that fits within the budget
        while (left <= right) {
            long mid = left + (right - left) / 2;  // To avoid potential overflow

            if (fishPrices[(int) mid] <= budget) {
                bestPrice = fishPrices[(int) mid];  // This fish can be afforded, update bestPrice
                left = mid + 1;  // Try to find a more expensive fish within budget
            } else {
                right = mid - 1;  // Mid fish is too expensive, move to cheaper fish
            }
        }

        return bestPrice;  // If no fish is affordable, bestPrice will remain -1
    }

    static long findClosestFishPrice(long[] fishPrices, long budget) {
        long left = 0;
        long right = fishPrices.length - 1;

        // Manual binary search to find the closest fish price
        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (fishPrices[(int) mid] == budget) {
                return fishPrices[(int) mid];  // Exact match found
            } else if (fishPrices[(int) mid] < budget) {
                left = mid + 1;  // Move right to look for a closer or exact match
            } else {
                right = mid - 1;  // Move left to look for a closer or exact match
            }
        }

        // After the binary search, left points to the first element greater than budget,
        // and right points to the last element less than budget.

        // Handle edge cases
        if (right < 0) {
            return fishPrices[(int) left];  // Budget is smaller than the smallest fish price
        }
        if (left >= fishPrices.length) {
            return fishPrices[(int) right];  // Budget is larger than the largest fish price
        }

        // Compare the differences to find the closest price
        long leftDiff = Math.abs(fishPrices[(int) right] - budget);
        long rightDiff = Math.abs(fishPrices[(int) left] - budget);

        // Return the fish price with the smaller difference
        if (leftDiff <= rightDiff) {
            return fishPrices[(int) right];
        } else {
            return fishPrices[(int) left];
        }
    }

    static void precomputeSouvenirDP(long[] costs, long[] values, int maxBudget) {
        int numSouvenirs = costs.length;
        dp = new long[3][maxBudget + 1];
        pickedItems = new List[3][maxBudget + 1];

        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                pickedItems[state][budget] = new ArrayList<>();
            }
        }

        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];

            for (int budget = maxBudget; budget >= 0; budget--) {
                if (budget + cost <= maxBudget) {
                    if (dp[1][(int) (budget + cost)] < dp[0][budget] + value) {
                        dp[1][(int) (budget + cost)] = dp[0][budget] + value;
                        pickedItems[1][(int) (budget + cost)] = new ArrayList<>(pickedItems[0][budget]);
                        pickedItems[1][(int) (budget + cost)].add(i);
                    }
                }

                if (budget + cost <= maxBudget) {
                    if (dp[2][(int) (budget + cost)] < dp[1][budget] + value) {
                        dp[2][(int) (budget + cost)] = dp[1][budget] + value;
                        pickedItems[2][(int) (budget + cost)] = new ArrayList<>(pickedItems[1][budget]);
                        pickedItems[2][(int) (budget + cost)].add(i);
                    }
                }

                if (dp[0][budget] < dp[2][budget]) {
                    dp[0][budget] = dp[2][budget];
                    pickedItems[0][budget] = new ArrayList<>(pickedItems[2][budget]);
                }

                if (dp[0][budget] < dp[1][budget]) {
                    dp[0][budget] = dp[1][budget];
                    pickedItems[0][budget] = new ArrayList<>(pickedItems[1][budget]);
                }
            }
        }
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
