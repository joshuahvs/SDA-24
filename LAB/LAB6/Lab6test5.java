import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Lab6test5 {
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
            //TODO:  process inputs
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
                    //TODO : Add character to the tree
                    String tier = in.next();
                    int battlePoint = in.nextInteger();
                    Char character = new Char(currentID++, battlePoint, tier);
                    tree.root = tree.insert(tree.root, character);
                    // Cari parent abis insert
                    Node parent = tree.findParent(tree.root, character, null);
                    if (parent != null) {
                        out.println(parent.character.tier + " " + parent.character.battlePoint);
                    } else {
                        out.println(character.tier + " " + character.battlePoint);
                    }
                    break;
                case "R":
                    //TODO : Remove character from the tree
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
                    //TODO : Print top 3 characters highest and lowest
                    List<Char> characters = new ArrayList<>();
                    tree.inorderTraversal(tree.root, characters);
                    if (characters.isEmpty()) {
                        out.println("-1");
                    } else {
                        // Print 3 karakter kuat (normal)
                        for (int j = 0; j < Math.min(3, characters.size()); j++) {
                            out.print(characters.get(j).tier + " " + characters.get(j).battlePoint + (j < 2 ? " " : ""));
                        }
                        out.print(" |");
                        // Print 3 karakter lemah (kebalik)
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
            String s = next();
            if (s == null)
                return -1;
            return Integer.parseInt(s);
        }
    }
}

class Char implements Comparable<Char> {
     //TODO: Implement Char class
    int id;
    int battlePoint;
    String tier;

    private static final List<String> tierRanking = Arrays.asList("S", "A", "B", "C", "D", "E", "F");

    public Char(int id, int battlePoint, String tier) {
        this.id = id;
        this.battlePoint = battlePoint;
        this.tier = tier;
    }

    @Override
    public int compareTo(Char other) {
        int thisTierRank = tierRanking.indexOf(this.tier);
        int otherTierRank = tierRanking.indexOf(other.tier);

        // Pake char yang lebih tinggi tiernya
        if (thisTierRank != otherTierRank) {
            return Integer.compare(thisTierRank, otherTierRank);
        }

        // Kalo tier sama, pake yang battle point lebih tinggi
        return Integer.compare(other.battlePoint, this.battlePoint);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Char character = (Char) o;

        if (battlePoint != character.battlePoint)
            return false;
        return tier.equals(character.tier);
    }

    @Override
    public int hashCode() {
        int result = battlePoint;
        result = 31 * result + tier.hashCode();
        return result;
    }
}

class Node {
    // TODO: modify attributes as needed
    Char character;
    Node left, right;
    int height;

    Node(Char character) {
        this.character = character;
        this.height = 1;
    }
}

class AVLTree {
    // TODO: modify attributes as needed
    Node root;

    Node insert(Node node, Char character) {
        // TODO: implement this method
        if (node == null) {
            return new Node(character);
        }

        if (character.compareTo(node.character) < 0) {
            node.left = insert(node.left, character);
        } else if (character.compareTo(node.character) > 0) {
            node.right = insert(node.right, character);
        } else {
            return node;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));
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

    Node delete(Node node, Char character) {
        // TODO: implement this method
        if (node == null)
            return node;

        if (character.compareTo(node.character) < 0)
            node.left = delete(node.left, character);
        else if (character.compareTo(node.character) > 0)
            node.right = delete(node.right, character);
        else {
            if ((node.left == null) || (node.right == null)) {
                Node temp;
                if (node.left != null) {
                    temp = node.left;
                } else {
                    temp = node.right;
                }
                
                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }
            } else {
                Node temp = minValueNode(node.right);
                node.character = temp.character;
                node.right = delete(node.right, temp.character);
            }
        }

        if (node == null)
            return node;

        node.height = Math.max(height(node.left), height(node.right)) + 1;
        int balance = getBalance(node);

        if (balance > 1 && getBalance(node.left) >= 0)
            return rightRotate(node);

        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        if (balance < -1 && getBalance(node.right) <= 0)
            return leftRotate(node);

        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    int height(Node N) {
        if (N == null)
            return 0;
        return N.height;
    }

    int getBalance(Node N) {
        if (N == null)
            return 0;
        return height(N.left) - height(N.right);
    }

    Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
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
        // TODO: implement this method
        if (node == null)
            return null;
        if (node.character.equals(character))
            return parent;
        if (character.compareTo(node.character) < 0)
            return findParent(node.left, character, node);
        else
            return findParent(node.right, character, node);
    }

    Node findNode(Node node, Char character) {
        // TODO: implement this method
        if (node == null)
            return null;
        if (node.character.equals(character))
            return node;
        if (character.compareTo(node.character) < 0)
            return findNode(node.left, character);
        else
            return findNode(node.right, character);
    }
}