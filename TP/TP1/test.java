public class test{
    public static void main(String[] args) {
        int A [] = {0,2,5,6,7,3};
        MySort(A);
    }

    public static void MySort(int[] a) {

        for (int ii = 1; ii < a.length; ii++) {
    
            int temp = a[ii];
    
            int jj = ii - 1;
    
            // x = jj >= 0
            // y = a[jj] < temp
            while (jj >= 0 && a[jj]<temp) {
    
                // z = a[jj + 1] = a[jj];
                a[jj + 1] = a[jj];
    
                jj--;
    
            }
    
            a[jj + 1] = temp;
    
        }
        for (int items: a){
            System.out.println(items);
        }
    
    }
    
}