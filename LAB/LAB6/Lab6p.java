import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Lab6p {
    private static InputReader in;
    private static PrintWriter out;
    private static AVLTree tree = new AVLTree();
    private static int currentID = 1;

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        int N = in.nextInteger();

        for (int i = 0; i < N; i++) {
            String tier = in.next();
            int battlePoint = in.nextInteger();
            Char character = new Char(currentID++, battlePoint, tier);
            tree.root = tree.insert(tree.root, character);
        }

        int Q = in.nextInteger();
        for (int i = 0; i < Q; i++) {
            String query = in.next();
            switch (query) {
                case "G":
                    String tier = in.next();
                    int battlePoint = in.nextInteger();
                    Char character = new Char(currentID++, battlePoint, tier);
                    tree.root = tree.insert(tree.root, character);
                    Node parent = tree.findParent(tree.root, character, null);
                    if (parent != null) {
                        out.println(parent.character.tier + " " + parent.character.battlePoint);
                    } else {
                        out.println(character.tier + " " + character.battlePoint);
                    }
                    break;
                case "R":
                    tier = in.next();
                    battlePoint = in.nextInteger();
                    Char deleteChar = new Char(0, battlePoint, tier);
                    Node deleteNode = tree.findNode(tree.root, deleteChar);
                    if (deleteNode != null) {
                        out.println(deleteNode.character.tier + " " + deleteNode.character.battlePoint);
                        tree.root = tree.delete(tree.root, deleteNode.character);
                    } else {
                        out.println("-1");
                    }
                    break;
                case "T":
                    List<Char> characters = new ArrayList<>();
                    tree.inorderTraversal(tree.root, characters);
                    if (characters.isEmpty()) {
                        out.println("-1");
                    } else {
                        for (int j = 0; j < Math.min(3, characters.size()); j++) {
                            out.print(characters.get(j).tier + " " + characters.get(j).battlePoint + (j < 2 ? " " : ""));
                        }
                        out.print(" |");
                        for (int j = characters.size() - 1; j >= Math.max(characters.size() - 3, 0); j--) {
                            out.print(" " + characters.get(j).tier + " " + characters.get(j).battlePoint);
                        }
                        out.println();
                    }
                    break;
            }
        }

        out.close();
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
                    if (s == null)
                        return null;
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

class Char implements Comparable<Char> {
    int id;
    int battlePoint;
    String tier;
    private static final Map<String, Integer> tierRankings;

    static {
        tierRankings = new HashMap<>();
        tierRankings.put("S", 0);
        tierRankings.put("A", 1);
        tierRankings.put("B", 2);
        tierRankings.put("C", 3);
        tierRankings.put("D", 4);
        tierRankings.put("E", 5);
        tierRankings.put("F", 6);
    }

    public Char(int id, int battlePoint, String tier) {
        this.id = id;
        this.battlePoint = battlePoint;
        this.tier = tier;
    }

    @Override
    public int compareTo(Char other) {
        int thisTierRank = tierRankings.get(this.tier);
        int otherTierRank = tierRankings.get(other.tier);
        if (thisTierRank != otherTierRank) {
            return Integer.compare(thisTierRank, otherTierRank);
        }
        return Integer.compare(other.battlePoint, this.battlePoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Char character = (Char) o;
        return battlePoint == character.battlePoint && tier.equals(character.tier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(battlePoint, tier);
    }
}

class Node {
    Char character;
    Node left, right;
    int height;

    Node(Char character) {
        this.character = character;
        this.height = 1;
    }
}

class AVLTree {
    Node root;

    Node insert(Node node, Char character) {
        if (node == null)
            return new Node(character);

        if (character.compareTo(node.character) < 0)
            node.left = insert(node.left, character);
        else if (character.compareTo(node.character) > 0)
            node.right = insert(node.right, character);
        else
            return node;

        updateHeight(node);
        return rebalance(node, character);
    }

    Node delete(Node node, Char character) {
        if (node == null)
            return null;

        if (character.compareTo(node.character) < 0)
            node.left = delete(node.left, character);
        else if (character.compareTo(node.character) > 0)
            node.right = delete(node.right, character);
        else {
            if (node.left == null || node.right == null)
                node = (node.left != null) ? node.left : node.right;
            else {
                Node minNode = minValueNode(node.right);
                node.character = minNode.character;
                node.right = delete(node.right, minNode.character);
            }
        }

        if (node == null)
            return null;

        updateHeight(node);
        return rebalance(node, character);
    }

    private void updateHeight(Node node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private Node rebalance(Node node, Char character) {
        int balance = getBalance(node);

        if (balance > 1 && character.compareTo(node.left.character) < 0)
            return rightRotate(node);
        if (balance < -1 && character.compareTo(node.right.character) > 0)
            return leftRotate(node);
        if (balance > 1 && character.compareTo(node.left.character) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        if (balance < -1 && character.compareTo(node.right.character) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        return node;
    }

    int height(Node node) {
        return (node == null) ? 0 : node.height;
    }

    int getBalance(Node node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    Node rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    Node leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }

    void inorderTraversal(Node node, List<Char> result) {
        if (node != null) {
            inorderTraversal(node.left, result);
            result.add(node.character);
            inorderTraversal(node.right, result);
        }
    }

    Node findParent(Node node, Char character, Node parent) {
        if (node == null)
            return null;
        if (node.character.equals(character))
            return parent;
        if (character.compareTo(node.character) < 0)
            return findParent(node.left, character, node);
        return findParent(node.right, character, node);
    }

    Node findNode(Node node, Char character) {
        if (node == null)
            return null;
        if (node.character.equals(character))
            return node;
        if (character.compareTo(node.character) < 0)
            return findNode(node.left, character);
        return findNode(node.right, character);
    }
}