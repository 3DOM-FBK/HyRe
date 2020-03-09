package eu.fbk.threedom.utils;
import java.util.Scanner;

public class Test {
    public static void displayMenu() {
        System.out.println("What would you like to do with the numbers you just entered:");
        System.out.println("1. Add numbers");
        System.out.println("2. Subtract numbers");
        System.out.println("3. Quit");
        System.out.println("Please enter a choice of 1, 2, or 3.");
    }

    public static int getChoice() {
        int choice;
        do {
            Scanner inputDevice = new Scanner(System.in);
            displayMenu();
            System.out.println("\nWhat would you like to do: ");
            choice = inputDevice.nextInt();
            if(choice > 3|| choice < 1)
                System.out.println("This is an invalid choice!");
        } while(choice > 3 || choice < 1);
        return choice;
    }

    public static void main(String[] args) {
        int num1;
        int num2;
        int answer;

        Scanner inputDevice = new Scanner(System.in);
        int choice;

        System.out.print("Please enter a number:");
        num1 = inputDevice.nextInt();
        System.out.print("Please enter a second number: ");
        num2 = inputDevice.nextInt();

        do {
            System.out.println("Your numbers are: " + num1 + " and "+ num2 +"\n");
            choice = getChoice();
            if(choice == 1)
                answer = num1 + num2;
            else if(choice == 2)
                answer = num1 -num2;
            if(choice == 3)
                System.out.print("Thank you for using my program. Have a nice day!");
        } while (choice !=3);
    }
}
