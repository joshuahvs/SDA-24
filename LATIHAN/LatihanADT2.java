import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;

public class LatihanADT2 {
    public static void main(String[] args) {
        // CONTOH 1
        // int[] menu = { 0, 1, 0, 1 };
        // int[] siswa = { 1, 1, 0, 0 };

        // CONTOH 2 
        int[] menu = { 1,0,0,0,1,1};
        int[] siswa = {1,1,1,0,0,1};

        Stack<Integer> menuStack = new Stack<>();
        for (int i = menu.length - 1; i >= 0; i--) {
            menuStack.push(menu[i]);
        }

        Queue<Integer> siswaQueue = new LinkedList<>();
        for (int s : siswa) {
            siswaQueue.offer(s);
        }

        int studentsNotFed = siswa.length;
        int totalIteration = 0;
        while (!siswaQueue.isEmpty() && !menuStack.isEmpty() && totalIteration!=1000) {
            int currentStudent = siswaQueue.poll();
            if (currentStudent == menuStack.peek()) {
                menuStack.pop();
                studentsNotFed -= 1;
            } else {
                siswaQueue.offer(currentStudent);
            }
            totalIteration += 1;
        }

        System.out.println(studentsNotFed);
    }
}
