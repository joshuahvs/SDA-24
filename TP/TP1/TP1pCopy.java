import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.StringTokenizer;

public class TP1pCopy {
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
            } else if (command == 'O') {
                int tipeQuery = Integer.parseInt(parts[1]);
                int budget = Integer.parseInt(parts[2]);
                if (tipeQuery == 1){
                    int maxHappiness = solveSouvenirDP(hargaSuvenir, kebahagiaanSuvenir, budget);
                    out.println(maxHappiness);
                } else {
                    solveSouvenirDPWithItems(hargaSuvenir, kebahagiaanSuvenir, budget);
                }
            }
        }

        out.close();
    }

    static long findFishPrice(long[] fishPrices, long budget) {
        int left = 0;
        int right = fishPrices.length - 1;
        long bestPrice = -1;

        // Manual binary search to find the most expensive fish that fits within the budget
        while (left <= right) {
            int mid = left + (right - left) / 2;  // To avoid potential overflow

            if (fishPrices[mid] <= budget) {
                bestPrice = fishPrices[mid];  // This fish can be afforded, update bestPrice
                left = mid + 1;  // Try to find a more expensive fish within budget
            } else {
                right = mid - 1;  // Mid fish is too expensive, move to cheaper fish
            }
        }

        return bestPrice;  // If no fish is affordable, bestPrice will remain -1
    }

    static long findClosestFishPrice(long[] fishPrices, long budget) {
        int left = 0;
        int right = fishPrices.length - 1;

        // Manual binary search to find the closest fish price
        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (fishPrices[mid] == budget) {
                return fishPrices[mid];  // Exact match found
            } else if (fishPrices[mid] < budget) {
                left = mid + 1;  // Move right to look for a closer or exact match
            } else {
                right = mid - 1;  // Move left to look for a closer or exact match
            }
        }

        // After the binary search, left points to the first element greater than budget,
        // and right points to the last element less than budget.

        // Handle edge cases
        if (right < 0) {
            return fishPrices[left];  // Budget is smaller than the smallest fish price
        }
        if (left >= fishPrices.length) {
            return fishPrices[right];  // Budget is larger than the largest fish price
        }

        // Compare the differences to find the closest price
        long leftDiff = Math.abs(fishPrices[right] - budget);
        long rightDiff = Math.abs(fishPrices[left] - budget);

        // Return the fish price with the smaller difference
        if (leftDiff <= rightDiff) {
            return fishPrices[right];
        } else {
            return fishPrices[left];
        }
    }

    static int solveSouvenirDP(long[] costs, long[] values, int maxBudget) {
        int numSouvenirs = costs.length;
        // dp[state][budget] tracks the max happiness with `budget` left and in `state`
        int[][] dp = new int[3][maxBudget + 1];
        
        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];
            int[][] newDp = new int[3][maxBudget + 1];  // Temporary DP array to hold updates
    
            // Copy the current state of dp to the newDp array
            for (int state = 0; state < 3; state++) {
                System.arraycopy(dp[state], 0, newDp[state], 0, maxBudget + 1);
            }
    
            // Try updating the DP table based on possible transitions between states
            for (int budget = 0; budget <= maxBudget; budget++) {
                // State 0: Not picking any souvenir, either continue or start picking
                newDp[0][budget] = Math.max(newDp[0][budget], dp[0][budget]);  // Continue not picking
                if (budget + cost <= maxBudget) {  // Pick one and move to state 1
                    newDp[1][(int) (budget + cost)] = (int) Math.max(newDp[1][(int) (budget + cost)], dp[0][budget] + value);
                }
                
                // State 1: Picked one souvenir, either stop picking or pick another
                newDp[0][budget] = Math.max(newDp[0][budget], dp[1][budget]);  // Stop picking, move to state 0
                if (budget + cost <= maxBudget) {  // Pick another and move to state 2
                    newDp[2][(int) (budget + cost)] = (int) Math.max(newDp[2][(int) (budget + cost)], dp[1][budget] + value);
                }
    
                // State 2: Picked two consecutive souvenirs, can only stop picking now
                newDp[0][budget] = Math.max(newDp[0][budget], dp[2][budget]);  // Stop picking, reset to state 0
            }
            dp = newDp;  // Move the updates back to the original dp array
        }
    
        // Find the maximum happiness achievable within the budget
        int maxHappiness = 0;
        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                maxHappiness = Math.max(maxHappiness, dp[state][budget]);
            }
        }
    
        return maxHappiness;
    }    

    static void solveSouvenirDPWithItems(long[] costs, long[] values, int maxBudget) {
        int numSouvenirs = costs.length;
        int[][] dp = new int[3][maxBudget + 1];
        int[][] itemIndices = new int[3][maxBudget + 1];
    
        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];
            int[][] newDp = new int[3][maxBudget + 1];  // Temporary DP array to hold updates
            int[][] newItemIndices = new int[3][maxBudget + 1];  // Temporary array to hold item indices
    
            // Copy the current state of dp to the newDp array
            for (int state = 0; state < 3; state++) {
                System.arraycopy(dp[state], 0, newDp[state], 0, maxBudget + 1);
                System.arraycopy(itemIndices[state], 0, newItemIndices[state], 0, maxBudget + 1);
            }
    
            // Try updating the DP table based on possible transitions between states
            for (int budget = 0; budget <= maxBudget; budget++) {
                // State 0: Not picking any souvenir, either continue or start picking
                newDp[0][budget] = Math.max(newDp[0][budget], dp[0][budget]);  // Continue not picking
                if (budget + (int) cost <= maxBudget) {  // Pick one and move to state 1
                    if (dp[0][budget] + value > newDp[1][(int) (budget + cost)]) {
                        newDp[1][(int) (budget + cost)] = (int) (dp[0][budget] + value);
                        newItemIndices[1][(int) (budget + cost)] = i;
                    }
                }
    
                // State 1: Picked one souvenir, either stop picking or pick another
                newDp[0][budget] = Math.max(newDp[0][budget], dp[1][budget]);  // Stop picking, move to state 0
                if (budget + (int) cost <= maxBudget) {  // Pick another and move to state 2
                    if (dp[1][budget] + value > newDp[2][(int) (budget + cost)]) {
                        newDp[2][(int) (budget + cost)] = (int) (dp[1][budget] + value);
                        newItemIndices[2][(int) (budget + cost)] = i;
                    }
                }
    
                // State 2: Picked two consecutive souvenirs, can only stop picking now
                newDp[0][budget] = Math.max(newDp[0][budget], dp[2][budget]);  // Stop picking, reset to state 0
            }
            dp = newDp;  // Move the updates back to the original dp array
            itemIndices = newItemIndices;  // Move the updates back to the original itemIndices array
        }
    
        // Find the maximum happiness achievable within the budget
        int maxHappiness = 0;
        int maxHappinessIndex = -1;
        int maxHappinessState = 0; // Initialize to a valid state
        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                if (dp[state][budget] > maxHappiness) {
                    maxHappiness = dp[state][budget];
                    maxHappinessIndex = itemIndices[state][budget];
                    maxHappinessState = state; 
                }
            }
        }
    
        // Backtrack to find the indices of the items that lead to the maximum happiness
        List<Integer> pickedItems = new ArrayList<>();
        int currentBudget = maxBudget;
        int currentState = maxHappinessState; // Start from the state with max happiness
    
        while (currentState > 0 && currentBudget > 0) { 
            int pickedItemIndex = itemIndices[currentState][currentBudget];
            if (pickedItemIndex >= 0) { // Valid item index
                pickedItems.add(pickedItemIndex);
                currentBudget -= (int) costs[pickedItemIndex]; 
            }
            currentState--; // Move to the previous state
        }
    
        // Print the maximum happiness and the indices of the picked items
        out.print(maxHappiness + " ");
        for (int i = pickedItems.size() - 1; i >= 0; i--) {
            out.print(pickedItems.get(i) + " ");
        }
        out.println(); 
    }
    

    // static void solveSouvenirDPWithItems(long[] costs, long[] values, int maxBudget) {
    //     int numSouvenirs = costs.length;
    //     int[][] dp = new int[3][maxBudget + 1];
    //     int[][] itemIndices = new int[3][maxBudget + 1];
    
    //     for (int i = 0; i < numSouvenirs; i++) {
    //         long cost = costs[i];
    //         long value = values[i];
    //         int[][] newDp = new int[3][maxBudget + 1];  // Temporary DP array to hold updates
    //         int[][] newItemIndices = new int[3][maxBudget + 1];  // Temporary array to hold item indices
    
    //         // Copy the current state of dp to the newDp array
    //         for (int state = 0; state < 3; state++) {
    //             System.arraycopy(dp[state], 0, newDp[state], 0, maxBudget + 1);
    //             System.arraycopy(itemIndices[state], 0, newItemIndices[state], 0, maxBudget + 1);
    //         }
    
    //         // Try updating the DP table based on possible transitions between states
    //         for (int budget = 0; budget <= maxBudget; budget++) {
    //             // State 0: Not picking any souvenir, either continue or start picking
    //             newDp[0][budget] = Math.max(newDp[0][budget], dp[0][budget]);  // Continue not picking
    //             if (budget + (int) cost <= maxBudget) {  // Pick one and move to state 1
    //                 if (dp[0][budget] + value > newDp[1][(int) (budget + cost)]) {
    //                     newDp[1][(int) (budget + cost)] = (int) (dp[0][budget] + value);
    //                     newItemIndices[1][(int) (budget + cost)] = i;
    //                 }
    //             }
    
    //             // State 1: Picked one souvenir, either stop picking or pick another
    //             newDp[0][budget] = Math.max(newDp[0][budget], dp[1][budget]);  // Stop picking, move to state 0
    //             if (budget + (int) cost <= maxBudget) {  // Pick another and move to state 2
    //                 if (dp[1][budget] + value > newDp[2][(int) (budget + cost)]) {
    //                     newDp[2][(int) (budget + cost)] = (int) (dp[1][budget] + value);
    //                     newItemIndices[2][(int) (budget + cost)] = i;
    //                 }
    //             }
    
    //             // State 2: Picked two consecutive souvenirs, can only stop picking now
    //             newDp[0][budget] = Math.max(newDp[0][budget], dp[2][budget]);  // Stop picking, reset to state 0
    //         }
    //         dp = newDp;  // Move the updates back to the original dp array
    //         itemIndices = newItemIndices;  // Move the updates back to the original itemIndices array
    //     }
    
    //     // Find the maximum happiness achievable within the budget
    //     int maxHappiness = 0;
    //     int maxHappinessIndex = -1;
    //     for (int state = 0; state < 3; state++) {
    //         for (int budget = 0; budget <= maxBudget; budget++) {
    //             if (dp[state][budget] > maxHappiness) {
    //                 maxHappiness = dp[state][budget];
    //                 maxHappinessIndex = itemIndices[state][budget];
    //             }
    //         }
    //     }
    
    //     out.println(maxHappiness + " " + maxHappinessIndex);
    // }


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
