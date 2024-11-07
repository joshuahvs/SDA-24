import java.io.*;
import java.util.StringTokenizer;

/**
 * Note:
 * 1. Mahasiswa tidak diperkenankan menggunakan data struktur dari library seperti ArrayList, LinkedList, dll.
 * 2. Mahasiswa diperkenankan membuat/mengubah/menambahkan class, class attribute, instance attribute, tipe data, dan method 
 *    yang sekiranya perlu untuk menyelesaikan permasalahan.
 * 3. Mahasiswa dapat menggunakan method {@code printList()} dari class {@code DoublyLinkedList}
 *    untuk membantu melakukan print statement debugging dan print hasil akhir.
**/
public class Lab5p {
    private static InputReader in;
    private static PrintWriter out;
    private static DoublyLinkedList keyboard = new DoublyLinkedList();

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        in = new InputReader(inputStream);
        OutputStream outputStream = System.out;
        out = new PrintWriter(outputStream);

        int N = in.nextInt();

        for (int i = 0; i < N; i++) {
            String command = in.next();
            char data;
            char direction;

            switch (command) {
                case "ADD":
                    direction = in.nextChar();
                    data = in.nextChar();
                    keyboard.add(data, direction);
                    break;

                case "DEL":
                    keyboard.delete();
                    break;

                case "RIGHT":
                    keyboard.right();
                    break;
                
                case "LEFT":
                    keyboard.left();
                    break;

                case "START":
                    keyboard.start();
                    break;

                case "END":
                    keyboard.end();
                    break;

                case "SUB":
                    direction = in.nextChar();
                    keyboard.sub(direction);
                    break;
            }
        }

        keyboard.printList();
        out.close();
    }

    // taken from https://codeforces.com/submissions/Petr
    // together with PrintWriter, these input-output (IO) is much faster than the
    // usual Scanner(System.in) and System.out
    // please use these classes to avoid your fast algorithm gets Time Limit
    // Exceeded caused by slow input-output (IO)
    private static class InputReader {

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

        public char nextChar() {
        return next().charAt(0);
        }

        public int nextInt() {
        return Integer.parseInt(next());
        }
    }
}

class DoublyLinkedList {
    ListNode first;
    ListNode current;
    ListNode last;
    int size = 0;

    public DoublyLinkedList() {
        this.first = null;
        this.current = null;
        this.last = null;
    }

    public void printList() {
        ListNode current = first;
        do {
            System.out.print(current.data);
            current = current.next;
        } while (current != null && current != first);
        System.out.println();
    }

    /*
    * Method untuk menambahkan ListNode dari {@code current} ListNode
    */
    public void add(char data, char direction) {
        //TODO: Implement this method
        ListNode newNode = new ListNode(data);
        if (first == null) {  // Jika LinkedList masih kosong
            first = newNode;
            last = newNode;
            current = newNode;
        } else if (direction == 'L') {
            if (current == first) {
                newNode.next = first;
                first.prev = newNode;
                first = newNode;
            } else {
                newNode.next = current;
                newNode.prev = current.prev;
                if (current.prev != null) {
                    current.prev.next = newNode;
                }
                current.prev = newNode;
            }
            current = newNode;
        } else if (direction == 'R') {
            newNode.prev = current;
            newNode.next = current.next;
            if (current.next != null) {
                current.next.prev = newNode;
            }
            current.next = newNode;
            if (current == last) {
                last = newNode;
            }
            current = newNode;
        }
    }

    /*
    * Method untuk menghapus ListNode dari {@code current} ListNode
    */
    public void delete() {
        //TODO: Implement this method
        if (current == null) return;
        
        if (current == first && current == last) {  // Hanya ada satu node
            first = last = current = null;
        } else if (current == first) {  // Hapus node pertama
            first = current.next;
            if (first != null) {
                first.prev = null;
            }
            current = first;
        } else if (current == last) {  // Hapus node terakhir
            last = current.prev;
            if (last != null) {
                last.next = null;
            }
            current = last;
        } else {  // Hapus node di tengah
            current.prev.next = current.next;
            current.next.prev = current.prev;
            current = current.prev;
        }
    }

    /*
    * Method untuk berpindah ke kanan (next) dari {@code current} ListNode
    */
    public void right() {
        //TODO: Implement this method
        if (current != null && current.next != null) {
            current = current.next;
        }
    }

    /*
    * Method untuk berpindah ke kiri (prev) dari {@code current} ListNode
    */
    public void left() {
        //TODO: Implement this method
        if (current != null && current.prev != null) {
            current = current.prev;
        }
    }

    /*
    * Method untuk berpindah ke node paling pertama (first) dari DoublyLinkedList
    */
    public void start() {
        //TODO: Implement this method
        current = first;
    }
    
    /*
    * Method untuk berpindah ke node paling terakhir (end) dari DoublyLinkedList
    */
    public void end() {
        //TODO: Implement this method
        current = last;
    }

    /*
    * Method untuk memindahkan ListNode dari  kiri {@code current} ListNode ke kanan {@code current} ListNode
    */
    public void sub(char direction) {
        if (current == null) return;
    
        // Substitution to the left (prev)
        if (direction == 'L' && current.prev != null) {
            char temp = current.data;
            current.data = current.prev.data;
            current.prev.data = temp;
            current = current.prev;  // Move current to the swapped position
        }
        // Substitution to the right (next)
        else if (direction == 'R' && current.next != null) {
            char temp = current.data;
            current.data = current.next.data;
            current.next.data = temp;
            current = current.next;  // Move current to the swapped position
        }
    }
}

class ListNode {

    char data;
    ListNode next;
    ListNode prev;

    ListNode(char data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }
}