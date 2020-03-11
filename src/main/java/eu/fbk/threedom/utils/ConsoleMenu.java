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
//        System.out.println("  select(" + selected + ")");
        int sel;

        switch(menuLevel) {
            case 0: System.out.println("\nmenu:\n 1. print info\n 2. change parameters\n 3. quit");
                break;

            case 1:
                switch(selected){
                    case 1: System.out.println("\nprint info:\n 1. location\n 2. class\n 3. voxel\n 4. back");
                        sel = scanner.nextInt();
                        switch(sel){
                            case 1: System.out.println("insert location:");
                                String location = scanner.next();
                                System.out.println("location: " + location);

                                selected = 4; // jump to case 4
                                break;

                            case 2: System.out.println("insert class:");
                                int classType = scanner.nextInt();
                                System.out.println("class: " + classType);

                                selected = 4; // jump to case 4
                                break;

                            case 3: System.out.println("insert voxel:");
                                int voxel = scanner.nextInt();
                                System.out.println("voxel: " + voxel);

                                selected = 4; // jump to case 4
                                break;

                            case 4: selected = sel;
                                break;
                        }

                        backIndex = 4;
                        return selected;

                    case 2: System.out.println("\nchange parameters:\n 1. voxelSide\n 2. back ");
                        sel = scanner.nextInt();
                        switch(sel) {
                            case 1: System.out.println("insert new voxelSide:");
                                Float voxelSide = scanner.nextFloat();
                                System.out.println("voxelSide: " + voxelSide);

                                selected = 2; // jump to case 2
                                break;

                            case 2: selected = sel;
                                break;
                        }

                        backIndex = 2;
                        return selected;

                    case 3: quit();
                }
                backIndex = 3;
                break;
        }

        return scanner.nextInt();
    }

    private void run() {
        scanner = new Scanner(System.in);
        int selected = 0;

        do {
            if (selected == backIndex) {
//                System.out.println("backindex " + backIndex);
                menuLevel--;
                selected = history.pop();
                backIndex = -1;
            } else {
                menuLevel++;
                history.push(selected);
            }

//            System.out.println("history: " + history);
//            System.out.println("  menuLevel: " + menuLevel);

            selected = select(selected);
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
