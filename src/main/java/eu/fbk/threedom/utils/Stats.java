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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Stats {

    private static long time;

    // Function for calculating mean
    public static float mean(float a[], int n) {
        float sum = 0;
        for (int i = 0; i < n; i++)
            sum += a[i];

        return sum / (float)n;
    }

    public static float mean(ArrayList<Float> a, int n) {
        float sum = 0;
        for (float f : a)
            sum += f;

        return sum / (float)n;
    }

    // Generic function to convert an Array to Set
    public static <T> Set<T> convertArrayToSet(T array[])
    {

        // Create the Set by passing the Array
        // as parameter in the constructor
        Set<T> set = new HashSet<>(Arrays.asList(array));

        // Return the converted Set
        return set;
    }

    // Function for calculating median
    public static float median(float a[], int n) {
        // First we sort the array
        Arrays.sort(a);
//        for (int i = 0; i < n; i++)
//            System.out.println(a[i]);

        // check for odd case
        if (n % 2 != 0)
            return a[n / 2];

//        System.out.println("a[(n - 1) / 2] " + a[(n - 1) / 2]);
//        System.out.println("a[n / 2] " +  a[n / 2]);
//        System.out.println("a[(n - 1) / 2] + a[n / 2] " + (a[(n - 1) / 2] + a[n / 2]));

        return (a[(n - 1) / 2] + a[n / 2]) / 2;
    }

    // Function for calculating median
    public static double median(double a[], int n) {
        // First we sort the array
        Arrays.sort(a);
//        for (int i = 0; i < n; i++)
//            System.out.println(a[i]);

        // check for odd case
        if (n % 2 != 0)
            return a[n / 2];

//        System.out.println("a[(n - 1) / 2] " + a[(n - 1) / 2]);
//        System.out.println("a[n / 2] " +  a[n / 2]);
//        System.out.println("a[(n - 1) / 2] + a[n / 2] " + (a[(n - 1) / 2] + a[n / 2]));

        return (a[(n - 1) / 2] + a[n / 2]) / 2;
    }


    // Function for calculating mad
    public static float mad(float a[], int n){
        float med = median(a, n);
//        System.out.println("med: " + med);

        for (int i = 0; i < n; i++) {
            a[i] = Math.abs(a[i] - med);
//            System.out.println(a[i]);
        }

        return median(a, n);
    }

    public static double mad(double a[], int n){
        double med = median(a, n);
//        System.out.println("med: " + med);

        for (int i = 0; i < n; i++) {
            a[i] = Math.abs(a[i] - med);
//            System.out.println(a[i]);
        }

        return median(a, n);
    }

    private static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%dh:%02dm:%02ds", h,m,s);
    }

    public static String elapsedTime(long start, String message){
        time = (System.currentTimeMillis() - start) / 1000;
        String out = message + " (" + convertSecondsToHMmSs(time) + ")";
        //System.out.println(out);
        return out;
    }

    public static void printElapsedTime(long start, String message){
        time = (System.currentTimeMillis() - start) / 1000;
        String out = message + " (" + convertSecondsToHMmSs(time) + ")";
        System.out.println(out);
    }

    public static void main(String[] args){
        float x = (float) 0.7;
        float y = (float) 1.2;
        System.out.println("sum " + (x + y));

        float[] a = {0.7f, 1.2f, 1.3f, 0.4f};
        float[] b = {3.5f, 1.7f, 21.5f, 9.3f};
        System.out.println("mad: " + Stats.mad(a, a.length));
        System.out.println("mad: " + Stats.mad(b, b.length));
    }
}
