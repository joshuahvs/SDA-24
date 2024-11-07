import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Lab6test6 {
    private static InputReader in;
    private static PrintWriter out;
    private static AVLTree tree = new AVLTree();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        int N = in.nextInteger();
        for (int i = 0; i < N; i++) {
            String tier = in.next();
            int battlePoint = in.nextInteger();
            tree.insert(new Char(tier, battlePoint));
        }

        int A = in.nextInteger();
        for (int i = 0; i < A; i++) {
            String query = in.next();
            if (query.equals("G")) {
                String tier = in.next();
                int battlePoint = in.nextInteger();
                tree.insert(new Char(tier, battlePoint));
                tree.printParentOfLastInserted(out);
            } else if (query.equals("R")) {
                String tier = in.next();
                int battlePoint = in.nextInteger();
                boolean removed = tree.delete(new Char(tier, battlePoint));
                if (!removed) {
                    out.println("-1");
                } else {
                    out.println(tier + " " + battlePoint);
                }
            } else if (query.equals("T")) {
                tree.printTopAndBottomThree(out);
            }
        }
        out.close();
    }

    static class Char {
        String tier;
        int battlePoint;

        Char(String tier, int battlePoint) {
            this.tier = tier;
            this.battlePoint = battlePoint;
        }

        static Map<String, Integer> tierRankMap = new HashMap<>();
        static {
            tierRankMap.put("S", 7);
            tierRankMap.put("A", 6);
            tierRankMap.put("B", 5);
            tierRankMap.put("C", 4);
            tierRankMap.put("D", 3);
            tierRankMap.put("E", 2);
            tierRankMap.put("F", 1);
        }

        int getTierRank(String tier) {
            return tierRankMap.getOrDefault(tier, 0);
        }

        // Comparator to prioritize tier in descending order, then battlePoint in descending order
        int compare(Char other) {
            int thisTierRank = getTierRank(this.tier);
            int otherTierRank = getTierRank(other.tier);
            if (thisTierRank != otherTierRank) {
                return Integer.compare(otherTierRank, thisTierRank); // Descending order for tier rank
            }
            return Integer.compare(other.battlePoint, this.battlePoint); // Descending order for battlePoint
        }

        @Override
        public String toString() {
            return tier + " " + battlePoint;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Char)) return false;
            Char other = (Char) obj;
            return tier.equals(other.tier) && battlePoint == other.battlePoint;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tier, battlePoint);
        }
    }

    static class Node {
        Node left, right;
        Char character;
        int height;

        Node(Char character) {
            this.character = character;
            this.height = 1;
        }
    }

    static class AVLTree {
        Node root;
        Node lastInserted;
        int size = 0; // Maintain the size of the tree

        int height(Node N) {
            if (N == null) return 0;
            return N.height;
        }

        int getBalance(Node N) {
            if (N == null) return 0;
            return height(N.left) - height(N.right);
        }

        Node singleRightRotate(Node y) {
            Node x = y.left;
            Node T2 = x.right;
            x.right = y;
            y.left = T2;
            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;
            return x;
        }

        Node singleLeftRotate(Node x) {
            Node y = x.right;
            Node T2 = y.left;
            y.left = x;
            x.right = T2;
            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;
            return y;
        }

        Node insert(Node node, Char character) {
            if (node == null) {
                lastInserted = new Node(character);
                size++; // Increase size when a new node is inserted
                return lastInserted;
            }

            if (character.compare(node.character) < 0) {
                node.left = insert(node.left, character);
            } else if (character.compare(node.character) > 0) {
                node.right = insert(node.right, character);
            } else {
                return node;
            }

            node.height = Math.max(height(node.left), height(node.right)) + 1;

            int balance = getBalance(node);

            if (balance > 1 && character.compare(node.left.character) < 0) {
                return singleRightRotate(node);
            }
            if (balance < -1 && character.compare(node.right.character) > 0) {
                return singleLeftRotate(node);
            }
            if (balance > 1 && character.compare(node.left.character) > 0) {
                node.left = singleLeftRotate(node.left);
                return singleRightRotate(node);
            }
            if (balance < -1 && character.compare(node.right.character) < 0) {
                node.right = singleRightRotate(node.right);
                return singleLeftRotate(node);
            }

            return node;
        }

        void insert(Char character) {
            root = insert(root, character);
        }

        Node minValueNode(Node node) {
            Node current = node;
            while (current.left != null) current = current.left;
            return current;
        }

        Node deleteNode(Node root, Char character, boolean[] deletedFlag) {
            if (root == null) return root;

            if (character.compare(root.character) < 0) {
                root.left = deleteNode(root.left, character, deletedFlag);
            } else if (character.compare(root.character) > 0) {
                root.right = deleteNode(root.right, character, deletedFlag);
            } else {
                deletedFlag[0] = true; // Mark that deletion has occurred
                size--; // Decrease size when a node is deleted
                if ((root.left == null) || (root.right == null)) {
                    Node temp = root.left != null ? root.left : root.right;
                    root = temp;
                } else {
                    Node temp = minValueNode(root.right);
                    root.character = temp.character;
                    root.right = deleteNode(root.right, temp.character, new boolean[]{false});
                }
            }

            if (root == null) return root;

            root.height = Math.max(height(root.left), height(root.right)) + 1;

            int balance = getBalance(root);

            if (balance > 1 && getBalance(root.left) >= 0) {
                return singleRightRotate(root);
            }
            if (balance > 1 && getBalance(root.left) < 0) {
                root.left = singleLeftRotate(root.left);
                return singleRightRotate(root);
            }
            if (balance < -1 && getBalance(root.right) <= 0) {
                return singleLeftRotate(root);
            }
            if (balance < -1 && getBalance(root.right) > 0) {
                root.right = singleRightRotate(root.right);
                return singleLeftRotate(root);
            }

            return root;
        }

        boolean delete(Char character) {
            boolean[] deletedFlag = new boolean[]{false};
            root = deleteNode(root, character, deletedFlag);
            return deletedFlag[0];
        }

        void printParentOfLastInserted(PrintWriter out) {
            if (lastInserted == null) {
                out.println("-1");
            } else {
                Node parent = findParent(root, lastInserted.character);
                if (parent != null) {
                    out.println(parent.character);
                } else {
                    out.println(lastInserted.character);
                }
            }
        }

        Node findParent(Node root, Char character) {
            if (root == null || root.character.equals(character)) return null;
            if ((root.left != null && root.left.character.equals(character)) ||
                (root.right != null && root.right.character.equals(character))) {
                return root;
            }
            if (character.compare(root.character) < 0) return findParent(root.left, character);
            else return findParent(root.right, character);
        }

        void printTopAndBottomThree(PrintWriter out) {
            if (size == 0) {
                out.println("|");
                return;
            }

            List<Char> allNodes = new ArrayList<>();
            getAllNodes(root, allNodes);

            // Create a copy of allNodes for top three sorting
            List<Char> allNodesCopyForTop = new ArrayList<>(allNodes);
            allNodesCopyForTop.sort((c1, c2) -> {
                int rank1 = c1.getTierRank(c1.tier);
                int rank2 = c2.getTierRank(c2.tier);
                if (rank1 != rank2) return Integer.compare(rank2, rank1); // Descending tier rank
                return Integer.compare(c2.battlePoint, c1.battlePoint); // Descending battlePoint
            });
            List<Char> topThree = allNodesCopyForTop.subList(0, Math.min(3, allNodesCopyForTop.size()));

            // Create a separate copy of allNodes for bottom three sorting
            List<Char> allNodesCopyForBottom = new ArrayList<>(allNodes);
            allNodesCopyForBottom.sort((c1, c2) -> {
                int rank1 = c1.getTierRank(c1.tier);
                int rank2 = c2.getTierRank(c2.tier);
                if (rank1 != rank2) return Integer.compare(rank1, rank2); // Ascending tier rank
                return Integer.compare(c1.battlePoint, c2.battlePoint); // Ascending battlePoint
            });
            List<Char> bottomThree = allNodesCopyForBottom.subList(0, Math.min(3, allNodesCopyForBottom.size()));

            out.println(formatList(topThree) + " | " + formatList(bottomThree));
        }

        void getAllNodes(Node node, List<Char> list) {
            if (node == null) return;
            getAllNodes(node.left, list);
            list.add(node.character);
            getAllNodes(node.right, list);
        }

        String formatList(List<Char> list) {
            StringBuilder sb = new StringBuilder();
            for (Char c : list) sb.append(c).append(" ");
            return sb.toString().trim();
        }
    }

    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    String s = reader.readLine();
                    if (s == null) return null;
                    tokenizer = new StringTokenizer(s);
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
