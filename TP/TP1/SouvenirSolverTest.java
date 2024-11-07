import java.util.ArrayList;
import java.util.List;

public class SouvenirSolverTest {
    static class Result {
        int maxHappiness;
        List<Integer> itemsPicked;

        public Result(int maxHappiness, List<Integer> itemsPicked) {
            this.maxHappiness = maxHappiness;
            this.itemsPicked = itemsPicked;
        }
    }

    static Result solveSouvenirDP(long[] costs, long[] values, int maxBudget) {
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

        return new Result(maxHappiness, bestPickedItems);
    }

    public static void main(String[] args) {
        long[] costs = { 100, 5, 5, 6, 7, 8, 9 };
        long[] values = { 1000, 20, 20, 90, 90, 90, 21 };
        int maxBudget = 27;

        Result result = solveSouvenirDP(costs, values, maxBudget);
        System.out.println("Max Happiness: " + result.maxHappiness);
        System.out.println("Items Picked: " + result.itemsPicked);
    }
}
