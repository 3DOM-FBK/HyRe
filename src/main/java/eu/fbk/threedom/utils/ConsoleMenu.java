package eu.fbk.threedom.utils;

import java.util.*;

public class ConsoleMenu {

    private Scanner scanner;

    private Stack<Integer> history = new Stack<>();
    private int menuLevel;
    private int backIndex;

    ConsoleMenu(){
        menuLevel = -1;
        backIndex = -1;
    }

    private int select(int selected){
        //System.out.println("  menuLevel(" + menuLevel + ")");
        //System.out.println("  select(" + selected + ")");
        int sel;

        switch(menuLevel) {
            case 0: System.out.println("\nmenu:\n 1. print info\n 2. change parameters\n 3. quit");
                break;

            case 1:
                switch(selected){
                    case 1:
                        while (true) {
                            System.out.println("\nprint info:\n 1. location\n 2. class\n 3. voxel\n 4. back");
                            if (!scanner.hasNextInt()) {
                                System.out.println("enter only integers! ");
                                scanner.next(); // discard
                                continue;
                            }
                            sel = scanner.nextInt();

                            switch (sel) {
                                case 1:
                                    System.out.println("insert location:");
                                    String location = scanner.next();
                                    // check if valid location
                                    System.out.println("location: " + location);
                                    break;

                                case 2:
                                    System.out.println("insert class:");
                                    // check if integer, check if a valid class
                                    int classType = scanner.nextInt();
                                    System.out.println("class: " + classType);
                                    break;

                                case 3:
                                    System.out.println("insert voxel:");
                                    // check if integer, check if valid voxel
                                    int voxel = scanner.nextInt();
                                    System.out.println("voxel: " + voxel);
                                    break;
                                case 4: break;
                                default: System.out.println("no menu selection available! "); continue;
                            }
                            break;
                        }

                        selected = 4;
                        backIndex = 4;
                        return selected;

                    case 2:
                        while (true) {
                            System.out.println("\nchange parameters:\n 1. voxelSide\n 2. back ");
                            if (!scanner.hasNextInt()) {
                                System.out.println("enter only integers! ");
                                scanner.next(); // discard
                                continue;
                            }
                            sel = scanner.nextInt();

                            switch(sel) {
                                case 1: System.out.println("insert new voxelSide:");
                                    Float voxelSide = scanner.nextFloat();
                                    System.out.println("voxelSide: " + voxelSide);
                                    break;
                                case 2: break;
                                default: System.out.println("no menu selection available! "); continue;
                            }
                            break;
                        }

                        backIndex = 2;
                        return selected;

                    case 3: quit();
                }
                System.out.println("not a valid option!");
                return -1;
                //break;
        }

        while (true) {
            if (!scanner.hasNextInt()) {
                System.out.println("enter only integers! ");
                scanner.next(); // discard
                continue;
            }
            return scanner.nextInt();
        }
    }

    private void run() {
        scanner = new Scanner(System.in);
        int selected = 0;

        do {
            System.out.println("\n  .menuLevel : " + menuLevel);
            System.out.println("  .backIndex : " + backIndex);
            System.out.println("  .selected : " + selected);

            if (selected == backIndex) {
//                System.out.println("11111");
                //System.out.println("backIndex " + backIndex);
                menuLevel--;
                if(backIndex != -1) {
//                    System.out.println("222222");
                    selected = history.pop();
                    backIndex = -1;
                }//else selected = 0;
            } else {
                menuLevel++;
                history.push(selected);
            }

            //System.out.println("history: " + history);
            System.out.println("  ..menuLevel : " + menuLevel);
            System.out.println("  ..backIndex : " + backIndex);

            selected = select(selected);
            System.out.println("  ...selected : " + selected);
        } while (true);
    }

    public void showVoxel(){System.out.println("..showVoxel()");}
    public void showPoints(){System.out.println("..showPoints()");}
    public void quit(){System.out.println("..Bye bye!"); System.exit(1);}

    public static void main(String[] args) {
        ConsoleMenu cm = new ConsoleMenu();
        cm.run();
    }
}
