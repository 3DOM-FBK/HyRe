package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pcFilter.utils.Stats;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    @Argument(index=0, required = true, metaVar = "file1") File inFile1;
    @Argument(index=1, required = true, metaVar = "file2") File inFile2;
    @Argument(index=2, required = true, metaVar = "voxelSide") Float voxelSide;
    @Option(name = "-o", aliases = { "--output" }, metaVar = "output") File outFile = new File("");
    @Option(name = "-w", aliases = { "--overwrite" }, metaVar = "overWrite") Boolean overWrite;
    @Option(name = "-v", aliases = { "--verbose" }, metaVar = "verbose") Boolean verbose;

    public static boolean DEBUG;
    private static final int RANDOM_POINTS_NUMBER = 1000000;
    private static final float RANDOM_POINTS_CUBE_SIZE = 10;
    private static final String RANDOM_FILE1_HEADER = "// X Y Z R G B Class Permeability";
    private static final String RANDOM_FILE2_HEADER = "// X Y Z Class Porosity";

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
                System.out.println("\nreading input files");
                if(overWrite)
                    System.out.println("..overWrite output file");
        }


        ///////////////////////////////////////////////////////
        // use the random function to generate random points
        ///////////////////////////////////////////////////////
        if ((fn1.toString() + fn2.toString()).equals("rnd1rnd2")) {
            generateRandomData(RANDOM_POINTS_NUMBER, 0);
            generateRandomData(RANDOM_POINTS_NUMBER, 1);
        }



        ///////////////////////////////////////////////////////
        // read all lines file 1 & 2
        ///////////////////////////////////////////////////////


        ///////////////////////////////////////////////////////
//        start = System.currentTimeMillis();
//        Path path = Paths.get(inFile1.toURI());
//        //long lineCount = Files.lines(path).count();
//        //System.out.println("..lines " + lineCount);
//        //printElapsedTime(start, "..number of lines read");
//
//        List<String> file1Data = Files.readAllLines(path);v
//        printElapsedTime(start, "..first file read");

//        Class cls = file1Data.getClass();
//        System.out.println("..the type of \"file1Data\" is: " + cls.getName());
//
//        start = System.currentTimeMillis();
//        path = Paths.get(inFile2.toURI());
//        path = Paths.get(inFile2.toURI());
//        List<String> file2Data = Files.readAllLines(path);
//        printElapsedTime(start, "..second file read");
        ///////////////////////////////////////////////////////


        ///////////////////////////////////////////////////////
        List<String> file1Data = new ArrayList<>();
        List<String> file2Data = new ArrayList<>();

        // read file 1
        start = System.currentTimeMillis();
        FileInputStream inputStream = new FileInputStream(inFile1);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        while (sc.hasNextLine())
            file1Data.add(sc.nextLine());
        printElapsedTime(start, "..first file read");

        // read file 2
        start = System.currentTimeMillis();
        inputStream = new FileInputStream(inFile2);
        sc = new Scanner(inputStream, "UTF-8");
        while (sc.hasNextLine())
            file2Data.add(sc.nextLine());
        printElapsedTime(start, "..second file read");
        ///////////////////////////////////////////////////////


        ///////////////////////////////////////////////////////
        // create the structure
        ///////////////////////////////////////////////////////
        start = System.currentTimeMillis();
        PcFilter pcf = new PcFilter(file1Data, file2Data, voxelSide);
        printElapsedTime(start, "..voxel grid created");


        ///////////////////////////////////////////////////////
        // pick a random voxel to filter
        ///////////////////////////////////////////////////////
        Random rnd = new Random();
        int vGridSize = pcf.getVGrid().getSize();
        int i = rnd.nextInt(vGridSize);


        ///////////////////////////////////////////////////////
        // postprocessing statistics
        ///////////////////////////////////////////////////////
        System.out.println("\n\nPOST-PROCESSING STATISTICS");
        ArrayList<Point> pointList, pointListRoofs, pointListFacades, pointListStreets;

        for(FileType ft : FileType.values()){
            start = System.currentTimeMillis();
            System.out.println("\n" + ft.name() + " cloud");
            for(PointClassification pc : PointClassification.values()){

                pointList = (ArrayList<Point>) pcf.getPoints(ft, i);

                if(pointList != null) {
                    System.out.println("..random voxel -> " + i);
                    if (Main.DEBUG)
                        System.out.println("...." + ft.name() + " points " + pointList);
                    else
                        System.out.println("...." + ft.name() + ": " + pointList.size() + " points");

                    pointList = (ArrayList<Point>) pcf.getPoints(ft, i, pc);
                    System.out.println("......" + pc.name() + ": " + pointList.size());
                }
            }
            printElapsedTime(start, "processed");
        }

        for(FileType ft : FileType.values()){
            start = System.currentTimeMillis();
            System.out.println("\n" + ft.name() + " cloud");
            for(PointClassification pc : PointClassification.values()){
                Set<Integer> voxelSet = pcf.getVGrid().getVoxels(ft, pc);
                if(Main.DEBUG)
                    System.out.println(".." + pc.name() + " points are contained in voxels " + voxelSet);
                else
                    System.out.println(".." + pc.name() + " points contained in " + voxelSet.size() + " voxels ");

                int numberOfPointsInVoxel_sum = 0;
                for(int v : voxelSet) {
                    pointList = (ArrayList<Point>) pcf.getPoints(ft, v);
                    numberOfPointsInVoxel_sum += pointList.size();
                    if(Main.DEBUG) {
                        System.out.println("..voxel " + v);//+ " " + pointList);
                        for (Point p : pointList) System.out.println("...." + p.toString());
                    }
                }
                System.out.println("..mean of voxel point density " + (numberOfPointsInVoxel_sum / voxelSet.size()));
            }
            printElapsedTime(start, "processed");
        }


        System.out.println("\nproperties statistics");

//        String[][] props = {    {"Permeability"},
//                                {"Porosity"}        };

        String[][] props = pcf.getProperties();

        HashMap data = pcf.getDataHm();

        start = System.currentTimeMillis();
        for(int k=0; k < props.length; k++) {
            String prop = props[k][0];

            ArrayList propValues = (ArrayList<Float>) data.get(prop);
            if(Main.DEBUG)
                System.out.println(".." + prop + " values (normalized) " + propValues);
            else
                System.out.println(".." + prop + " values (normalized) " + propValues.size() + " values");

            // transform arrayList to array
            float[] values = new float[propValues.size()];
            int n = 0;
            for (Object p : propValues)
                values[n++] = (float) p;

            float med = Stats.median(values, values.length);
            float mad = Stats.mad(values, values.length);
            System.out.println("....med: " + med + "\n....mad: " + mad + "\n....sigmaM: " + (mad * 1.4826));
        }
        printElapsedTime(start, "processed");

        for(FileType ft : FileType.values()) {
            System.out.println("\n.." + ft.name());
            start = System.currentTimeMillis();

            for(int k=0; k < props[0].length; k++) {
                String prop = props[ft.ordinal()][k];
                System.out.println("...." + prop);

                for (PointClassification pc : PointClassification.values()) {
                    Set<Integer> voxelSet = pcf.getVGrid().getVoxels(ft, pc);

                    // extract values from voxels
                    ArrayList propValues = new ArrayList();
                    for (int v : voxelSet) {
                        pointList = (ArrayList<Point>) pcf.getPoints(ft, v);
                        for (Point p : pointList)
                            propValues.add(p.getProp(prop));
                    }
                    if(Main.DEBUG)
                        System.out.println("......" + pc.name() + " (normalized) " + propValues);
                    else
                        System.out.println("......" + pc.name() + " (normalized) " + propValues.size() + " values");

                    // transform arrayList to array
                    float[] values = new float[propValues.size()];
                    values = new float[propValues.size()];
                    int n = 0;
                    for (Object p : propValues)
                        values[n++] = (float) p;

                    float med = Stats.median(values, values.length);
                    float mad = Stats.mad(values, values.length);
                    System.out.println("........med: " + med + "\n........mad: " + mad + "\n........sigmaM: " + (mad * 1.4826));
                }
            }
            printElapsedTime(start, "processed");
        }


        ///////////////////////////////////////////////////////
        // write output file
        ///////////////////////////////////////////////////////
        // convert char array to list of strings
        List<String> dataOut = new ArrayList<>();

        // TODO: separate output file in two files
//        start = System.currentTimeMillis();
//        dataOut.add(fileType == 0 ? RANDOM_FILE1_HEADER : RANDOM_FILE2_HEADER + " FileType");
//        dataOut.add("// filter voxel " + i);
//        for(Point p : pointList){
//            StringBuilder line = new StringBuilder();
//            line.append(p.x + " " + p.y + " " + p.z + " ");
//            line.append("255 0 0 " + p.getIntensity() + " " + p.getType() );
//
//            dataOut.add(line.toString());
//        }
//
//        Files.write(out, dataOut);
//        printElapsedTime(start, "..output written");
    }

    private void generateRandomData(int numberOfPoints, int type){
        start = System.currentTimeMillis();

        List<String> randomIn = new ArrayList<>();

        Random rn = new Random();

        randomIn.add(type == 0 ? RANDOM_FILE1_HEADER : RANDOM_FILE2_HEADER);
        for (int i=0; i < numberOfPoints; i++){
            float rndFX = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);
            float rndFY = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE / 15 - 0.0f);
            float rndFZ = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);

            int rndClassification = rn.nextInt(3);

            randomIn.add(   String.valueOf(rndFX) + " " +
                            String.valueOf(rndFY) + " " +
                            String.valueOf(rndFZ) + " " +
                            (type==0 ? "0 255 0 " : "") +
                            String.valueOf(rndClassification) + " " +
                            String.valueOf(rn.nextFloat()) );
        }

        fileName = type==0 ? "rnd1.txt" : "rnd2.txt";
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
