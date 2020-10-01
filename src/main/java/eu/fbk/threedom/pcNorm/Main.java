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
package eu.fbk.threedom.pcNorm;

import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import eu.fbk.threedom.utils.Stats;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {


    @Argument(index=0, required = true, metaVar = "file") File inFile;
    @Option(name = "-o", aliases = { "--output" }, metaVar = "output") String outFile;
    @Option(name = "-w", aliases = { "--overwrite" }, metaVar = "overWrite") Boolean overWrite;
    @Option(name = "-v", aliases = { "--verbose" }, metaVar = "verbose") Boolean verbose;

    public static boolean DEBUG;
    private static final int RANDOM_POINTS_NUMBER = 10000;
    private static final double RANDOM_POINTS_CUBE_SIZE = 100;
    public static final String RANDOM_FILE_HEADER = "// X Y Z R G B Class P1 P2";

    private static String filePath, fileName, fn;

    // timer
    private static long start;
    private static long time;

    private File file;

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
            System.err.print("Usage: pcNorm");
            parser.printSingleLineUsage(System.err);
            System.err.println();

            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("Example:\n\n  pcNorm f.txt -v" + parser.printExample(OptionHandlerFilter.ALL));
            System.exit(1);
        }
    }

    private void run() throws Exception {
        ///////////////////////////////////////////////////////
        // create an output file
        ///////////////////////////////////////////////////////
        filePath = FilenameUtils.getFullPath(inFile.getPath());

        if(filePath.isEmpty())
            filePath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath();
        System.out.println("filePath:\n.." + filePath);

        fn = FilenameUtils.getBaseName(inFile.getPath()); //System.out.println("fn1: " + fn1);

        Path out = null;

        try {
            if (outFile == null)
                file = new File(filePath + File.separator + fn + "_out.txt");

            System.out.println("\noutFile:\n.." + file);

            out = Paths.get(file.toURI()); Files.createFile(out);

        } catch (FileAlreadyExistsException foee) {
            if (overWrite == null || !overWrite) {
                System.out.println("\nWARNING! the output file already exists");
                System.exit(1);
            } else
                System.out.println("\nreading input file");
            if(overWrite)
                System.out.println("..overWrite output file");
        }


        ///////////////////////////////////////////////////////
        // use the random function to generate random points
        ///////////////////////////////////////////////////////
        if (fn.toString().equals("rnd")) {
            generateRandomData(RANDOM_POINTS_NUMBER);
        }


        ///////////////////////////////////////////////////////
        // create the structure
        ///////////////////////////////////////////////////////
        //start = System.currentTimeMillis();
        PcNorm pcn = new PcNorm(inFile);
        //Stats.printElapsedTime(start, "..voxel grid created");

        ArrayList<Point> pointList;




        System.out.println("\nproperties statistics");

        String[] props = pcn.getProperties();

        FileType ft_photo = FileType.PHOTOGRAMMETRIC;

        // EVALUATE PROPERTY MED/MAD
        //for(FileType ft : FileType.values()) {
        //System.out.println("\n.." + ft.name());
        start = System.currentTimeMillis();

        for(int p=0; p < props.length; p++) {
            String prop = props[p];

            List<Point> points = pcn.getPoints(ft_photo);

            // transform arrayList to array
            double[] values = new double[points.size()];

            if(values.length == 0) break;

            int n = 0;
//                for (Object v : propValues)
            for (Point pnt : points)
                values[n++] = pnt.getNormProp(p);

            if (Main.DEBUG)
                System.out.println(".." + prop + " values (normalized) " + Arrays.toString(values));
            else
                System.out.println(".." + prop + " values (normalized) " + values.length + " values");

            double med = Stats.median(values, values.length);
            double mad = Stats.mad(values, values.length);
            System.out.println("....med: " + med + "\n....mad: " + mad
                    + "\n....sigmaM: " + (mad * 1.4826)
                    + "\n....3sigmaM: " + 3 * (mad * 1.4826));
        }
        Stats.printElapsedTime(start, "processed");
        //}

        // EVALUATE PROPERTY MED/MAD (CLASS)
        //for(FileType ft : FileType.values()) {

        System.out.println("\n.." + ft_photo.name());
        start = System.currentTimeMillis();

        for(int k=0; k < props.length; k++) {
            String prop = props[k];
            System.out.println("...." + prop);

            for (PointClassification pc : PointClassification.values()) {

                ArrayList propValues = new ArrayList();

                pointList = (ArrayList<Point>) pcn.getPoints(ft_photo, pc);
                for (Point p : pointList)
                    propValues.add(p.getNormProp(k));

                if(propValues.isEmpty()) break;

                if(Main.DEBUG)
                    System.out.println("......" + pc.name() + " (normalized) " + propValues);
                else
                    System.out.println("......" + pc.name() + " (normalized) " + propValues.size() + " values");

                // transform arrayList to array
                double[] values = new double[propValues.size()];
                values = new double[propValues.size()];
                int n = 0;
                for (Object p : propValues)
                    values[n++] = (double)p;

                double med = Stats.median(values, values.length);
                double mad = Stats.mad(values, values.length);
                System.out.println("........med: " + med + "\n........mad: " + mad
                        + "\n........sigmaM: " + (mad * 1.4826)
                        + "\n....3sigmaM: " + 3*(mad * 1.4826) );
            }
        }

        Stats.printElapsedTime(start, "processed");
        //}


        ///////////////////////////////////////////////////////
        // collect points
        ///////////////////////////////////////////////////////

        // write the output files
        System.out.println("\noutput");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

        for(FileType ft : FileType.values()) {
            start = System.currentTimeMillis();

            List<Point> points = pcn.getPoints(ft);

            if(points.size() == 0) continue;

            String headerStr = Arrays.toString(pcn.getHeader()).replaceAll(",", "");
            //headerStr = headerStr.substring(1, headerStr.length()-1); // remove square brackets []
            headerStr = headerStr.replace("[", "// ");
            headerStr = headerStr.replace("]", "");

            System.out.println("..header: " + headerStr);

            writer.write(headerStr + "");
            writer.newLine();

            if (!points.isEmpty())
                for (Point p : points) {
                    // SELECT true if you want normalized values
                    writer.write(p.toStringDoubleOutput(true, pcn.getMin()));
                    writer.newLine();
                }

            Stats.printElapsedTime(start, "file written");
        }

        writer.close();
    }


    private void generateRandomData(int numberOfPoints){
        start = System.currentTimeMillis();

        List<String> randomIn = new ArrayList<>();

        Random rn = new Random();
        randomIn.add(RANDOM_FILE_HEADER);
        for (int i=0; i < numberOfPoints; i++){
            float rndFX = (float) (0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f));
            float rndFY = (float) (0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE / 10 - 0.0f));
            float rndFZ = (float) (0.0f + rn.nextFloat() * (RANDOM_POINTS_CUBE_SIZE - 0.0f));

            int rndClassification = rn.nextInt(9);

            randomIn.add(   String.valueOf(rndFX) + " " +
                    String.valueOf(rndFY) + " " +
                    String.valueOf(rndFZ) + " " +
                    "0 255 0 " +
                    String.valueOf(rndClassification) + " " +
                    String.valueOf(0.000001 + rn.nextFloat() * 0.00009) + " " +
                    String.valueOf(0.000001 + rn.nextFloat() * 0.00009)
            );
        }

        fileName = "rnd.txt";
        File rnd1 = new File(filePath + File.separator + fileName);
        Path rnd1_out = Paths.get(rnd1.toURI());

        try {
            if(!Files.exists(rnd1_out))
                Files.createFile(rnd1_out);
            Files.write(rnd1_out, randomIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stats.printElapsedTime(start, "..random cloud generated and written in " + fileName);
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
