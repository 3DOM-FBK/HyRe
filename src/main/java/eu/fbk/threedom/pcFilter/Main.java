package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import eu.fbk.threedom.structs.Voxel;
import eu.fbk.threedom.utils.Stats;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;
import sun.reflect.generics.tree.Tree;

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
    @Option(name = "-o", aliases = { "--output" }, metaVar = "output") String outFile;
    @Option(name = "-w", aliases = { "--overwrite" }, metaVar = "overWrite") Boolean overWrite;
    @Option(name = "-v", aliases = { "--verbose" }, metaVar = "verbose") Boolean verbose;

    public static boolean DEBUG;
    private static final int RANDOM_POINTS_NUMBER = 10000;
    private static final float RANDOM_POINTS_CUBE_SIZE = 100;
    private static final String RANDOM_FILE1_HEADER = "// X Y Z R G B Class NumberOfReturns PIntensity";
    private static final String RANDOM_FILE2_HEADER = "// X Y Z Class LIntensity dZVariance ScanAngleRank EchoRatio";

    private static String filePath, fileName, fn1, fn2;

    // timer
    private static long start;
    private static long time;

    private File outFile1, outFile2;

    public static JSONObject config;

    private HashMap<String, Float> voxelDensityStats;

    private PcFilter pcf;

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

//        Path out1 = null, out2 = null;

//        try {
//            if (outFile == null) {
//                outFile1 = new File(filePath + File.separator + fn1 + "_out.txt");
//                outFile2 = new File(filePath + File.separator + fn2 + "_out.txt");
//            }
//                //outFile = new File(filePath + File.separator + fn1 + "_" + fn2 + ".txt");
//            System.out.println("\noutFile:\n.." + outFile1 + "\n.." + outFile2);
//
//            out1 = Paths.get(outFile1.toURI()); Files.createFile(out1);
//            out2 = Paths.get(outFile2.toURI()); Files.createFile(out2);
//
//        } catch (FileAlreadyExistsException foee) {
//            if (overWrite == null || !overWrite) {
//                System.out.println("\nWARNING! the output file already exists");
//                System.exit(1);
//            } else
//                System.out.println("\nreading input files");
//                if(overWrite)
//                    System.out.println("..overWrite output file");
//        }

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
        //start = System.currentTimeMillis();
        pcf = new PcFilter(inFile1, inFile2, voxelSide);
        //Stats.printElapsedTime(start, "..voxel grid created");

        ArrayList<Point> pointList;
        Set<Integer> filteredIntersectionSet = new TreeSet<>();
        Set<Integer> scoredFilteredIntersectionSet = new TreeSet<>();

        if(voxelSide != 0) {
            ///////////////////////////////////////////////////////
            // pick a random voxel to filter
            ///////////////////////////////////////////////////////
//            Random rnd = new Random();
//            int vGridSize = pcf.getVGrid().getSize();
//            int i = rnd.nextInt(vGridSize);

            ///////////////////////////////////////////////////////
            // postprocessing statistics
            ///////////////////////////////////////////////////////

//            // POINT BELONGING TO SPECIFIC VOXEL
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
            // SHOW DATA
            ///////////////////////////////////////////////////////



            ///////////////////////////////////////////////////////
            // AVERAGE VOXEL DENSITY
            System.out.println("\n///////////////////////////////////////////////////////\n// VOXEL DENSITY");

            voxelDensityStats = new HashMap<>();

            int numberOfPointsInVoxel_sum;

            // cycle on photogrammetry/lidar file
            for (FileType ft : FileType.values()) {
                start = System.currentTimeMillis();
                System.out.println(ft.name() + " cloud");

                ///////////////////////////////////////////////////////
                // AVERAGE VOXEL DENSITY (PER CLASS)
                // cycle on roof/facade/street point types
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
                // AVERAGE VOXEL DENSITY
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



            ///////////////////////////////////////////////////////
            // PHOTO/LIDAR INTERSECTION IN EACH VOXEL
            System.out.println("\n///////////////////////////////////////////////////////\n// PHOTO/LIDAR INTERSECTION IN EACH VOXEL");

//            Set<Integer> voxelSetPhoto = pcf.getVGrid().getVoxels(FileType.PHOTOGRAMMETRIC);
//            Set<Integer> voxelSetLidar = pcf.getVGrid().getVoxels(FileType.LIDAR);
//
//            // intersection of sets
//            Set<Integer> intersection = new HashSet<Integer>(voxelSetPhoto);
//            intersection.retainAll(voxelSetLidar);

            start = System.currentTimeMillis();
            // initialize with all the voxels
            Set<Integer> intersectionSet = pcf.getVGrid().getVoxels( new FileType[] {FileType.PHOTOGRAMMETRIC,FileType.LIDAR} );
            // cycle on photogrammetry/lidar file and find the intersection
            for (FileType ft : FileType.values())
                intersectionSet.retainAll(pcf.getVGrid().getVoxels(ft));


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


            ///////////////////////////////////////////////////////
            // MULTICLASS IN EACH INTERSECTION VOXEL
            System.out.println("\n///////////////////////////////////////////////////////\n// MULTICLASS IN EACH INTERSECTION VOXEL");

            List<Integer> c0_c1 = new ArrayList();
            List<Integer> c0_c2 = new ArrayList();
            List<Integer> c1_c2 = new ArrayList();
            List<Integer> c0_c1_c2 = new ArrayList();

            // cycle on photogrammetry/lidar file
            for (FileType ft : FileType.values()) {
                start = System.currentTimeMillis();

                c0_c1.clear();
                c0_c2.clear();
                c1_c2.clear();

                for( int v : intersectionSet) {
                    List<Point> points = pcf.getVGrid().getPoints(ft, v);

//                    for(Point p : points)
//                        System.out.println("++++++++++++++++" + p.toString(pcf.getCoordShift()) + ", c " + p.getClassification().ordinal());

                    boolean c0 = false, c1 = false, c2 = false;
                    for(Point p : points){
                        if(p.getClassification().ordinal() == 0) c0 = true;
                        if(p.getClassification().ordinal() == 1) c1 = true;
                        if(c0 && c1) {
                            c0_c1.add(v);
                            //System.out.println("..c0_c1");
                            break;
                        }
                    }

                    //points.clear();
                    c0 = false; c1 = false; c2 = false;
                    for(Point p : points){
                        if(p.getClassification().ordinal() == 0) c0 = true;
                        if(p.getClassification().ordinal() == 2) c2 = true;
                        if(c0 && c2) {
                            //System.out.println("..c0_c2");
                            c0_c2.add(v);
                            break;
                        }
                    }

                    //points.clear();
                    c0 = false; c1 = false; c2 = false;
                    for(Point p : points){
                        if(p.getClassification().ordinal() == 1) c1 = true;
                        if(p.getClassification().ordinal() == 2) c2 = true;
                        if(c1 && c2) {
                            //System.out.println("..c1_c2");
                            c1_c2.add(v);
                            break;
                        }
                    }

                    //points.clear();
                    c0 = false; c1 = false; c2 = false;
                    for(Point p : points){
                        if(p.getClassification().ordinal() == 0) c0 = true;
                        if(p.getClassification().ordinal() == 1) c1 = true;
                        if(p.getClassification().ordinal() == 2) c2 = true;
                        if(c0 && c1 && c2) {
                            //System.out.println("..c0_c1_c2");
                            c0_c1_c2.add(v);
                            break;
                        }
                    }
                }

                System.out.println("...." + ft + " cloud");
                if(c0_c1.size() > 0) System.out.println("......c0/c1 (in " + c0_c1.size() + " voxels)");
                if(c0_c2.size() > 0) System.out.println("......c0/c2 (in " + c0_c2.size() + " voxels)");
                if(c1_c2.size() > 0) System.out.println("......c1/c2 (in " + c1_c2.size() + " voxels)");
                if(c0_c1_c2.size() > 0) System.out.println("......c0/c1/c2 (in " + c0_c1_c2.size() + " voxels)");

//                System.out.println("\nc0_c1 (" + ft + "): " + c0_c1.size());
//                System.out.println("c0_c2 (" + ft + "): " + c0_c2.size());
//                System.out.println("c1_c2 (" + ft + "): " + c1_c2.size());
//                System.out.println("c0_c1_c2 (" + ft + "): " + c0_c1_c2.size());
                Stats.printElapsedTime(start, "processed");
            }


            //System.out.println("\n" + voxelDensityStats.keySet() + "\nsize: " + voxelDensityStats.size());



            ////////////////////////////////////////////////////////////////////////////////
            // FILTERED INTERSECTION SET
            System.out.println("\n///////////////////////////////////////////////////////\n// FILTERED INTERSECTION SET");

            System.out.println("photo/lidar intersection voxels where " +
                    "at least one class for each -> filetype voxel density >= voxel density mean");
            filteredIntersectionSet = intersectionSet;

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

        }
        Stats.printElapsedTime(start, "processed");


        ///////////////////////////////////////////////////////////////////////
        //PRINT PROPERTIES STATISTICS
        System.out.println("\n///////////////////////////////////////////////////////\n// PROPERTIES STATISTICS");


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
                            pointList = (ArrayList<Point>) pcf.getPoints(ft, v);
                            for (Point p : pointList)
//                            propValues.add(p.getProp(prop));
                                propValues.add(p.getNormProp(k));

                        }
                    }else{
                        pointList = (ArrayList<Point>) pcf.getPoints(ft, pc,false);
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


        ///////////////////////////////////////////////////////
        // aggregation formula test
//        System.out.println("\nscore");
//        //i = rnd.nextInt( Math.round(pcf.getPropsStats().get("PIntensity_N")) );
//        List<Point> points = (voxelSide != 0) ? pcf.getPoints(FileType.PHOTOGRAMMETRIC, true) :
//                pcf.getPoints(FileType.PHOTOGRAMMETRIC, false);
//
//        System.out.println(".." + FileType.parse(FileType.PHOTOGRAMMETRIC.ordinal()) + " points");
//        for(int l=0; l < 1; l++) {
//            Point p = points.get(l);
//            System.out.println("...." + p.toString() + " -> score: " + p.getScore());
//        }
//
//        points = (voxelSide != 0) ? pcf.getPoints(FileType.LIDAR, true) :
//                pcf.getPoints(FileType.LIDAR, false);
//
//        System.out.println(".." + FileType.parse(FileType.LIDAR.ordinal()) + " points");
//        for(int l=0; l < 1; l++) {
//            Point p = points.get(l);
//            System.out.println("...." + p.toString() + " -> score: " + p.getScore());
//        }

//        System.out.println("\nscore");
//        List<Point> points = (voxelSide != 0) ? pcf.getPoints(FileType.LIDAR, true) :
//                pcf.getPoints(FileType.LIDAR, false);



        ////////////////////////////////////////////////////////////////////////////////
        // SCORED FILTERED INTERSECTION SET
        System.out.println("\n///////////////////////////////////////////////////////\n// SCORED FILTERED INTERSECTION SET");

        if(voxelSide != 0) {
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

                        if (pointList.size() < (density_mean - density_std)) {
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


        
        /////////////////////////////////////////////
        // WRITE DATA
        ////////////////////////////////////////////
        if(voxelSide == 0)
            writeOutput(pcf.getPoints(), "out");
        else
            writeOutput(scoredFilteredIntersectionSet, true, "out");



//        ///////////////////////////////////////////////////////
//        // collect points (filtering according to threasholds)
//        ///////////////////////////////////////////////////////
//
////        // read the config file
////        File jsonfile = new File(filePath + File.separator + "config.json");
////        readThresholdJson(jsonfile);
//        JSONArray fileTypes = config.getJSONArray("fileTypes");
//
//        // write the output files
//        BufferedWriter writer1 = new BufferedWriter(new FileWriter(outFile1, false));
//        BufferedWriter writer2 = new BufferedWriter(new FileWriter(outFile2, false));
//        BufferedWriter bw;
//
//        System.out.println("\nWrite files");
//        for(FileType ft : FileType.values()) {
//            System.out.println(".."+ft);
//            start = System.currentTimeMillis();
//
//            JSONObject fileTypeObj = (JSONObject) fileTypes.get(ft.ordinal());
//            JSONArray classTypes = fileTypeObj.getJSONArray("classTypes");
//
//            String headerStr = Arrays.toString(pcf.getHeader(ft)).replaceAll(",", "");
//            //headerStr = headerStr.substring(1, headerStr.length()-1); // remove square brackets []
//            headerStr = headerStr.replace("[", "// ");
//            headerStr = headerStr.replace("]", "");
//
//            headerStr += " score";
//
//            System.out.println("..header: " + headerStr);
//
//            bw = (ft == FileType.PHOTOGRAMMETRIC) ? writer1 : writer2;
//            bw.write(headerStr + "");
//            bw.newLine();
//
//            List<Point> points = new ArrayList<>();
//
//            if(voxelSide == 0) {
//                points = (voxelSide != 0) ? pcf.getPoints(ft, true) : pcf.getPoints(ft, false);
//
//                if (!points.isEmpty())
//                    for (Point p : points) {
//                        System.out.println("......point " + p.toString(pcf.getCoordShift()));
//                        JSONObject classTypeObj = (JSONObject) classTypes.get(p.getClassification().ordinal());
//                        float threshold = classTypeObj.getFloat("threshold");
//                        //String formula = classTypeObj.getString("formula");
//
//                        //                    System.out.println(p.toString() +
//                        //                            " type: " + p.getType() +
//                        //                            " class: " + p.getClassification().ordinal() +
//                        //                            " formula: " + formula);
//
//                        System.out.println("........score " + p.getScore());
//                        System.out.println("........threshold " + threshold);
//
//                        // filter according to the score
//                        if (p.getScore() <= threshold) {
//                            System.out.println("..........yes");
//                            // SELECT true if you want normalized values
//                            bw.write(p.toStringOutput(false, pcf.getCoordShift()));
//                            bw.newLine();
//                        }
//                    }
//
//                //Stats.printElapsedTime(start, "file written");
//            }else {
//
//                for (Integer v : filteredIntersectionSet) {
//                    System.out.println("....v"+v);
//
//                    //List<Point> points = (voxelSide != 0) ? pcf.getPoints(ft, true) : pcf.getPoints(ft, false);
//                    points = pcf.getPoints(ft, v);
//
//                    if (!points.isEmpty())
//                        for (Point p : points) {
//                            System.out.println("......point " + p.toString(pcf.getCoordShift()));
//                            JSONObject classTypeObj = (JSONObject) classTypes.get(p.getClassification().ordinal());
//                            float threshold = classTypeObj.getFloat("threshold");
//                            //String formula = classTypeObj.getString("formula");
//
//                            //                    System.out.println(p.toString() +
//                            //                            " type: " + p.getType() +
//                            //                            " class: " + p.getClassification().ordinal() +
//                            //                            " formula: " + formula);
//
//                            System.out.println("........score " + p.getScore());
//                            System.out.println("........threshold " + threshold);
//
//                            // filter according to the score
//                            if (p.getScore() <= threshold) {
//                                System.out.println("..........yes");
//                                // SELECT true if you want normalized values
//                                bw.write(p.toStringOutput(false, pcf.getCoordShift()));
//                                bw.newLine();
//                            }else{
//                                points.remove(p);
//                            }
//                        }
//
//                    if(points.isEmpty())
//                        break;
//                }
//            }
//            Stats.printElapsedTime(start, "file written");
//        }
//
//        writer1.close();
//        writer2.close();
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


        JSONArray fileTypes = config.getJSONArray("fileTypes");

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
            float rndFX = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);
            float rndFY = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE / 10 - 0.0f);
            float rndFZ = 0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f);

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
