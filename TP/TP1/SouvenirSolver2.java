import java.util.Arrays;

public class SouvenirSolver2 {

    public static void main(String[] args) {
        long[] hargaSuvenir = { 100, 5, 5, 6, 7, 8, 9 };
        long[] kebahagiaanSuvenir = { 1000, 20, 20, 90, 90, 90, 21 };
        int maxBudget = 27;

        solveSouvenirDPWithItems(hargaSuvenir, kebahagiaanSuvenir, maxBudget);
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

        System.out.println(maxHappiness);
        for (int i = 0; i < selectedLength; i++) {
            System.out.print(selectedSouvenirs[i] + " ");
        }
        System.out.println();
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
}
