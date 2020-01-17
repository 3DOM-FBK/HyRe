package eu.fbk.threedom.pcFilter;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;

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

    @Argument(index=0, required = true, metaVar = "file1") File inFile1;
    @Argument(index=1, required = true, metaVar = "file2") File inFile2;
    @Argument(index=2, required = true, metaVar = "voxelSide") Float voxelSide;
    @Option(name = "-o", aliases = { "--output" }, metaVar = "output") File outFile = new File("");
    @Option(name = "-w", aliases = { "--overwrite" }, metaVar = "overWrite") Boolean overWrite;
    @Option(name = "-v", aliases = { "--verbose" }, metaVar = "verbose") Boolean verbose;

    public static boolean DEBUG;
    private static final int RANDOM_POINTS_NUMBER = 100000;
    private static final float RANDOM_POINTS_CUBE_SIZE = 8.0f;
    private static final String RANDOM_FILE_HEADER = "// X Y Z R G B FileType Intensity";
    private static final String RANDOM_FILE_FAKE_PROPERTIES_1 = " 0 255 0 1";
    private static final String RANDOM_FILE_FAKE_PROPERTIES_2 = " 0 0 255 1";

    private static String filePath, fileName, fn1, fn2;

    // timer
    private static long start;
    private static long time;

    private void parseArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        verbose = false;

        try {
            // parse the arguments.
            parser.parseArgument(args);

            DEBUG = verbose;

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll getQTB this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.print("Usage: pcFilter");
            parser.printSingleLineUsage(System.err);
            System.err.println();

            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            System.err.print("  voxelSide: the lenght of the voxel cube\n");

            // print option sample. This is useful some time
            System.err.println("\nExample:\n\n  pcFilter f1.txt f2.txt -v" + parser.printExample(OptionHandlerFilter.ALL));
            System.exit(1);
        }
    }

    private void run() throws Exception {
        ///////////////////////////////////////////////////////
        // create an output file
        ///////////////////////////////////////////////////////
        filePath = FilenameUtils.getFullPath(inFile1.getPath());

        if(filePath.isEmpty())
            filePath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath();
        System.out.println("filePath: " + filePath);

        fn1 = FilenameUtils.getBaseName(inFile1.getPath()); //System.out.println("fn1: " + fn1);
        fn2 = FilenameUtils.getBaseName(inFile2.getPath()); //System.out.println("fn2: " + fn2);

        Path out = null;

        try {
            if (outFile.getName() == "")
                outFile = new File(filePath + File.separator + fn1 + "_" + fn2 + ".txt");
            System.out.println("outFile: " + outFile);


            out = Paths.get(outFile.toURI());
            Files.createFile(out);
        } catch (FileAlreadyExistsException foee) {
            if (overWrite == null || !overWrite) {
                System.out.println("\nWARNING! the output file already exists");
                System.exit(1);
            } else
                System.out.println("..overWrite file");
        }


        ///////////////////////////////////////////////////////
        // use the random function to generate random points
        ///////////////////////////////////////////////////////
        if ((fn1.toString() + fn2.toString()).equals("rnd1rnd2")) {
            generateRandomData(RANDOM_POINTS_NUMBER, 1);
            generateRandomData(RANDOM_POINTS_NUMBER, 2);
        }


        ///////////////////////////////////////////////////////
        // read all lines file 1 & 2
        ///////////////////////////////////////////////////////
        start = System.currentTimeMillis();
        Path path = Paths.get(inFile1.toURI());
        List<String> file1Data = Files.readAllLines(path);
        path = Paths.get(inFile2.toURI());
        List<String> file2Data = Files.readAllLines(path);
        printElapsedTime(start, "..input files read");


        ///////////////////////////////////////////////////////
        // filter the data
        ///////////////////////////////////////////////////////
        start = System.currentTimeMillis();
        PcFilter pcf = new PcFilter(file1Data, file2Data, voxelSide);
        printElapsedTime(start, "..voxel grid created");

        if (Main.DEBUG)
            System.out.println("\n" + pcf.toString());


        ///////////////////////////////////////////////////////
        // pick a random voxel to filter
        ///////////////////////////////////////////////////////
        start = System.currentTimeMillis();
        Random rnd = new Random();
        int vGridSize = pcf.getVGrid().getSize();
        int i = rnd.nextInt(vGridSize);
        ArrayList<Point> pointList = (ArrayList<Point>) pcf.getPoints(i);

        while (pointList == null) {
            i = rnd.nextInt(vGridSize);
            pointList = (ArrayList<Point>) pcf.getPoints(i);
        }

        printElapsedTime(start, "..voxel points retrieved");

        if (Main.DEBUG)
            System.out.println("\nvoxel (" + i + ")\n\t" + pointList);


        ///////////////////////////////////////////////////////
        // write output file
        ///////////////////////////////////////////////////////
        // convert char array to list of strings
        List<String> dataOut = new ArrayList<>();

        start = System.currentTimeMillis();
        dataOut.add(RANDOM_FILE_HEADER);
        dataOut.add("// filter voxel " + i);
        for(Point p : pointList){
            StringBuilder line = new StringBuilder();
            line.append(p.x + " " + p.y + " " + p.z + " ");
            line.append("255 0 0 " + p.getType() + " " + p.getIntensity());

            dataOut.add(line.toString());
        }

        Files.write(out, dataOut);
        printElapsedTime(start, "..output written");
    }

    private void generateRandomData(int numberOfPoints, int fileType){
        start = System.currentTimeMillis();

        List<String> randomIn = new ArrayList<>();

        Random rn = new Random();

        // generate first random file
        randomIn.add(RANDOM_FILE_HEADER);
        for (int i=0; i < numberOfPoints; i++){
            float rndFX = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);
            float rndFY = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);
            float rndFZ = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);

            randomIn.add(   String.valueOf(rndFX) + " " +
                            String.valueOf(rndFY) + " " +
                            String.valueOf(rndFZ) + (fileType==1 ? RANDOM_FILE_FAKE_PROPERTIES_1 : RANDOM_FILE_FAKE_PROPERTIES_2));
        }

        fileName = fileType==1 ? "rnd1.txt" : "rnd2.txt";
        File rnd1 = new File(filePath + File.separator + fileName);
        Path rnd1_out = Paths.get(rnd1.toURI());

        try {
            if(!Files.exists(rnd1_out))
                Files.createFile(rnd1_out);
            Files.write(rnd1_out, randomIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        printElapsedTime(start, "..random cloud generated and written in " + fileName);
    }

    private static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%dh:%02dm:%02ds", h,m,s);
    }

    private void printElapsedTime(long start, String message){
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
