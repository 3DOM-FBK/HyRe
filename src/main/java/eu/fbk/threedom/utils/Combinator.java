package eu.fbk.threedom.utils;

import java.util.ArrayList;
import java.util.List;

public class Combinator {

    private static int[] input;
    private static List<int[]> result;

    public Combinator(int[] data){
        this.input = data;
        result = new ArrayList<>();
    }

//    private void helper(List<int[]> combinations, int start, int end, int index) {
//        if (index == data.length) {
//            int[] combination = data.clone();
//            combinations.add(combination);
//        } else {
//            System.out.println("start <= end : " + start + " <= " + end);
//            if (start <= end) {
//                System.out.println("..data[index] = data[start] : " + data[index] + " = " + data[start]);
//                data[index] = data[start];
//                helper(combinations, start + 1, end, index + 1);
//                helper(combinations, start + 1, end, index);
//            }
//        }
//    }
//
//    public List<int[]> generate(int numberOfitems, int combinationSize) {
//        List<int[]> combinations = new ArrayList<>();
//        helper(combinations, 0, numberOfitems-1, 0);
//        return combinations;
//    }
//


    /* arr[]  ---> Input Array
    data[] ---> Temporary array to store current combination
    start & end ---> Staring and Ending indexes in arr[]
    index  ---> Current index in data[]
    r ---> Size of a combination to be printed */
    static void combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r)
    {
        // Current combination is ready to be printed, print it
        if (index == r)
        {
            int[] temp = new int[r];
            for (int j=0; j < r; j++) {
                temp[j] = data[j];
                //System.out.print(data[j] + " ");
            }
            //System.out.println("");

            result.add(temp);

            return;
        }

        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {
            data[index] = arr[i];
            combinationUtil(arr, data, i+1, end, index+1, r);
        }
    }

    // The main function that prints all combinations of size r
    // in arr[] of size n. This function mainly uses combinationUtil()
    static void printCombination(int input[], int n, int r)
    {
        // A temporary array to store all combination one by one
        int data[] = new int[r];

        // Print all combination using temprary array 'data[]'
        combinationUtil(input, data, 0, n-1, 0, r);
    }



    public static void main(String[] args){
//        //String[] data = {"c0", "c1", "c2"};
//        int[] data = {3, 8, 2};
//        Combinator c = new Combinator(data);
//        List combinations = c.generate(data.length, 2);
//
//        for(Object o : combinations) {
//            System.out.print("\n");
//            int[] a = (int[])o;
//            for(int i : a)
//                System.out.print(i + ", ");
//        }

        int data[] = {1, 2, 3, 4, 5};
        int r = 3;
        int n = data.length;

        Combinator c = new Combinator(data);
        c.printCombination(data, data.length, 3);

        for (int[] a : result) {
            for (int i : a)
                System.out.print(i + " ");

            System.out.print("\n");
        }
    }







//    private void helper(List<String[]> combinations, int intdata[], int start, int end, int index) {
//        if (index == this.data.length) {
//            String[] combination = this.data.clone();
//            combinations.add(combination);
//        } else if (start <= end) {
//            intdata[index] = start;
//            helper(combinations, intdata, start + 1, end, index + 1);
//            helper(combinations, intdata, start + 1, end, index);
//        }
//    }

//    public List<int[]> generate(int n, int r) {
//        List<int[]> combinations = new ArrayList<>();
//        helper(combinations, new int[r], 0, n-1, 0);
//        return combinations;
//    }
}
