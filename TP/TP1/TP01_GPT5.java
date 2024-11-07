import java.io.*;
import java.util.*;


public class TP01_GPT5 {
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

    static PriorityQueue customerQueue = new PriorityQueue();
    static Map idToCustomer = new Map();
    static LeaveEvents leaveEvents = new LeaveEvents();
    static Stack discountStack = new Stack();

    static int N, M, Q;
    static long[] fishPrices;
    static int[] souvenirPrices;
    static int[] souvenirValues;

    static QueryO[] queryOList;
    static int queryOIndex = 0;

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

    static FastReader sc;

    public static void main(String[] args) throws IOException {
        sc = new FastReader();

        N = sc.nextInt();
        M = sc.nextInt();
        Q = sc.nextInt();

        fishPrices = new long[N];
        readFishPrices(0, N);

        ArraysSort.sort(fishPrices);

        souvenirPrices = new int[M + 1]; // 1-based indexing
        souvenirValues = new int[M + 1];

        readSouvenirPrices(1, M);
        readSouvenirValues(1, M);

        StringBuilder output = new StringBuilder();

        queryOList = new QueryO[Q];

        processQueries(0, Q, output);

        // Print the output
        System.out.print(output.toString());
    }

    static void readFishPrices(int index, int N) throws IOException {
        if (index >= N) return;
        fishPrices[index] = sc.nextLong();
        readFishPrices(index + 1, N);
    }

    static void readSouvenirPrices(int index, int M) throws IOException {
        if (index > M) return;
        souvenirPrices[index] = sc.nextInt();
        readSouvenirPrices(index + 1, M);
    }

    static void readSouvenirValues(int index, int M) throws IOException {
        if (index > M) return;
        souvenirValues[index] = sc.nextInt();
        readSouvenirValues(index + 1, M);
    }

    static void processQueries(int qIndex, int Q, StringBuilder output) throws IOException {
        if (qIndex >= Q) {
            // Process O queries if any
            if (queryOIndex > 0) {
                processOQueries(output);
            }
            return;
        }

        currentTime++;

        // Remove customers who are supposed to leave at currentTime
        removeLeavingCustomers(currentTime);

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

                leaveEvents.add(leaveTime, customer);

                output.append(id).append("\n");
                break;
            case "S":
                // S [PRICE_TO_SEARCH]
                long priceToSearch = Long.parseLong(tokens[1]);
                long minDiff = findMinPriceDifference(priceToSearch, 0, fishPrices.length - 1);
                output.append(minDiff).append("\n");
                break;
            case "L":
                // L [ID]
                int customerID = Integer.parseInt(tokens[1]);
                Customer cust = idToCustomer.get(customerID);
                if (cust != null) {
                    output.append(cust.budget).append("\n");
                    customerQueue.remove(cust);
                    idToCustomer.remove(customerID);
                    leaveEvents.remove(cust.leaveTime, cust);
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
                    removeLeavingCustomers(currentTime);

                    if (customerQueue.isEmpty()) {
                        output.append("-1\n");
                        break;
                    }

                    Customer c = customerQueue.pollFirst();
                    // Find the most expensive fish the customer can afford
                    long maxAffordableFishPrice = findMaxAffordableFishPrice(c.budget, 0, fishPrices.length - 1);

                    if (maxAffordableFishPrice == -1) {
                        // Customer cannot afford any fish
                        output.append(c.id).append("\n");
                        idToCustomer.remove(c.id);
                        leaveEvents.remove(c.leaveTime, c);
                    } else {
                        long change = 0;
                        long priceToPay = maxAffordableFishPrice;
                        if (c.budget == priceToPay) {
                            if (!discountStack.isEmpty()) {
                                long discountValue = discountStack.pop();
                                long discountedPrice = Math.max(1, priceToPay - discountValue);
                                change = c.budget - discountedPrice;
                                c.budget = change;
                            } else {
                                c.budget -= priceToPay;
                            }
                        } else {
                            change = c.budget - priceToPay;
                            c.budget -= priceToPay;
                            // Add change as discount coupon
                            discountStack.push(change);
                        }
                        // Reset patience and leave time
                        c.patience = c.initialPatience;
                        c.leaveTime = currentTime + c.patience;
                        // Update leaveEvents
                        leaveEvents.add(c.leaveTime, c);
                        // Re-insert customer if they have remaining budget
                        if (c.budget > 0) {
                            customerQueue.add(c);
                        } else {
                            idToCustomer.remove(c.id);
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
                queryOList[queryOIndex++] = new QueryO(queryType, X, qIndex); // Store the query index for output ordering
                break;
            default:
                // Ignore invalid commands
                break;
        }

        // Recursively process the next query
        processQueries(qIndex + 1, Q, output);
    }

    static void removeLeavingCustomers(int time) {
        TP01_GPT5.LeaveEvents.LeaveEventsNode node = leaveEvents.get(time);
        if (node != null) {
            removeCustomersRecursive(node.customers);
            leaveEvents.remove(time);
        }
    }

    static void removeCustomersRecursive(CustomersList customers) {
        if (customers == null) return;
        customerQueue.remove(customers.customer);
        idToCustomer.remove(customers.customer.id);
        removeCustomersRecursive(customers.next);
    }

    // Function to find the minimal difference between a price and the fish prices
    static long findMinPriceDifference(long price, int left, int right) {
        if (left > right) return Long.MAX_VALUE;

        int mid = (left + right) / 2;
        if (fishPrices[mid] == price) {
            return 0;
        } else if (fishPrices[mid] < price) {
            long diff = price - fishPrices[mid];
            return Math.min(diff, findMinPriceDifference(price, mid + 1, right));
        } else {
            long diff = fishPrices[mid] - price;
            return Math.min(diff, findMinPriceDifference(price, left, mid - 1));
        }
    }

    // Function to find the most expensive fish the customer can afford
    static long findMaxAffordableFishPrice(long budget, int left, int right) {
        if (left > right) return -1;

        int mid = (left + right) / 2;
        if (fishPrices[mid] == budget) {
            return fishPrices[mid];
        } else if (fishPrices[mid] < budget) {
            long temp = findMaxAffordableFishPrice(budget, mid + 1, right);
            return temp == -1 ? fishPrices[mid] : temp;
        } else {
            return findMaxAffordableFishPrice(budget, left, mid - 1);
        }
    }

    // Function to process O queries
    static void processOQueries(StringBuilder output) {
        // Since the number of O queries is small, we can process them directly
        for (int i = 0; i < queryOIndex; i++) {
            QueryO query = queryOList[i];
            int X = query.X;
            int queryType = query.type;

            int maxHappiness = calculateMaxHappiness(X, M);

            output.append(maxHappiness).append("\n");
        }
    }

    static int calculateMaxHappiness(int budget, int n) {
        if (n == 0 || budget == 0) return 0;

        // Implement the rules to prevent taking 3 consecutive souvenirs
        int maxVal = calculateMaxHappiness(budget, n - 1);

        if (souvenirPrices[n] <= budget) {
            int val = souvenirValues[n] + calculateMaxHappiness(budget - souvenirPrices[n], n - 1);
            maxVal = Math.max(maxVal, val);
        }

        return maxVal;
    }

    // Custom Stack implementation
    static class Stack {
        private StackNode top;

        private class StackNode {
            long data;
            StackNode next;

            StackNode(long data) {
                this.data = data;
            }
        }

        public void push(long data) {
            StackNode newNode = new StackNode(data);
            newNode.next = top;
            top = newNode;
        }

        public long pop() {
            if (top == null) {
                // Handle underflow as needed
                return -1;
            }
            long data = top.data;
            top = top.next;
            return data;
        }

        public boolean isEmpty() {
            return top == null;
        }

        public int size() {
            return sizeRecursive(top);
        }

        private int sizeRecursive(StackNode node) {
            if (node == null) return 0;
            return 1 + sizeRecursive(node.next);
        }
    }

    // Custom Map implementation
    static class Map {
        private MapEntry head;

        private class MapEntry {
            int key;
            Customer value;
            MapEntry next;

            MapEntry(int key, Customer value) {
                this.key = key;
                this.value = value;
            }
        }

        public void put(int key, Customer value) {
            head = putRecursive(head, key, value);
        }

        private MapEntry putRecursive(MapEntry node, int key, Customer value) {
            if (node == null) {
                return new MapEntry(key, value);
            }
            if (node.key == key) {
                node.value = value;
            } else {
                node.next = putRecursive(node.next, key, value);
            }
            return node;
        }

        public Customer get(int key) {
            return getRecursive(head, key);
        }

        private Customer getRecursive(MapEntry node, int key) {
            if (node == null) return null;
            if (node.key == key) return node.value;
            return getRecursive(node.next, key);
        }

        public void remove(int key) {
            head = removeRecursive(head, key);
        }

        private MapEntry removeRecursive(MapEntry node, int key) {
            if (node == null) return null;
            if (node.key == key) return node.next;
            node.next = removeRecursive(node.next, key);
            return node;
        }
    }

    // Custom Priority Queue implementation
    static class PriorityQueue {
        private PQNode head;

        private class PQNode {
            Customer data;
            PQNode next;

            PQNode(Customer data) {
                this.data = data;
            }
        }

        public void add(Customer customer) {
            head = addRecursive(head, customer);
        }

        private PQNode addRecursive(PQNode node, Customer customer) {
            if (node == null || customer.compareTo(node.data) < 0) {
                PQNode newNode = new PQNode(customer);
                newNode.next = node;
                return newNode;
            }
            node.next = addRecursive(node.next, customer);
            return node;
        }

        public Customer pollFirst() {
            if (head == null) return null;
            Customer data = head.data;
            head = head.next;
            return data;
        }

        public void remove(Customer customer) {
            head = removeRecursive(head, customer);
        }

        private PQNode removeRecursive(PQNode node, Customer customer) {
            if (node == null) return null;
            if (node.data.equals(customer)) {
                return node.next;
            }
            node.next = removeRecursive(node.next, customer);
            return node;
        }

        public boolean isEmpty() {
            return head == null;
        }
    }

    // Custom Leave Events implementation
    static class LeaveEvents {
        private LeaveEventsNode head;

        private class LeaveEventsNode {
            int time;
            CustomersList customers;
            LeaveEventsNode next;

            LeaveEventsNode(int time, Customer customer) {
                this.time = time;
                this.customers = new CustomersList(customer);
            }
        }

        public void add(int time, Customer customer) {
            head = addRecursive(head, time, customer);
        }

        private LeaveEventsNode addRecursive(LeaveEventsNode node, int time, Customer customer) {
            if (node == null || time < node.time) {
                LeaveEventsNode newNode = new LeaveEventsNode(time, customer);
                newNode.next = node;
                return newNode;
            } else if (time == node.time) {
                node.customers = addCustomer(node.customers, customer);
                return node;
            } else {
                node.next = addRecursive(node.next, time, customer);
                return node;
            }
        }

        private CustomersList addCustomer(CustomersList list, Customer customer) {
            CustomersList newCustomer = new CustomersList(customer);
            newCustomer.next = list;
            return newCustomer;
        }

        public LeaveEventsNode get(int time) {
            return getRecursive(head, time);
        }

        private LeaveEventsNode getRecursive(LeaveEventsNode node, int time) {
            if (node == null) return null;
            if (node.time == time) return node;
            return getRecursive(node.next, time);
        }

        public void remove(int time) {
            head = removeRecursive(head, time);
        }

        private LeaveEventsNode removeRecursive(LeaveEventsNode node, int time) {
            if (node == null) return null;
            if (node.time == time) return node.next;
            node.next = removeRecursive(node.next, time);
            return node;
        }

        public void remove(int time, Customer customer) {
            LeaveEventsNode node = get(time);
            if (node != null) {
                node.customers = removeCustomer(node.customers, customer);
            }
        }

        private CustomersList removeCustomer(CustomersList list, Customer customer) {
            if (list == null) return null;
            if (list.customer.equals(customer)) {
                return list.next;
            }
            list.next = removeCustomer(list.next, customer);
            return list;
        }
    }

    static class CustomersList {
        Customer customer;
        CustomersList next;

        CustomersList(Customer customer) {
            this.customer = customer;
        }
    }

    // Custom sorting class for arrays
    static class ArraysSort {
        public static void sort(long[] arr) {
            quickSort(arr, 0, arr.length - 1);
        }

        private static void quickSort(long[] arr, int low, int high) {
            if (low < high) {
                int pi = partition(arr, low, high);

                quickSort(arr, low, pi - 1);
                quickSort(arr, pi + 1, high);
            }
        }

        private static int partition(long[] arr, int low, int high) {
            long pivot = arr[high];
            int i = (low - 1); // index of smaller element
            for (int j = low; j < high; j++) {
                if (arr[j] <= pivot) {
                    i++;

                    // swap arr[i] and arr[j]
                    long temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }

            // swap arr[i+1] and arr[high] (or pivot)
            long temp = arr[i + 1];
            arr[i + 1] = arr[high];
            arr[high] = temp;

            return i + 1;
        }
    }

    // FastReader class for faster input
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String nextLine() throws IOException {
            return br.readLine();
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

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }
}
