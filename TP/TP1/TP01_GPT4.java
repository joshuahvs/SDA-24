import java.io.*;
import java.util.*;

public class FishShop {
    static class Customer implements Comparable<Customer> {
        int id;
        long budget;
        int patience;
        int arrivalTime;
        int leaveTime;
        int initialPatience;

        public Customer(int id, long budget, int patience, int arrivalTime, int leaveTime) {
            this.id = id;
            this.budget = budget;
            this.patience = patience;
            this.initialPatience = patience;
            this.arrivalTime = arrivalTime;
            this.leaveTime = leaveTime;
        }

        @Override
        public int compareTo(Customer other) {
            if (this.budget != other.budget) {
                return Long.compare(other.budget, this.budget); // higher budget first
            }
            if (this.patience != other.patience) {
                return Integer.compare(this.patience, other.patience); // lower patience first
            }
            return Integer.compare(this.id, other.id); // lower ID first
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Customer) {
                Customer other = (Customer) obj;
                return this.id == other.id;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    static int currentTime = 0;
    static int nextCustomerID = 0;

    static TreeSet<Customer> customerQueue = new TreeSet<>();
    static HashMap<Integer, Customer> idToCustomer = new HashMap<>();
    static TreeMap<Integer, List<Customer>> leaveEvents = new TreeMap<>();
    static Stack<Long> discountStack = new Stack<>();

    static int N, M, Q;
    static long[] fishPrices;
    static int[] souvenirPrices;
    static int[] souvenirValues;

    static List<QueryO> queryOList = new ArrayList<>();
    static int maxX = 0;

    static class QueryO {
        int type;
        int X;
        int index;

        public QueryO(int type, int X, int index) {
            this.type = type;
            this.X = X;
            this.index = index;
        }
    }

    static String[] queryOOutputs;

    public static void main(String[] args) throws IOException {
        FastReader sc = new FastReader();

        N = sc.nextInt();
        M = sc.nextInt();
        Q = sc.nextInt();

        fishPrices = new long[N];
        for (int i = 0; i < N; i++) {
            fishPrices[i] = sc.nextLong();
        }
        // Since fish prices are guaranteed to be sorted from largest to smallest
        // But the problem seems to have conflicting info, so we sort them just in case
        Arrays.sort(fishPrices);

        souvenirPrices = new int[M + 1]; // 1-based indexing
        souvenirValues = new int[M + 1];

        for (int i = 1; i <= M; i++) {
            souvenirPrices[i] = sc.nextInt();
        }
        for (int i = 1; i <= M; i++) {
            souvenirValues[i] = sc.nextInt();
        }

        StringBuilder output = new StringBuilder();
        List<String> outputs = new ArrayList<>();

        for (int q = 0; q < Q; q++) {
            currentTime++;

            // Remove customers who are supposed to leave at currentTime
            if (leaveEvents.containsKey(currentTime)) {
                List<Customer> leavingCustomers = leaveEvents.get(currentTime);
                for (Customer cust : leavingCustomers) {
                    customerQueue.remove(cust);
                    idToCustomer.remove(cust.id);
                }
                leaveEvents.remove(currentTime);
            }

            String line = sc.nextLine();
            String[] tokens = line.split(" ");
            String command = tokens[0];

            switch (command) {
                case "A":
                    // A [BUDGET] [PATIENCE]
                    long budget = Long.parseLong(tokens[1]);
                    int patience = Integer.parseInt(tokens[2]);
                    int id = nextCustomerID++;
                    int leaveTime = currentTime + patience;
                    Customer customer = new Customer(id, budget, patience, currentTime, leaveTime);
                    customerQueue.add(customer);
                    idToCustomer.put(id, customer);

                    leaveEvents.computeIfAbsent(leaveTime, k -> new ArrayList<>()).add(customer);

                    output.append(id).append("\n");
                    break;
                case "S":
                    // S [PRICE_TO_SEARCH]
                    long priceToSearch = Long.parseLong(tokens[1]);
                    long minDiff = findMinPriceDifference(priceToSearch);
                    output.append(minDiff).append("\n");
                    break;
                case "L":
                    // L [ID]
                    int customerID = Integer.parseInt(tokens[1]);
                    if (idToCustomer.containsKey(customerID)) {
                        Customer cust = idToCustomer.get(customerID);
                        output.append(cust.budget).append("\n");
                        customerQueue.remove(cust);
                        idToCustomer.remove(customerID);
                        int custLeaveTime = cust.leaveTime;
                        if (leaveEvents.containsKey(custLeaveTime)) {
                            leaveEvents.get(custLeaveTime).remove(cust);
                            if (leaveEvents.get(custLeaveTime).isEmpty()) {
                                leaveEvents.remove(custLeaveTime);
                            }
                        }
                    } else {
                        output.append("-1\n");
                    }
                    break;
                case "D":
                    // D [DISCOUNT]
                    long discount = Long.parseLong(tokens[1]);
                    discountStack.push(discount);
                    output.append(discountStack.size()).append("\n");
                    break;
                case "B":
                    // B
                    if (customerQueue.isEmpty()) {
                        output.append("-1\n");
                    } else {
                        // Remove customers who are supposed to leave at currentTime
                        if (leaveEvents.containsKey(currentTime)) {
                            List<Customer> leavingCustomers = leaveEvents.get(currentTime);
                            for (Customer cust : leavingCustomers) {
                                customerQueue.remove(cust);
                                idToCustomer.remove(cust.id);
                            }
                            leaveEvents.remove(currentTime);
                        }

                        if (customerQueue.isEmpty()) {
                            output.append("-1\n");
                            break;
                        }

                        Customer cust = customerQueue.pollFirst();
                        // Find the most expensive fish the customer can afford
                        long maxAffordableFishPrice = findMaxAffordableFishPrice(cust.budget);

                        if (maxAffordableFishPrice == -1) {
                            // Customer cannot afford any fish
                            output.append(cust.id).append("\n");
                            idToCustomer.remove(cust.id);
                            int custLeaveTime = cust.leaveTime;
                            if (leaveEvents.containsKey(custLeaveTime)) {
                                leaveEvents.get(custLeaveTime).remove(cust);
                                if (leaveEvents.get(custLeaveTime).isEmpty()) {
                                    leaveEvents.remove(custLeaveTime);
                                }
                            }
                        } else {
                            long change = 0;
                            long priceToPay = maxAffordableFishPrice;
                            if (cust.budget == priceToPay) {
                                if (!discountStack.isEmpty()) {
                                    long discountValue = discountStack.pop();
                                    long discountedPrice = Math.max(1, priceToPay - discountValue);
                                    change = cust.budget - discountedPrice;
                                    cust.budget = change;
                                } else {
                                    cust.budget -= priceToPay;
                                }
                            } else {
                                change = cust.budget - priceToPay;
                                cust.budget -= priceToPay;
                                // Add change as discount coupon
                                discountStack.push(change);
                            }
                            // Reset patience and leave time
                            cust.patience = cust.initialPatience;
                            cust.leaveTime = currentTime + cust.patience;
                            // Update leaveEvents
                            leaveEvents.computeIfAbsent(cust.leaveTime, k -> new ArrayList<>()).add(cust);
                            // Re-insert customer if they have remaining budget
                            if (cust.budget > 0) {
                                customerQueue.add(cust);
                            } else {
                                idToCustomer.remove(cust.id);
                            }
                            output.append(change).append("\n");
                        }
                    }
                    break;
                case "O":
                    // O [QUERY_TYPE] [X]
                    int queryType = Integer.parseInt(tokens[1]);
                    int X = Integer.parseInt(tokens[2]);
                    maxX = Math.max(maxX, X);
                    queryOList.add(new QueryO(queryType, X, outputs.size()));
                    outputs.add(""); // Placeholder
                    break;
                default:
                    // Ignore invalid commands
                    break;
            }
        }

        // Process O queries
        if (!queryOList.isEmpty()) {
            queryOOutputs = new String[outputs.size()];
            processOQueries();
            for (int i = 0; i < outputs.size(); i++) {
                if (outputs.get(i).isEmpty()) {
                    output.append(queryOOutputs[i]);
                } else {
                    output.append(outputs.get(i));
                }
            }
        } else {
            System.out.print(output.toString());
            return;
        }

        // Print the output
        System.out.print(output.toString());
    }

    // Function to find the minimal difference between a price and the fish prices
    static long findMinPriceDifference(long price) {
        int idx = Arrays.binarySearch(fishPrices, price);
        if (idx >= 0) {
            return 0;
        } else {
            idx = -idx - 1;
            long minDiff = Long.MAX_VALUE;
            if (idx < fishPrices.length) {
                minDiff = Math.min(minDiff, Math.abs(fishPrices[idx] - price));
            }
            if (idx > 0) {
                minDiff = Math.min(minDiff, Math.abs(fishPrices[idx - 1] - price));
            }
            return minDiff;
        }
    }

    // Function to find the most expensive fish the customer can afford
    static long findMaxAffordableFishPrice(long budget) {
        int idx = Arrays.binarySearch(fishPrices, budget);
        if (idx >= 0) {
            return fishPrices[idx];
        } else {
            idx = -idx - 2;
            if (idx >= 0) {
                return fishPrices[idx];
            } else {
                return -1;
            }
        }
    }

    // Function to process O queries
    static void processOQueries() {
        // Group queries by X to avoid redundant DP computations
        Map<Integer, List<QueryO>> queriesByX = new HashMap<>();
        for (QueryO query : queryOList) {
            queriesByX.computeIfAbsent(query.X, k -> new ArrayList<>()).add(query);
        }

        for (Map.Entry<Integer, List<QueryO>> entry : queriesByX.entrySet()) {
            int X = entry.getKey();
            List<QueryO> queries = entry.getValue();

            // Run DP for this X
            processSingleOQuery(X, queries);
        }
    }

    static void processSingleOQuery(int X, List<QueryO> queries) {
        int maxBudget = X;

        // DP arrays
        int[][] dp = new int[3][maxBudget + 1]; // dp[s][k] = max happiness
        Arrays.fill(dp[0], Integer.MIN_VALUE);
        Arrays.fill(dp[1], Integer.MIN_VALUE);
        Arrays.fill(dp[2], Integer.MIN_VALUE);
        dp[0][0] = 0;

        // For reconstruction
        DPState[][] state = new DPState[3][maxBudget + 1];

        for (int i = 1; i <= M; i++) {
            int price = souvenirPrices[i];
            int value = souvenirValues[i];

            for (int s = 2; s >= 0; s--) {
                for (int k = 0; k <= maxBudget; k++) {
                    if (dp[s][k] != Integer.MIN_VALUE) {
                        // Skip the item
                        if (dp[0][k] < dp[s][k]) {
                            dp[0][k] = dp[s][k];
                            state[0][k] = new DPState(s, k, -1, dp[s][k]);
                        } else if (dp[0][k] == dp[s][k]) {
                            // Compare sequences
                            if (compareSequences(state[0][k], new DPState(s, k, -1, dp[s][k])) > 0) {
                                state[0][k] = new DPState(s, k, -1, dp[s][k]);
                            }
                        }

                        // Take the item
                        if (s < 2 && k + price <= maxBudget) {
                            int newS = s + 1;
                            int newK = k + price;
                            int newHappiness = dp[s][k] + value;
                            if (dp[newS][newK] < newHappiness) {
                                dp[newS][newK] = newHappiness;
                                state[newS][newK] = new DPState(s, k, i, newHappiness);
                            } else if (dp[newS][newK] == newHappiness) {
                                // Compare sequences
                                if (compareSequences(state[newS][newK], new DPState(s, k, i, newHappiness)) > 0) {
                                    state[newS][newK] = new DPState(s, k, i, newHappiness);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Find the max happiness
        int maxHappiness = Integer.MIN_VALUE;
        int finalS = -1, finalK = -1;
        for (int s = 0; s <= 2; s++) {
            for (int k = 0; k <= maxBudget; k++) {
                if (dp[s][k] != Integer.MIN_VALUE) {
                    if (dp[s][k] > maxHappiness) {
                        maxHappiness = dp[s][k];
                        finalS = s;
                        finalK = k;
                    } else if (dp[s][k] == maxHappiness) {
                        // Compare sequences
                        if (compareSequences(state[finalS][finalK], state[s][k]) > 0) {
                            finalS = s;
                            finalK = k;
                        }
                    }
                }
            }
        }

        // Reconstruct the sequence
        List<Integer> sequence = new ArrayList<>();
        if (state[finalS][finalK] != null) {
            DPState currentState = state[finalS][finalK];
            while (currentState != null && currentState.prevS != -1) {
                if (currentState.itemIndex != -1) {
                    sequence.add(currentState.itemIndex);
                }
                int tempS = currentState.prevS;
                int tempK = currentState.prevK;
                currentState = state[tempS][tempK];
            }
            Collections.sort(sequence); // Since we need indices in ascending order
        }

        // Output the results for the queries with this X
        for (QueryO query : queries) {
            if (query.type == 1) {
                queryOOutputs[query.index] = maxHappiness + "\n";
            } else if (query.type == 2) {
                StringBuilder sb = new StringBuilder();
                sb.append(maxHappiness);
                for (int idx : sequence) {
                    sb.append(" ").append(idx);
                }
                sb.append("\n");
                queryOOutputs[query.index] = sb.toString();
            }
        }
    }

    static class DPState {
        int prevS;
        int prevK;
        int itemIndex; // -1 if no item was taken at this state
        int happiness;

        public DPState(int prevS, int prevK, int itemIndex, int happiness) {
            this.prevS = prevS;
            this.prevK = prevK;
            this.itemIndex = itemIndex;
            this.happiness = happiness;
        }
    }

    // Compare sequences represented by DPState a and DPState b
    // Return -1 if a is lex smaller than b, 1 if a is lex larger than b, 0 if equal
    static int compareSequences(DPState a, DPState b) {
        List<Integer> seqA = reconstructSequence(a);
        List<Integer> seqB = reconstructSequence(b);

        int len = Math.min(seqA.size(), seqB.size());
        for (int i = 0; i < len; i++) {
            if (!seqA.get(i).equals(seqB.get(i))) {
                return seqA.get(i) - seqB.get(i);
            }
        }
        return seqA.size() - seqB.size();
    }

    static Map<DPState, List<Integer>> sequenceCache = new HashMap<>();

    static List<Integer> reconstructSequence(DPState state) {
        if (sequenceCache.containsKey(state)) {
            return sequenceCache.get(state);
        }
        List<Integer> sequence = new ArrayList<>();
        DPState currentState = state;
        while (currentState != null && currentState.prevS != -1) {
            if (currentState.itemIndex != -1) {
                sequence.add(currentState.itemIndex);
            }
            int tempS = currentState.prevS;
            int tempK = currentState.prevK;
            currentState = currentState.prevS != -1 ? state[tempS][tempK] : null;
        }
        Collections.reverse(sequence);
        sequenceCache.put(state, sequence);
        return sequence;
    }

    // FastReader class for faster input
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() throws IOException {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String nextLine() throws IOException {
            String str = br.readLine();
            while (str != null && str.trim().isEmpty()) {
                str = br.readLine();
            }
            return str == null ? "" : str;
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    String s = br.readLine();
                    if (s == null) return null;
                    st = new StringTokenizer(s);
                } catch (IOException e) {
                    return null;
                }
            }
            return st.nextToken();
        }

        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }

        long nextLong() throws IOException {
            return Long.parseLong(next());
        }
    }
}
