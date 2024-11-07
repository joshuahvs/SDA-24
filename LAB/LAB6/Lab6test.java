import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Lab6test {
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
            switch (query) {
                case "G":
                    String tier = in.next();
                    long battlePoint = in.nextInteger();
                    tree.insert(new Char(tier, battlePoint));
                    tree.printParentOfLastInserted(out);
                    break;
                case "R":
                    tier = in.next();
                    battlePoint = in.nextInteger();
                    boolean removed = tree.delete(new Char(tier, battlePoint));
                    if (!removed) {
                        out.println("-1");
                    }
                    break;
                case "T":
                    tree.printTopAndBottomThree(out);
                    break;
            }
        }
        out.close();
    }

    static class Char {
        String tier;
        long battlePoint;

        Char(String tier, long battlePoint2) {
            this.tier = tier;
            this.battlePoint = battlePoint2;
        }

        // Comparator to prioritize tier and battlePoint in specific order
        int compare(Char other) {
            if (!this.tier.equals(other.tier)) {
                return this.tier.compareTo(other.tier);
            } else{
                return Long.compare(this.battlePoint, other.battlePoint);
            }

        }

        @Override
        public String toString() {
            return tier + " " + battlePoint;
        }
    }

    static class Node {
        Node left, right;
        Char character;
        long height;

        Node(Char character) {
            this.character = character;
            this.height = 1;
        }
    }

    static class AVLTree {
        Node root;
        Node lastInserted;

        long height(Node N) {
            if (N == null)
                return 0;
            return N.height;
        }

        long getBalance(Node N) {
            if (N == null)
                return 0;
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

            long balance = getBalance(node);

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
            while (current.left != null)
                current = current.left;
            return current;
        }

        Node deleteNode(Node root, Char character) {
            if (root == null)
                return root;

            if (character.compare(root.character) < 0) {
                root.left = deleteNode(root.left, character);
            } else if (character.compare(root.character) > 0) {
                root.right = deleteNode(root.right, character);
            } else {
                if ((root.left == null) || (root.right == null)) {
                    Node temp = null;
                    if (temp == root.left)
                        root = root.right;
                    else
                        root = root.left;
                } else {
                    Node temp = minValueNode(root.right);
                    root.character = temp.character;
                    root.right = deleteNode(root.right, temp.character);
                }
            }

            if (root == null)
                return root;

            root.height = Math.max(height(root.left), height(root.right)) + 1;

            long balance = getBalance(root);

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
            long originalSize = countNodes(root);
            root = deleteNode(root, character);
            return countNodes(root) < originalSize;
        }

        long countNodes(Node node) {
            if (node == null)
                return 0;
            return 1 + countNodes(node.left) + countNodes(node.right);
        }

        void printParentOfLastInserted(PrintWriter out) {
            if (lastInserted == null) {
                out.println("-1");
            } else {
                Node parent = findParent(root, lastInserted.character);
                if (parent != null)
                    out.println(parent.character);
                else
                    out.println(lastInserted.character);
            }
        }

        Node findParent(Node root, Char character) {
            if (root == null || root.character == character)
                return null;
            if ((root.left != null && root.left.character == character) ||
                    (root.right != null && root.right.character == character)) {
                return root;
            }
            if (character.compare(root.character) < 0)
                return findParent(root.left, character);
            else
                return findParent(root.right, character);
        }

        void printTopAndBottomThree(PrintWriter out) {
            List<Char> topThree = new ArrayList<>();
            List<Char> bottomThree = new ArrayList<>();
            getTopThree(root, topThree);
            getBottomThree(root, bottomThree);
            out.println(formatList(topThree) + " | " + formatList(bottomThree));
        }

        void getTopThree(Node node, List<Char> list) {
            if (node == null || list.size() >= 3) return;
            getTopThree(node.right, list); // Right first for highest tiers
            if (list.size() < 3) list.add(node.character);
            getTopThree(node.left, list);
        }
        
        void getBottomThree(Node node, List<Char> list) {
            if (node == null || list.size() >= 3) return;
            getBottomThree(node.left, list); // Left first for lowest tiers
            if (list.size() < 3) list.add(node.character);
            getBottomThree(node.right, list);
        }        

        String formatList(List<Char> list) {
            StringBuilder sb = new StringBuilder();
            for (Char c : list)
                sb.append(c).append(" ");
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

