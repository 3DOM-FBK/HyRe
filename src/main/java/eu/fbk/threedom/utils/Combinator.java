/**
 * Hybrid Registration (C) 2019 is a command line software designed to
 * analyze, co-register and filter airborne point clouds acquired by LiDAR sensors
 * and photogrammetric algorithm.
 * Copyright (C) 2019  Michele Welponer, mwelponer@gmail.com (Fondazione Bruno Kessler)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <https://www.gnu.org/licenses/> and file GPL3.txt
 *
 * -------------
 * IntelliJ Program arguments:
 * $ContentRoot$/resources/f1.txt $ContentRoot$/resources/f2.txt 1f -w -v
 */
package eu.fbk.threedom.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Combinator {

    private static List result;

//    /* arr[]  ---> Input Array
//    data[] ---> Temporary array to store current combination
//    start & end ---> Staring and Ending indexes in arr[]
//    index  ---> Current index in data[]
//    r ---> Size of a combination to be printed */
//    private static void combinationUtil(int arr[], int data[], int start, int end, int index, int r) {
//        // Current combination is ready to be printed, print it
//        if (index == r)
//        {
//            int[] temp = new int[r];
//            for (int j=0; j < r; j++) {
//                temp[j] = data[j];
//                //System.out.print(data[j] + " ");
//            }
//            //System.out.println("");
//
//            result.add(temp);
//
//            return;
//        }
//
//        // replace index with all possible elements. The condition
//        // "end-i+1 >= r-index" makes sure that including one element
//        // at index will make a combination with remaining elements
//        // at remaining positions
//        for (int i=start; i<=end && end-i+1 >= r-index; i++)
//        {
//            data[index] = arr[i];
//            combinationUtil(arr, data, i+1, end, index+1, r);
//        }
//    }
//
//    // The main function that prints all combinations of size r
//    // in arr[] of size n. This function mainly uses combinationUtil()
//    public static List<int[]> generate(int[] input, int combinationSize) {
//        result = new ArrayList<int[]>();
//
//        // A temporary array to store all combination one by one
//        int data[] = new int[combinationSize];
//
//        // Print all combination using temprary array 'data[]'
//        combinationUtil(input, data, 0, input.length-1, 0, combinationSize);
//
//        return result;
//    }

    /* arr[]  ---> Input Array
data[] ---> Temporary array to store current combination
start & end ---> Staring and Ending indexes in arr[]
index  ---> Current index in data[]
r ---> Size of a combination to be printed */
    private static void combinationUtil(String arr[], String data[], int start, int end, int index, int r) {
        // Current combination is ready to be printed, print it
        if (index == r) {
            String[] temp = new String[r];
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
        for (int i=start; i <= end && end-i+1 >= r-index; i++) {
            data[index] = arr[i];
            combinationUtil(arr, data, i+1, end, index+1, r);
        }
    }

    // The main function that prints all combinations of size r
    // in arr[] of size n. This function mainly uses combinationUtil()
    public static List<String[]> generate(String[] input, int combinationSize) {
        result = new ArrayList<String[]>();

        // A temporary array to store all combination one by one
        String[] data = new String[combinationSize];

        // Print all combination using temprary array 'data[]'
        combinationUtil(input, data, 0, input.length-1, 0, combinationSize);

        return result;
    }

    public static void main(String[] args) {
//        int[] data = {1, 2, 3, 4, 5};
//        List<int[]> list = Combinator.generate(data, 3);

        String[] data = {"c0", "c1", "c2"};
        List<String[]> list = Combinator.generate(data, 2);

//        for (int[] a : list) {
        for (String[] a : list) {
            for (String i : a)
                System.out.print(i + " ");

            System.out.print("\n");
        }

        double test = 5435345.235467;
        System.out.println("double: " + test);
        System.out.println("float: " + (float)test);
    }
}
