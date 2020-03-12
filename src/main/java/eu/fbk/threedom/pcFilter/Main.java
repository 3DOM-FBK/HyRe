package eu.fbk.threedom.pcFilter;

import com.sun.javafx.scene.paint.GradientUtils;
import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import eu.fbk.threedom.utils.Combinator;
import eu.fbk.threedom.utils.Stats;
import javafx.scene.input.KeyCode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;
import java.util.stream.Stream;

public class Main {

    @Argument(index=0, required = true, metaVar = "file1") File inFile1;
    @Argument(index=1, required = true, metaVar = "file2") File inFile2;
    @Argument(index=2, required = true, metaVar = "voxelSide") Float voxelSide;
    @Option(name = "-o", aliases = { "--output" }, metaVar = "output") String outFile;
    @Option(name = "-w", aliases = { "--overwrite" }, metaVar = "overWrite") Boolean overWrite;
    @Option(name = "-v", aliases = { "--verbose" }, metaVar = "verbose") Boolean verbose;

    public static boolean DEBUG;
    private static final int RANDOM_POINTS_NUMBER = 1000;
    private static final float RANDOM_POINTS_CUBE_SIZE = 123456;
    private static final String RANDOM_FILE1_HEADER = "// X Y Z R G B Class NumberOfReturns PIntensity";
    private static final String RANDOM_FILE2_HEADER = "// X Y Z Class LIntensity dZVariance ScanAngleRank EchoRatio";

    private static String filePath, fileName, fn1, fn2;

    // timer
    private static long start;
    private File outFile1, outFile2;
    public static JSONObject config;

    private static PcFilter pcf;
    private HashMap<String, Float> voxelDensityStats;

    private static Set<Integer> intersectionSet;
    private static Set<Integer> filteredIntersectionSet;
    private static Set<Integer> scoredFilteredIntersectionSet;

    private Scanner scanner;
    private int menuLevel;
    private int backIndex;
    private Stack<Integer> history;

    private void parseArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        verbose = false;

        try {
            // parse the arguments.
            parser.parseArgument(args);

            DEBUG = verbose;
            config = null;
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
            System.err.println("\nExample:\n\n  pcFilter f1.txt f2.txt 1.0f -v" + parser.printExample(OptionHandlerFilter.ALL));
            System.exit(1);
        }
    }

    private void createPcFilter(float voxelSide){
        ///////////////////////////////////////////////////////
        // create the structure
        ///////////////////////////////////////////////////////
        //start = System.currentTimeMillis();
        pcf = new PcFilter(inFile1, inFile2, voxelSide);
        //Stats.printElapsedTime(start, "..voxel grid created");
    }

    private void printStatistics(){
        ///////////////////////////////////////////////////////////////////////
        // PRINT PROPERTIES STATISTICS
        System.out.println("\n///////////////////////////////////////////////////////\n// PROPERTIES STATISTICS");

        ArrayList<Point> pointList;
        String[][] props = pcf.getProperties();

        // EVALUATE PROPERTY MED/MAD
        for(FileType ft : FileType.values()) {
            //System.out.println("\n.." + ft.name());
            start = System.currentTimeMillis();

            for(int p=0; p < props[ft.ordinal()].length; p++) {
                String prop = props[ft.ordinal()][p];

                //ArrayList propValues = (ArrayList<Float>) data.get(prop);
                List<Point> points = (voxelSide != 0) ? pcf.getPoints(ft, true) : pcf.getPoints(ft, false);

                // transform arrayList to array
                float[] values = new float[points.size()];

                if(values.length == 0) break;

                int n = 0;
//                for (Object v : propValues)
                for (Point pnt : points)
                    values[n++] = pnt.getNormProp(p);

                if (Main.DEBUG)
                    System.out.println(".." + prop + " values (normalized) " + Arrays.toString(values));
                else
                    System.out.println(".." + prop + " values (normalized) " + values.length + " values");

                float med = Stats.median(values, values.length);
                float mad = Stats.mad(values, values.length);
                System.out.println("....med: " + med + "\n....mad: " + mad
                        + "\n....sigmaM: " + (mad * 1.4826)
                        + "\n....3sigmaM: " + 3 * (mad * 1.4826));
            }
            Stats.printElapsedTime(start, "processed");
        }

        // EVALUATE PROPERTY MED/MAD (CLASS)
        for(FileType ft : FileType.values()) {
            System.out.println("\n.." + ft.name());
            start = System.currentTimeMillis();

            for(int k=0; k < props[ft.ordinal()].length; k++) {
                String prop = props[ft.ordinal()][k];
                System.out.println("...." + prop);

                for (PointClassification pc : PointClassification.values()) {

                    ArrayList propValues = new ArrayList();
                    if(voxelSide != 0) {
                        Set<Integer> voxelSet = pcf.getVGrid().getVoxels(ft, pc);

                        if(voxelSet == null) continue;

                        // extract values from voxels
                        for (int v : voxelSet) {
                            // list of points of fileType ft, in voxel v, of class pc
                            pointList = (ArrayList<Point>) pcf.getPoints(ft, v, pc);
                            for (Point p : pointList)
//                            propValues.add(p.getProp(prop));
                                propValues.add(p.getNormProp(k));

                        }
                    }else{
                        pointList = (ArrayList<Point>) pcf.getPoints(ft, pc);
                        for (Point p : pointList)
                            propValues.add(p.getNormProp(k));
                    }

                    if(propValues.isEmpty()) break;

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
                    System.out.println("........med: " + med + "\n........mad: " + mad
                            + "\n........sigmaM: " + (mad * 1.4826)
                            + "\n........3sigmaM: " + 3*(mad * 1.4826) );
                }
            }

            Stats.printElapsedTime(start, "processed");
        }
    }

    private void printVoxelDensity(){
        ///////////////////////////////////////////////////////
        // AVERAGE VOXEL DENSITY
        System.out.println("\n///////////////////////////////////////////////////////\n// VOXEL DENSITY");

        ArrayList<Point> pointList;
        voxelDensityStats = new HashMap<>();

        int numberOfPointsInVoxel_sum;

        // cycle on photogrammetry/lidar file
        for (FileType ft : FileType.values()) {
            start = System.currentTimeMillis();
            System.out.println("\n" + ft.name() + " cloud");

            ///////////////////////////////////////////////////////
            // evaluate average per class voxel density
            for (PointClassification pclass : PointClassification.values()) {
                numberOfPointsInVoxel_sum = 0;
                Set<Integer> voxelSet = pcf.getVGrid().getVoxels(ft, pclass);

                if(voxelSet == null) continue;

                if (Main.DEBUG)
                    System.out.println(".." + pclass.name() + " points are contained in voxels " + voxelSet);
                else
                    System.out.println(".." + pclass.name() + " points contained in " + voxelSet.size() + " voxels ");

                // cycle on voxel to evaluate mean
                float mean = 0;
                for (int v : voxelSet) {
                    pointList = (ArrayList<Point>) pcf.getPoints(ft, v, pclass, false);
                    numberOfPointsInVoxel_sum += pointList.size();

                    // filetype_f class_i voxel_v density
                    voxelDensityStats.put(ft.name() + "_" + pclass.name() + "_v" + v + "_density", (float)pointList.size());

                    if (Main.DEBUG) {
                        System.out.println("....voxel " + v);//+ " " + pointList);
                        for (Point p : pointList) System.out.println("......" + p.toString(pcf.getCoordShift()));
                    }
                }
                if(voxelSet.size() > 0)
                    mean = (float)numberOfPointsInVoxel_sum / voxelSet.size();
                System.out.println("....mean of voxel point density " + mean);

                // cycle on voxel to evaluate std
                float std = 0;
                for (int v : voxelSet) {
                    pointList = (ArrayList<Point>) pcf.getPoints(ft, v, pclass, false);
                    std +=  Math.pow((pointList.size() - mean), 2);
                }
                if(voxelSet.size() > 0)
                    std = (float)Math.sqrt(std / voxelSet.size());
                System.out.println("....std of voxel point density " + std);

                voxelDensityStats.put(ft.name() + "_" + pclass.name() + "_density_mean", mean);
                voxelDensityStats.put(ft.name() + "_" + pclass.name() + "_density_std", std);

//                    System.out.println("C0_density_mean: " + voxelDensityStats.get("C0_density_mean"));
//                    System.out.println("C0_density_std: " + voxelDensityStats.get("C0_density_std"));
            }
            Stats.printElapsedTime(start, "processed");


            ///////////////////////////////////////////////////////
            // evaluate average voxel density
            start = System.currentTimeMillis();
            Set<Integer> voxelSet = pcf.getVGrid().getVoxels(ft);
            if(voxelSet == null) continue;

            if (Main.DEBUG)
                System.out.println("\n..points are contained in voxels " + voxelSet);

            numberOfPointsInVoxel_sum = 0;

            // cycle on voxel to evaluate mean
            for (int v : voxelSet) {
                pointList = (ArrayList<Point>) pcf.getPoints(ft, v);

                numberOfPointsInVoxel_sum += pointList.size();
                if (Main.DEBUG) {
                    System.out.println("..voxel " + v);//+ " " + pointList);
                    for (Point p : pointList) System.out.println("...." + p.toString(pcf.getCoordShift()));
                }
            }
            float mean = (float)numberOfPointsInVoxel_sum / voxelSet.size();
            System.out.println("..mean of voxel point density " + mean);

            // cycle on voxel to evaluate std
            float std = 0;
            for (int v : voxelSet) {
                pointList = (ArrayList<Point>) pcf.getPoints(ft, v);
                std +=  (float)Math.pow((pointList.size() - mean), 2);
            }
            std = (float)Math.sqrt(std / voxelSet.size());
            System.out.println("..std of voxel point density " + std);

            voxelDensityStats.put(ft.name() + "_" + "density_mean", mean);
            voxelDensityStats.put(ft.name() + "_" + "density_std", std);

            Stats.printElapsedTime(start, "processed");
        }

    }

    private void printMultiFileTypeVoxels(){
        ///////////////////////////////////////////////////////
        // PHOTO/LIDAR INTERSECTION IN EACH VOXEL
        System.out.println("\n///////////////////////////////////////////////////////\n// PHOTO/LIDAR INTERSECTION IN EACH VOXEL");

        start = System.currentTimeMillis();
        // initialize with all the voxels
        intersectionSet = pcf.getVGrid().getVoxels( new FileType[] {FileType.PHOTOGRAMMETRIC,FileType.LIDAR} );
        // cycle on photogrammetry/lidar file and find the intersection
        for (FileType ft : FileType.values()) {
            if(Main.DEBUG)
                System.out.println(".." + ft + " set " + pcf.getVGrid().getVoxels(ft));
            else
                System.out.println(".." + ft + " " + pcf.getVGrid().getVoxels(ft).size() + " voxels");
            intersectionSet.retainAll(pcf.getVGrid().getVoxels(ft));
        }


        if(Main.DEBUG)
            System.out.println("photo/lidar voxels sets intersection set " + intersectionSet.toString());
        else
            System.out.println("photo/lidar points are contained in " +
                    + intersectionSet.size() + " voxels");

        if(Main.DEBUG)
            for (int v : intersectionSet) {
                System.out.println("..voxel " + v);
                //System.out.println("....points " + pcf.getVGrid().getPoints(v));
            }
        Stats.printElapsedTime(start, "processed");
    }

    private void printMultiClassVoxels(){
        ///////////////////////////////////////////////////////
        // MULTICLASS IN EACH INTERSECTION VOXEL
        System.out.println("\n///////////////////////////////////////////////////////\n// MULTICLASS IN EACH INTERSECTION VOXEL");

        String[] classes = Stream.of(PointClassification.values()).map(PointClassification::name).toArray(String[]::new);
        //for(String cls : classes) System.out.println(cls);

        // all two places combinations
        List<String[]> combinations = Combinator.generate(classes, 2);
        // add the 3 places case
        combinations.add(classes);

        // cycle on photogrammetry/lidar file
        for (FileType ft : FileType.values()) {
            System.out.println("...." + ft + " cloud");

            start = System.currentTimeMillis();

            for(String[] combination : combinations)
                System.out.println("......" + Arrays.toString(combination)
                        + " combination in " + pcf.getVGrid().getVoxels(ft, combination).size() + " voxels");

            Stats.printElapsedTime(start, "processed");
        }
    }

    private void printFilteredVoxels(){
        ////////////////////////////////////////////////////////////////////////////////
        // FILTERED INTERSECTION SET
        System.out.println("\n///////////////////////////////////////////////////////\n// FILTERED INTERSECTION SET");

        System.out.println("photo/lidar intersection voxels where " +
                "at least one class for each -> filetype voxel density >= voxel density mean");

        filteredIntersectionSet = new TreeSet<>();
        start = System.currentTimeMillis();

        for (int v : intersectionSet) {
            if(Main.DEBUG)
                System.out.println("..v" + v);
            boolean passed = true;
            for (PointClassification pclass : PointClassification.values()) {
                if(Main.DEBUG)
                    System.out.println("...." + pclass);
                for (FileType ft : FileType.values()) {
                    if(Main.DEBUG)
                        System.out.println("......" + ft);

                    float ftClVDensity = 0, ftClVDensityMean = 0;
                    if (voxelDensityStats.containsKey(ft + "_" + pclass + "_v" + v + "_density"))
                        ftClVDensity = voxelDensityStats.get(ft + "_" + pclass + "_v" + v + "_density");

                    if (voxelDensityStats.containsKey(ft + "_" + pclass + "_density_mean"))
                        ftClVDensityMean = voxelDensityStats.get(ft + "_" + pclass + "_density_mean");

                    //System.out.println("........ftClVDensity " + ftClVDensity);
                    //System.out.println("........ftClVDensityMean " + ftClVDensityMean);

                    if (ftClVDensityMean == 0 || ftClVDensity < ftClVDensityMean) {
                        passed = false; //System.out.println("one ft fails");
                        break;
                    } else passed = true; //System.out.println("one ft succeed");
                }

                if(passed){
                    filteredIntersectionSet.add(v);
                    if(Main.DEBUG)
                        System.out.println("....OK");
                    break;
                }
            }
        }

        if(Main.DEBUG)
            System.out.println(".." + filteredIntersectionSet.toString());
        else
            System.out.println(".." + filteredIntersectionSet.size() + " voxels");

        Stats.printElapsedTime(start, "processed");
    }

    private void printScoredFilteredVoxels(){
        ////////////////////////////////////////////////////////////////////////////////
        // SCORED FILTERED INTERSECTION SET
        System.out.println("\n///////////////////////////////////////////////////////\n// SCORED FILTERED INTERSECTION SET");

        ArrayList<Point> pointList;
        scoredFilteredIntersectionSet = new TreeSet<>();
        start = System.currentTimeMillis();

        System.out.println("photo/lidar intersection voxels where " +
                "at least one class for each filetype -> voxel density >= voxel density mean - std");
        for (Integer v : filteredIntersectionSet) {
            if(Main.DEBUG)
                System.out.println("..v" + v);
            boolean passed = true;
            for (PointClassification pclass : PointClassification.values()) {
                if(Main.DEBUG)
                    System.out.println("...." + pclass);
                for (FileType ft : FileType.values()) {
                    if(Main.DEBUG)
                        System.out.println("......" + ft);
                    pointList = (ArrayList<Point>) pcf.getPoints(ft, v, pclass, true);
                    float density_mean = voxelDensityStats.get(ft + "_" + pclass + "_density_mean");
                    float density_std = voxelDensityStats.get(ft.name() + "_" + pclass.name() + "_density_std");

                    //System.out.println("........pointList.size() " + pointList.size());
                    //System.out.println("........density_mean - density_std " + (density_mean - density_std));

                    if (pointList.size() < (density_mean /*- density_std*/)) {
                        passed = false; //System.out.println("one ft fails");
                        break;
                    } else passed = true; //System.out.println("one ft succeed");
                }

                if (passed) {
                    scoredFilteredIntersectionSet.add(v);
                    if(Main.DEBUG)
                        System.out.println("....OK");
                    break;
                }
            }
        }

        if (Main.DEBUG)
            System.out.println(".." + scoredFilteredIntersectionSet.toString());
        else
            System.out.println(".." + scoredFilteredIntersectionSet.size() + " voxels");

        Stats.printElapsedTime(start, "processed");
    }

    private void run() throws Exception {
        ///////////////////////////////////////////////////////
        // create an output file
        ///////////////////////////////////////////////////////
        filePath = FilenameUtils.getFullPath(inFile1.getPath());

        if(filePath.isEmpty())
            filePath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath();
        System.out.println("filePath:\n.." + filePath);

        fn1 = FilenameUtils.getBaseName(inFile1.getPath()); //System.out.println("fn1: " + fn1);
        fn2 = FilenameUtils.getBaseName(inFile2.getPath()); //System.out.println("fn2: " + fn2);

        ///////////////////////////////////////////////////////
        // read the config file
        File jsonfile = new File(filePath + File.separator + "config.json");
        readThresholdJson(jsonfile);

        ///////////////////////////////////////////////////////
        // use the random function to generate random points
        ///////////////////////////////////////////////////////
        if ((fn1.toString() + fn2.toString()).equals("rnd1rnd2")) {
            generateRandomData(RANDOM_POINTS_NUMBER, 0);
            generateRandomData(RANDOM_POINTS_NUMBER, 1);
        }

        ///////////////////////////////////////////////////////
        // create the structure
        ///////////////////////////////////////////////////////
        createPcFilter(voxelSide);

        ///////////////////////////////////////////////////////
        // SHOW DATA
        ///////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        // PRINT PROPERTIES STATISTICS
        printStatistics();


        if(voxelSide != 0) {
//            ///////////////////////////////////////////////////////
//            // pick a random voxel to filter
//            /////////////////////////////////////////////////////
//            Random rnd = new Random();
//            int vGridSize = pcf.getVGrid().getSize();
//            int i = rnd.nextInt(vGridSize);

//            ///////////////////////////////////////////////////////
//            // Check POINT BELONGING TO SPECIFIC VOXEL
//            System.out.println("\n\nPOST-PROCESSING STATISTICS");
//
//            for(int i=0; i < pcf.getVGrid().getSize(); i++) {
//
//                // TODO: temporary do only PHOTOGRAMMETRIC or LIDAR
//                FileType ft = FileType.PHOTOGRAMMETRIC;
//                //FileType ft = FileType.LIDAR;
//                //for (FileType ft : FileType.values()) {
//                    start = System.currentTimeMillis();
//                    System.out.println("\n" + ft.name() + " cloud");
//                    System.out.println("..voxel -> " + i);
//
//                    pointList = (ArrayList<Point>) pcf.getPoints(ft, i);
//                    if (pointList != null)
//                        if (Main.DEBUG) {
//                            for(Point p : pointList)
//                                System.out.println("......" + p.toString(pcf.getCoordShift()));
//                            //System.out.println("...." + ft.name() + " points " + pointList);
//                        }else
//                            System.out.println("...." + ft.name() + ": " + pointList.size() + " points");
//
//                    for (PointClassification pc : PointClassification.values()) {
//                        pointList = (ArrayList<Point>) pcf.getPoints(ft, i, pc);
//                        if (pointList != null) System.out.println("......" + pc.name() + ": " + pointList.size());
//
//                    }
//                    Stats.printElapsedTime(start, "processed");
//                //}
//            }

            ///////////////////////////////////////////////////////
            // AVERAGE VOXEL DENSITY
            printVoxelDensity();

            ///////////////////////////////////////////////////////
            // PHOTO/LIDAR INTERSECTION IN EACH VOXEL
            printMultiFileTypeVoxels();

            ///////////////////////////////////////////////////////
            // MULTICLASS IN EACH INTERSECTION VOXEL
            printMultiClassVoxels();

            ////////////////////////////////////////////////////////////////////////////////
            // FILTERED INTERSECTION SET
            printFilteredVoxels();

            ////////////////////////////////////////////////////////////////////////////////
            // SCORED FILTERED INTERSECTION SET
            printScoredFilteredVoxels();
        }

        /////////////////////////////////////////////
        // WRITE DATA
        ////////////////////////////////////////////
        if(voxelSide == 0)
            writeOutput(pcf.getPoints(), "out");
        else {
            // write in output files
            writeOutput(filteredIntersectionSet, false, "filteredIntersection");
            writeOutput(scoredFilteredIntersectionSet, true, "out");
        }

        /////////////////////////////////////////////
        // INTERACTIVE CONSOLE
        ////////////////////////////////////////////
        scanner = new Scanner(System.in);
        menuLevel = -1;
        backIndex = -1;
        int selected = 0;
        history = new Stack<>();

        do {
//            System.out.println("\n  .menuLevel : " + menuLevel);
//            System.out.println("  .backIndex : " + backIndex);
//            System.out.println("  .selected : " + selected);

            if (selected == backIndex) {
                menuLevel--;
                if(backIndex != -1) {
                    selected = history.pop();
                    backIndex = -1;
                }
            } else {
                menuLevel++;
                history.push(selected);
            }

//            System.out.println("  ..menuLevel : " + menuLevel);
//            System.out.println("  ..backIndex : " + backIndex);

            selected = printMenu(selected);
//            System.out.println("history: " + history);
//            System.out.println("  ...selected : " + selected);
        } while (true);
    }

    public void quit(){System.out.println("..Bye bye!"); System.exit(1);}

    public void printLocation(Point location){
        int voxel;

        voxel = pcf.getVoxelId(location.subPoint(pcf.getCoordShift()));

        if(voxel == -1)
            System.out.println("..the location " + location.toString() + " is outside of the bounding box");
        else
            System.out.println("..the location " + location.toString() + " falls into voxel " + voxel);
    }

    public void printPointsInVoxel(int voxel, boolean verbose){
        List<Point> points;

        for (FileType ft : FileType.values()) {
            System.out.println("\n.." + ft);

            if(verbose) {
                for (PointClassification pclass : PointClassification.values()) {
                    points = pcf.getPoints(ft, voxel, pclass);
                    //check it points is null
                    if (points == null || points.size() == 0) continue;

                    System.out.println("...." + pclass.name());

                    for (Point p : points)
                        System.out.println("......" + p.toString(pcf.getCoordShift()));
                }
            }else {
                points = pcf.getPoints(ft, voxel, false);
                //check it points is null
                if (points == null || points.size() == 0) continue;

                System.out.println("...." + points.size() + " points");
            }
        }
    }

    public void printPointsInClass(PointClassification pclass, boolean verbose){
        List<Point> points;

        for (FileType ft : FileType.values()) {
            System.out.println("\n.." + ft);

            if (verbose) {
                Set<Integer> voxelSet = pcf.getVGrid().getVoxels(ft, pclass);

                if(voxelSet == null || voxelSet.size() == 0) continue;

                // cycle on voxels
                for (int v : voxelSet) {
                    points = (ArrayList<Point>) pcf.getPoints(ft, v, pclass, false);

                    if(points == null || points.size() == 0) continue;

                    System.out.println("..voxel " + v);
                    for (Point p : points)
                        System.out.println("...." + p.toString(pcf.getCoordShift()));
                }
            }else {
                points = pcf.getPoints(ft, pclass);
                //check it points is null
                if (points == null || points.size() == 0 ) continue;

                System.out.println("...." + points.size() + " points");
            }
        }
    }

    public int printMenu(int selected){
        //System.out.println("  printMenu(" + selected + ")");
        int sel;
        boolean error = false;
        String yn;
        boolean verbose;

        switch(menuLevel) {
            case 0: System.out.println("\nmenu:\n 1. print info\n 2. change parameters\n 3. quit");
                break;

            case 1:
                switch(selected){
                    case 1:
                        while (true) {
                            System.out.println("\nprint info:\n 1. location\n 2. points in class\n 3. points in voxel\n 4. back");
                            if (!scanner.hasNextInt()) {
                                System.out.println("only integers allowed! ");
                                scanner.next(); // discard
                                continue;
                            }
                            sel = scanner.nextInt();

                            switch (sel) {
                                case 1:
                                    String location;// = scanner.next();
                                    String[] coords;

                                    do {
                                        System.out.println("enter location (x,y,z) :");
                                        location = scanner.next(); //System.out.println("location: " + location);

                                        error = false;
                                        if (location.isEmpty()) error = true;
                                        coords = location.split(",");
                                        if (coords.length != 3) error = true;
                                    } while (error);

                                    Point point = new Point(Double.parseDouble(coords[0]),
                                            Double.parseDouble(coords[1]),
                                            Double.parseDouble(coords[2]));

                                    printLocation(point);
                                    break;

                                case 2:
                                    int classType = -1;

                                    // check integer
                                    do {
                                        error = false;
                                        System.out.println("enter class (integer): ");
                                        if (!scanner.hasNextInt()) {
                                            System.out.println("only integers allowed! ");
                                            scanner.next(); // discard
                                            error = true;
                                            continue;
                                        }
                                        classType = scanner.nextInt();

                                        // check if class exsist
                                        if (PointClassification.parse(classType) == null) {error = true; continue;}

                                        break;
                                    } while (error);

                                    do {
                                        System.out.println("print verbose (y/n): ");
                                        yn = scanner.next();

                                        error = false;
                                        if (!yn.equals("y") && !yn.equals("n")) error = true;
                                    } while (error);

                                    verbose = yn.equals("y") ? true : false;

                                    //TODO: add verbose option
                                    printPointsInClass(PointClassification.parse(classType), verbose);
                                    //System.out.println("class " + PointClassification.parse(classType).name() + " ok now what?");

                                    break;

                                case 3:
                                    int voxel = -1;

                                    // check integer
                                    do{
                                        error = false;
                                        System.out.println("enter voxel (integer): ");
                                        if (!scanner.hasNextInt()) {
                                            System.out.println("only integers allowed! ");
                                            scanner.next(); // discard
                                            error = true;
                                            continue;
                                        }
                                        voxel = scanner.nextInt();

                                        // check if voxel exsist
                                        //System.out.println(pcf.getVGrid().getSize());
                                        if (voxel < 0 || voxel >= pcf.getVGrid().getSize()) {
                                            System.out.println("the voxel doens't exsist!");
                                            error = true;
                                            continue;
                                        }

                                        break;
                                    }while(error);

                                    do {
                                        System.out.println("print verbose (y/n): ");
                                        yn = scanner.next();

                                        error = false;
                                        if (!yn.equals("y") && !yn.equals("n")) error = true;
                                    } while (error);

                                    verbose = yn.equals("y") ? true : false;
                                    printPointsInVoxel(voxel, verbose);
                                    break;

                                case 4:
                                    history.clear();
                                    menuLevel = -1;
                                    return selected;

                                default: System.out.println("print info: no menu selection available! "); continue;
                            }
                            break;
                        }

                        selected = history.pop();
                        menuLevel--;
                        return selected;

                    case 2:
                        while (true) {
                            System.out.println("\nchange parameters:\n 1. voxelSide\n 2. back ");
                            if (!scanner.hasNextInt()) {
                                System.out.println("only integers allowed!");
                                scanner.next(); // discard
                                continue;
                            }
                            sel = scanner.nextInt();

                            switch (sel) {
                                case 1:
                                    Float voxelSide;
                                    for(;;) {
                                        System.out.println("enter voxelSide (float): ");
                                        if (!scanner.hasNextFloat()) {
                                            scanner.next(); // discard
                                            continue;
                                        }
                                        voxelSide = scanner.nextFloat();
                                        break;
                                    }

                                    //TODO: recompute voxelSide
                                    createPcFilter(voxelSide);
                                    break;

                                case 2:
                                    history.clear();
                                    menuLevel = -1;
                                    return selected;

                                default: System.out.println("change parameters: no menu selection available! "); continue;
                            }
                            break;
                        }

                        selected = history.pop();
                        menuLevel--;
                        return selected;

                    case 3: quit();

                }
                history.pop();
                System.out.println("no menu selection available!");
                return -1;
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

    public void showVoxel(){
        System.out.println("\nat which coordinates [x, y, z] ?");
        int voxel;
        boolean error = true;
        String[] coords = null;

        while(error) {
            String choiche = scanner.nextLine();
            if(choiche.isEmpty()) continue;

            coords = choiche.split(",");
            if(coords.length != 3) {
                System.out.println("not a valid point");
                return;
            }

            error = false;
        }

        Point point = new Point(Double.parseDouble(coords[0]),
                            Double.parseDouble(coords[1]),
                            Double.parseDouble(coords[2]));

        voxel = pcf.getVoxelId(point.subPoint(pcf.getCoordShift()));

        if(voxel == -1)
            System.out.println("point is outside the bounding box");
        else
            System.out.println("in voxel " + voxel);
    }

    public void showPoints(boolean verbose){
        System.out.println("\nin which voxel?");

        List<Point> points;

        int choiche = scanner.nextInt();
        if(choiche < 0 || choiche > pcf.getVGrid().getSize()) {
            System.out.println("the voxel " + choiche + " doesn't exist");
            //showMainMenu();
        }

        for (FileType ft : FileType.values()) {
            System.out.println(ft);
            for (PointClassification pclass : PointClassification.values()) {

                points = pcf.getPoints(ft, choiche, pclass, false);

                if(points.size() > 0) {
                    if(verbose) {
                        System.out.println(".." + pclass);

                        for (Point p : points)
                            System.out.println("...." + p.toString(pcf.getCoordShift()));
                    }else{
                        System.out.println(".." + pclass + " -> " + points.size() + " points");
                    }
                }
            }
        }
    }

    public void writeOutput(List<Point> points, String label){
        Path out1 = null, out2 = null;

        try {
            if (outFile == null) {
                outFile1 = new File(filePath + File.separator + fn1 + "_" + label + ".txt");
                outFile2 = new File(filePath + File.separator + fn2 + "_" + label + ".txt");
            }
            //outFile = new File(filePath + File.separator + fn1 + "_" + fn2 + ".txt");
            System.out.println("\noutFile:\n.." + outFile1 + "\n.." + outFile2);

            out1 = Paths.get(outFile1.toURI()); Files.createFile(out1);
            out2 = Paths.get(outFile2.toURI()); Files.createFile(out2);

        } catch (IOException foee) {
            if (overWrite == null || !overWrite) {
                System.out.println("\nWARNING! the output file already exists");
                System.exit(1);
            }

            if(overWrite) System.out.println("..overWrite output file");
        }

        // write the output files
        BufferedWriter writer1 = null, writer2 = null;
        try {
            writer1 = new BufferedWriter(new FileWriter(outFile1, false));
            writer2 = new BufferedWriter(new FileWriter(outFile2, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw;


        System.out.println("\nWrite files");
        start = System.currentTimeMillis();
        for(FileType ft : FileType.values()) {
            //System.out.println(".." + ft);

            String headerStr = Arrays.toString(pcf.getHeader(ft)).replaceAll(",", "");
            //headerStr = headerStr.substring(1, headerStr.length()-1); // remove square brackets []
            headerStr = headerStr.replace("[", "// ");
            headerStr = headerStr.replace("]", "");

            headerStr += " score";

            System.out.println(".." + ft + " header: " + headerStr);

            bw = (ft == FileType.PHOTOGRAMMETRIC) ? writer1 : writer2;
            try {
                bw.write(headerStr + "");
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!points.isEmpty())
            for (Point p : points) {
//                        System.out.println("......point " + p.toString(pcf.getCoordShift()));
//                        System.out.println("........score " + p.getScore());
//                        System.out.println("........threshold " + p.getThreshold());

                try {
                    bw = (p.getType() == FileType.PHOTOGRAMMETRIC) ? writer1 : writer2;
                    // SELECT true if you want normalized values
                    bw.write(p.toStringOutput(false, pcf.getCoordShift()));
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        Stats.printElapsedTime(start, (label + " files written"));

        try {
            writer1.close();
            writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeOutput(Set<Integer> voxels, boolean scoreCheck, String label){
        Path out1 = null, out2 = null;

        try {
            if (outFile == null) {
                outFile1 = new File(filePath + File.separator + fn1 + "_" + label + ".txt");
                outFile2 = new File(filePath + File.separator + fn2 + "_" + label + ".txt");
            }
            //outFile = new File(filePath + File.separator + fn1 + "_" + fn2 + ".txt");
            System.out.println("\noutFile:\n.." + outFile1 + "\n.." + outFile2);

            out1 = Paths.get(outFile1.toURI()); Files.createFile(out1);
            out2 = Paths.get(outFile2.toURI()); Files.createFile(out2);

        } catch (IOException foee) {
            if (overWrite == null || !overWrite) {
                System.out.println("\nWARNING! the output file already exists");
                System.exit(1);
            }

            if(overWrite) System.out.println("..overWrite output file");
        }

        // write the output files
        BufferedWriter writer1 = null, writer2 = null;
        try {
            writer1 = new BufferedWriter(new FileWriter(outFile1, false));
            writer2 = new BufferedWriter(new FileWriter(outFile2, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw;


        System.out.println("\nWrite files");
        start = System.currentTimeMillis();
        for(FileType ft : FileType.values()) {
            //System.out.println(".." + ft);

            String headerStr = Arrays.toString(pcf.getHeader(ft)).replaceAll(",", "");
            //headerStr = headerStr.substring(1, headerStr.length()-1); // remove square brackets []
            headerStr = headerStr.replace("[", "// ");
            headerStr = headerStr.replace("]", "");

            headerStr += " score";

            System.out.println(".." + ft + " header: " + headerStr);

            bw = (ft == FileType.PHOTOGRAMMETRIC) ? writer1 : writer2;
            try {
                bw.write(headerStr + "");
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<Point> points = null;

            for(Integer v : voxels) {

                points = pcf.getPoints(ft, v, scoreCheck);

                if (!points.isEmpty())
                    for (Point p : points) {
//                        System.out.println("......point " + p.toString(pcf.getCoordShift()));
//                        System.out.println("........score " + p.getScore());
//                        System.out.println("........threshold " + p.getThreshold());

                        try {
                            // SELECT true if you want normalized values
                            bw.write(p.toStringOutput(false, pcf.getCoordShift()));
                            bw.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }

        Stats.printElapsedTime(start, (label + " files written"));

        try {
            writer1.close();
            writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readThresholdJson(File file){
        System.out.println("\nreading " + file.getPath());
        StringBuilder sb = new StringBuilder();
        String str = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            start = System.currentTimeMillis();
            while ((str = reader.readLine()) != null)
                sb.append(str);

            str = sb.toString();

            Stats.printElapsedTime(start, "file read");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read the input JSON file
        config = new JSONObject(str);
        JSONArray fileTypes = config.getJSONArray("fileTypes");

        for(Object ft : fileTypes) {
            JSONObject fileType = (JSONObject) ft;

            int typeId = fileType.getInt("typeId");
            String typeName = fileType.getString("name");
            System.out.println(".." + typeName + "(" + typeId + ")");

            JSONArray classTypes = fileType.getJSONArray("classTypes");

            for (Object cls : classTypes) {
                JSONObject classType = (JSONObject) cls;

                int classId = classType.getInt("classId");
                String className = classType.getString("name");
                float threshold = classType.getFloat("threshold");
                String formula = classType.getString("formula");
                System.out.println("...." + className + "(" + classId + ")\n"
                        + "......threashold: " + threshold+ "\n"
                        + "......formula: " + formula);
            }
        }
    }

    private void generateRandomData(int numberOfPoints, int type){
        System.out.println("\ngenerating random cloud file");
        start = System.currentTimeMillis();

        List<String> randomIn = new ArrayList<>();

        Random rn = new Random();

        randomIn.add(type == FileType.PHOTOGRAMMETRIC.ordinal() ? RANDOM_FILE1_HEADER : RANDOM_FILE2_HEADER);
        for (int i=0; i < numberOfPoints; i++){
            double rndFX = 0.0f + rn.nextDouble() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);
            double rndFY = 0.0f + rn.nextDouble() * (RANDOM_POINTS_CUBE_SIZE / 10 - 0.0f);
            double rndFZ = 0.0f + rn.nextDouble() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);

            int rndClassification = rn.nextInt(3);

//            randomIn.add(   String.valueOf(rndFX) + " " +
//                            String.valueOf(rndFY) + " " +
//                            String.valueOf(rndFZ) + " " +
//                            (type==0 ? "0 255 0 " : "") +
//                            String.valueOf(rndClassification) + " "
//            );

            if(type == FileType.PHOTOGRAMMETRIC.ordinal())

                randomIn.add(   String.valueOf(rndFX) + " " +
                                String.valueOf(rndFY) + " " +
                                String.valueOf(rndFZ) + " " +
                                (type==0 ? "0 255 0 " : "") +
                                String.valueOf(rndClassification) + " " +
                                String.valueOf(rn.nextInt(10)) + " " +
                                String.valueOf(rn.nextFloat())
                );
            else
                randomIn.add(   String.valueOf(rndFX) + " " +
                                String.valueOf(rndFY) + " " +
                                String.valueOf(rndFZ) + " " +
                                (type==0 ? "0 255 0 " : "") +
                                String.valueOf(rndClassification) + " " +
                                String.valueOf(rn.nextFloat()) + " " +
                                String.valueOf(rn.nextFloat()) + " " +
                                String.valueOf(rn.nextInt(359)) + " " +
                                String.valueOf(rn.nextFloat())
                );
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

        Stats.printElapsedTime(start, "processed " + fileName);
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
