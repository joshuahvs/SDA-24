import java.io.*;
import java.util.*;

public class TP01_GPT2 {
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

        // Map to keep track of customers by ID
        Map<Integer, Customer> idToCustomer = new HashMap<>();

        // Stack for discount coupons
        Stack<Long> discountStack = new Stack<>();

        // List of customers in the queue
        List<Customer> customerList = new ArrayList<>();

        // Read and process activities
        for (int q = 0; q < Q; q++) {
            currentTime++;

            String line = br.readLine();
            String[] parts = line.split(" ");
            char command = parts[0].charAt(0);

            if (command == 'A') {
                // New customer arrives
                long budget = Long.parseLong(parts[1]);
                int patience = Integer.parseInt(parts[2]);
                Customer customer = new Customer(customerCount, budget, patience, currentTime);
                customerQueue.add(customer);
                idToCustomer.put(customerCount, customer);
                customerList.add(customer);
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
                if (customer == null || customer.budget == -1) {
                    out.println(-1);
                } else {
                    out.println(customer.budget);
                    customer.budget = -1; // Mark as left
                    customerQueue.remove(customer);
                }
            } else if (command == 'D') {
                // Add discount coupon
                long diskon = Long.parseLong(parts[1]);
                discountStack.push(diskon);
                out.println(discountStack.size());
            } else if (command == 'B') {
                // Update customer patience
                List<Customer> toRemove = new ArrayList<>();
                for (Customer customer : customerList) {
                    if (customer.budget > 0 && customer.arriveTime + customer.patience <= currentTime) {
                        // Customer leaves due to patience expiring
                        customer.budget = -1;
                        customerQueue.remove(customer);
                        toRemove.add(customer);
                    }
                }
                customerList.removeAll(toRemove);

                // Serve the customer
                while (!customerQueue.isEmpty() && idToCustomer.get(customerQueue.peek().id).budget == -1) {
                    customerQueue.poll();
                }
                if (customerQueue.isEmpty()) {
                    out.println(-1);
                } else {
                    Customer customer = customerQueue.poll();
                    if (customer.budget == -1) {
                        out.println(-1);
                        continue;
                    }
                    // Try to find the most expensive fish they can afford
                    int fishPrice = findFishPrice(fishPrices, customer.budget);
                    if (fishPrice == -1) {
                        // Cannot buy any fish
                        out.println(customer.id);
                        customer.budget = -1; // Customer leaves
                    } else {
                        long priceToPay = fishPrice;
                        long change = 0;
                        if (customer.budget == fishPrice) {
                            // Paying exact amount, apply discount
                            if (!discountStack.isEmpty()) {
                                long discount = discountStack.pop();
                                priceToPay = Math.max(1, fishPrice - discount);
                            }
                            // No change is given
                            change = 0;
                            customer.budget -= priceToPay;
                        } else {
                            // Paying with non-exact amount
                            change = customer.budget - fishPrice;
                            // Add change as discount coupon
                            discountStack.push(change);
                            customer.budget = fishPrice;
                            customer.budget -= fishPrice;
                        }
                        // Reset patience and arrival time
                        customer.patience = customer.originalPatience;
                        customer.arriveTime = currentTime;
                        // Re-add to queue if customer still has money
                        if (customer.budget >= 1) {
                            customerQueue.add(customer);
                        } else {
                            customer.budget = -1; // Customer leaves
                        }
                        out.println(change);
                    }
                }
            } else if (command == 'O') {
                // Process 'O' queries (Not implemented in this sample due to constraints)
            }
        }

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
}
