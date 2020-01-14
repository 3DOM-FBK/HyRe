package eu.it.fbk.threedom.pcFilter;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static boolean DEBUG;

    @Argument(required = true, metaVar = "First input file")
    File inFile;

    //@Argument(required = true, metaVar = "Second input file")
    //File inFile2;

    //@Argument(required = true, metaVar = "VoxelSide")
    //Float voxelSide;

    @Option(name = "-o", metaVar = "output")
    File outFile = new File("");

    @Option(name = "-w", metaVar = "overWrite")
    Boolean overWrite;

    private static String filePath;
    private static String fileName;

    // timer
    private static long start;
    private static long time;

//    @Option(name = "-p", metaVar = "animation period (ms)")
//    int animationPeriod = 0;

    private void parseArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll getQTB this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.print("Usage: PointCloud Filter");
            parser.printSingleLineUsage(System.err);
            System.err.println();

            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            System.err.print("animation period set to 0 means no animation");

            // print option sample. This is useful some time
            System.err.println("  Example: pcFilter "+parser.printExample(OptionHandlerFilter.ALL));
            System.exit(1);
        }
    }

    private void run() throws Exception {
        ///////////////////////////////////////////////////////
        // create an output file
        filePath = FilenameUtils.getFullPath(inFile.getPath());
        fileName = FilenameUtils.getBaseName(inFile.getPath());
        fileName = FilenameUtils.removeExtension(fileName);

        Path out = null;

        try {
            if (outFile.getName() == "")
                outFile = new File(FilenameUtils.removeExtension(inFile.getName()) + "_result.txt");

            out = Paths.get(outFile.toURI());
            Files.createFile(out);
        } catch (FileAlreadyExistsException foee) {
            if(overWrite == null || !overWrite) {
                System.out.println("\nWARNING! the output file already exists");
                System.exit(1);
            }else
                System.out.println("overWrite file");
        }


        ///////////////////////////////////////////////////////
        // use the random function to generate random points
//        generateRandomData(1000000);


        ///////////////////////////////////////////////////////
        // read all lines
        Path path = Paths.get(inFile.toURI());
        start = System.currentTimeMillis();
        List<String> data = Files.readAllLines(path);
        printElapsedTime(start, "..input file1 read");

        //start = System.currentTimeMillis();
        //Path path2 = Paths.get(inFile2.toURI());
        //List<String> data = Files.readAllLines(path, path2);
        //printElapsedTime(start, "..input file2 read");

        ///////////////////////////////////////////////////////
        // filter the data
        ///////////////////////////////////////////////////////
        start = System.currentTimeMillis();
        PcFilter pcf = new PcFilter(data);
        pcf.parseData(0.5f);
        //pcf.parseData(voxelSide);
        printElapsedTime(start, "..voxel grid created");

        System.out.println("\n" + pcf.toString());


        ///////////////////////////////////////////////////////
        // pick a random voxel to filter
//        Random rnd = new Random();
//        int i = rnd.nextInt(pcf.getNumVoxel());
//        start = System.currentTimeMillis();
//        ArrayList<Point> pointList = (ArrayList<Point>) pcf.getVoxelPoints(i++);
//        printElapsedTime(start, "..voxel points retrieved");
//
//        if(Main.DEBUG)
//            System.out.println("\nvoxel " + (i) + "\n\t" + pointList);


        ///////////////////////////////////////////////////////
        // pick first non empty voxel
        start = System.currentTimeMillis();
        int i = 0;
        ArrayList<Point> pointList = (ArrayList<Point>) pcf.getVoxelPoints(i++);
        while(pointList == null)
            pointList = (ArrayList<Point>) pcf.getVoxelPoints(i++);
        printElapsedTime(start, "..voxel points retrieved");

        if(Main.DEBUG)
            System.out.println("\nvoxel " + i + "\n\t" + pointList);



        ///////////////////////////////////////////////////////
        // pick a random voxel to filter

        // convert char array to list of strings
        List<String> dataOut = new ArrayList<>();

        start = System.currentTimeMillis();
        dataOut.add("//X Y Z R G B Intensity");
        dataOut.add("//filter voxel " + i);
        for(Point p : pointList){
            StringBuilder line = new StringBuilder();
            line.append(p.x + " " + p.y + " " + p.z + " ");
            //line.append(p.getR() + " " + p.getG() + " " + p.getB() + " ");
            line.append(255 + " " + 0 + " " + 0 + " ");
            line.append(1);

            dataOut.add(line.toString());
        }
        printElapsedTime(start, "..output written");


//        char[][] solution = pcf.solve();

//        for (int i = 0; i < solution.length; i++) {
//            StringBuilder line = new StringBuilder();
//            for (int j = 0; j < solution[0].length; j++) {
//                line.append(solution[i][j] + " ");
//            }
//            dataOut.add(line.toString());
//        }


        ///////////////////////////////////////////////////////
        // writing result
//        try {
//            if(outFile.getName() == "")
//                outFile = new File(FilenameUtils.removeExtension(inFile.getName()) + "_result.txt");
//
//            Path out = Paths.get(outFile.toURI());
//            Files.createFile(out);
            Files.write(out, dataOut);
//        }catch(FileAlreadyExistsException foee){
//            if(!overWrite) {
//                System.out.println("\nWARNING! the output file already exists");
//                System.exit(1);
//            }else
//                System.out.println("overWrite file");
//        }
    }

    private void generateRandomData(int numberOfPoints){
        start = System.currentTimeMillis();

        List<String> randomIn = new ArrayList<>();

        final float min = 0.0f;
        final float max = 3.0f;

        String header = "//X Y Z R G B Intensity";
        String fakeColorIntensity = " 255 255 255 1";

        randomIn.add(header);
        Random rn = new Random();

        for (int i=0; i < numberOfPoints; i++){
            float rndFX = min + rn.nextFloat() * (max - min);
            float rndFY = min + rn.nextFloat() * (max - min);
            float rndFZ = min + rn.nextFloat() * (max - min);

            randomIn.add(   String.valueOf(rndFX) + " " +
                    String.valueOf(rndFY) + " " +
                    String.valueOf(rndFZ) + fakeColorIntensity);
        }

        System.out.println("path: " + inFile.getParent());

        File randomFile = new File(inFile.getParent() + File.separator + "random.txt");

        Path randomOut = Paths.get(randomFile.toURI());
        try {
            if(!Files.exists(randomOut))
                Files.createFile(randomOut);
            Files.write(randomOut, randomIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        printElapsedTime(start, "..random cloud generated and written");
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%dh:%02dm:%02ds", h,m,s);
    }

    public void printElapsedTime(long start, String message){
        time = (System.currentTimeMillis() - start) / 1000;
        System.out.println(message + " (" + convertSecondsToHMmSs(time) + ")");
    }

    /**
     * Main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.DEBUG  = true;
        main.parseArgs(args);
        main.run();
    }

}
