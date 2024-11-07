import java.util.ArrayList;
import java.util.Scanner;

public class LatihanADT1 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String data = input.next();
        ArrayList<String> array = new ArrayList<>();
        ArrayList<String> array2 = new ArrayList<>();
        for (int i = 0; i < data.length(); i++){
            array.add(data.substring(i, i+1));
            array2.add(data.substring(i,i+1));
        }
        for (int i = 0; i < array.size(); i++) {
            String currentChar = array.get(i);
            if (array.indexOf(currentChar) == array.lastIndexOf(currentChar)) {
                System.out.println(currentChar);
                break; 
            }
        }
    }
}
