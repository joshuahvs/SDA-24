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

public class TP1p {
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
        int[][][] souvenirIndices = new int[3][maxBudget + 1][numSouvenirs + 1]; // Stores indices of souvenirs selected
        int[][] souvenirLengths = new int[3][maxBudget + 1]; // Stores the length of each souvenir list

        for (int i = 0; i < numSouvenirs; i++) {
            long cost = costs[i];
            long value = values[i];
            int[][] newDp = new int[3][maxBudget + 1];
            int[][][] newIndices = new int[3][maxBudget + 1][numSouvenirs + 1];
            int[][] newLengths = new int[3][maxBudget + 1];

            // Copy the previous state
            for (int state = 0; state < 3; state++) {
                for (int budget = 0; budget <= maxBudget; budget++) {
                    newDp[state][budget] = dp[state][budget];
                    newLengths[state][budget] = souvenirLengths[state][budget];
                    System.arraycopy(souvenirIndices[state][budget], 0, newIndices[state][budget], 0, souvenirLengths[state][budget]);
                }
            }

            for (int budget = 0; budget <= maxBudget; budget++) {
                if (budget + cost <= maxBudget) {
                    int newHappiness = (int) (dp[0][budget] + value);
                    if (newHappiness > newDp[1][(int) (budget + cost)] ||
                            (newHappiness == newDp[1][(int) (budget + cost)] && isLexicographicallySmaller(
                                    souvenirIndices[0][budget], newIndices[1][(int) (budget + cost)], souvenirLengths[0][budget], newLengths[1][(int) (budget + cost)], i))) {
                        newDp[1][(int) (budget + cost)] = newHappiness;
                        newLengths[1][(int) (budget + cost)] = souvenirLengths[0][budget] + 1;
                        System.arraycopy(souvenirIndices[0][budget], 0, newIndices[1][(int) (budget + cost)], 0, souvenirLengths[0][budget]);
                        newIndices[1][(int) (budget + cost)][souvenirLengths[0][budget]] = i + 1;
                    }
                }

                if (budget + cost <= maxBudget) {
                    int newHappiness = (int) (dp[1][budget] + value);
                    if (newHappiness > newDp[2][(int) (budget + cost)] ||
                            (newHappiness == newDp[2][(int) (budget + cost)] && isLexicographicallySmaller(
                                    souvenirIndices[1][budget], newIndices[2][(int) (budget + cost)], souvenirLengths[1][budget], newLengths[2][(int) (budget + cost)], i))) {
                        newDp[2][(int) (budget + cost)] = newHappiness;
                        newLengths[2][(int) (budget + cost)] = souvenirLengths[1][budget] + 1;
                        System.arraycopy(souvenirIndices[1][budget], 0, newIndices[2][(int) (budget + cost)], 0, souvenirLengths[1][budget]);
                        newIndices[2][(int) (budget + cost)][souvenirLengths[1][budget]] = i + 1;
                    }
                }

                for (int state = 0; state < 3; state++) {
                    if (dp[state][budget] > newDp[0][budget] ||
                            (dp[state][budget] == newDp[0][budget] && isLexicographicallySmaller(
                                    souvenirIndices[state][budget], newIndices[0][budget], souvenirLengths[state][budget], newLengths[0][budget], -1))) {
                        newDp[0][budget] = dp[state][budget];
                        newLengths[0][budget] = souvenirLengths[state][budget];
                        System.arraycopy(souvenirIndices[state][budget], 0, newIndices[0][budget], 0, souvenirLengths[state][budget]);
                    }
                }
            }
            dp = newDp;
            souvenirIndices = newIndices;
            souvenirLengths = newLengths;
        }

        int maxHappiness = 0;
        int[] selectedSouvenirs = new int[numSouvenirs + 1];
        int selectedLength = 0;
        for (int state = 0; state < 3; state++) {
            for (int budget = 0; budget <= maxBudget; budget++) {
                if (dp[state][budget] > maxHappiness ||
                        (dp[state][budget] == maxHappiness && isLexicographicallySmaller(
                                souvenirIndices[state][budget], selectedSouvenirs, souvenirLengths[state][budget], selectedLength, -1))) {
                    maxHappiness = dp[state][budget];
                    selectedLength = souvenirLengths[state][budget];
                    System.arraycopy(souvenirIndices[state][budget], 0, selectedSouvenirs, 0, selectedLength);
                }
            }
        }

        out.print(maxHappiness + " ");
        for (int i = 0; i < selectedLength; i++) {
            out.print(selectedSouvenirs[i] + " ");
        }
        out.println();
    }

    static boolean isLexicographicallySmaller(int[] list1, int[] list2, int length1, int length2, int newItem) {
        for (int i = 0; i < Math.min(length1, length2); i++) {
            if (list1[i] != list2[i]) {
                return list1[i] < list2[i];
            }
        }
        if (length1 != length2) {
            return length1 < length2;
        }
        if (newItem != -1) {
            return true;
        }
        return false;
    }
    
    // static void solveSouvenirDPWithItems(long[] costs, long[] values, int maxBudget) {
    //     int numSouvenirs = costs.length;
    //     // DP array to store maximum happiness for each budget
    //     long[] dp = new long[maxBudget + 1];
    //     // Arrays to track the last souvenir added and the previous budget
    //     int[] parentSouvenir = new int[maxBudget + 1];
    //     int[] parentBudget = new int[maxBudget + 1];
        
    //     // Initialize parent arrays
    //     Arrays.fill(parentSouvenir, -1);
    //     Arrays.fill(parentBudget, -1);
        
    //     for (int i = 0; i < numSouvenirs; i++) {
    //         long cost = costs[i];
    //         long value = values[i];
    //         // Iterate from maxBudget down to cost to avoid using the same item multiple times
    //         for (int budget = maxBudget; budget >= cost; budget--) {
    //             if (dp[budget - (int)cost] + value > dp[budget]) {
    //                 dp[budget] = dp[budget - (int)cost] + value;
    //                 parentSouvenir[budget] = i;
    //                 parentBudget[budget] = budget - (int)cost;
    //             } else if (dp[budget - (int)cost] + value == dp[budget]) {
    //                 // Handle lexicographical order
    //                 // Reconstruct both existing and new selections to compare
    //                 List<Integer> existingSelection = reconstructSelection(parentSouvenir, parentBudget, budget);
    //                 List<Integer> newSelection = reconstructSelection(parentSouvenir, parentBudget, budget - (int)cost);
    //                 newSelection.add(i);
    //                 if (isLexicographicallySmaller(newSelection, existingSelection)) {
    //                     dp[budget] = dp[budget - (int)cost] + value;
    //                     parentSouvenir[budget] = i;
    //                     parentBudget[budget] = budget - (int)cost;
    //                 }
    //             }
    //         }
    //     }

    //     // Find the budget with the maximum happiness
    //     long maxHappiness = 0;
    //     int bestBudget = 0;
    //     List<Integer> bestSelection = new ArrayList<>();
    //     for (int budget = 0; budget <= maxBudget; budget++) {
    //         if (dp[budget] > maxHappiness) {
    //             maxHappiness = dp[budget];
    //             bestBudget = budget;
    //             bestSelection = reconstructSelection(parentSouvenir, parentBudget, budget);
    //         } else if (dp[budget] == maxHappiness) {
    //             List<Integer> currentSelection = reconstructSelection(parentSouvenir, parentBudget, budget);
    //             if (isLexicographicallySmaller(currentSelection, bestSelection)) {
    //                 bestBudget = budget;
    //                 bestSelection = currentSelection;
    //             }
    //         }
    //     }

    //     // Sort the selected souvenirs to ensure lexicographical order
    //     bestSelection.sort(Integer::compareTo);

    //     // Output the results
    //     out.print(maxHappiness + " ");
    //     for (int idx = 0; idx < bestSelection.size(); idx++) {
    //         out.print((bestSelection.get(idx) + 1) + " "); // Assuming souvenirs are 1-indexed
    //     }
    //     out.println();
    // }

    // // Helper method to reconstruct the selection list from parent pointers
    // static List<Integer> reconstructSelection(int[] parentSouvenir, int[] parentBudget, int budget) {
    //     List<Integer> selection = new ArrayList<>();
    //     while (budget != -1 && parentSouvenir[budget] != -1) {
    //         selection.add(parentSouvenir[budget]);
    //         budget = parentBudget[budget];
    //     }
    //     // The selection is reconstructed in reverse order
    //     List<Integer> orderedSelection = new ArrayList<>();
    //     for (int i = selection.size() - 1; i >= 0; i--) {
    //         orderedSelection.add(selection.get(i));
    //     }
    //     return orderedSelection;
    // }

    // // Helper method to compare two selections lexicographically
    // static boolean isLexicographicallySmaller(List<Integer> a, List<Integer> b) {
    //     int len = Math.min(a.size(), b.size());
    //     for (int i = 0; i < len; i++) {
    //         if (a.get(i) < b.get(i)) return true;
    //         if (a.get(i) > b.get(i)) return false;
    //     }
    //     return a.size() < b.size();
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
