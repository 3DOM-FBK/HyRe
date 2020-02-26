package eu.fbk.threedom.pcNorm;

import eu.fbk.threedom.pc.BBox;
import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import eu.fbk.threedom.structs.LlNode;
import eu.fbk.threedom.utils.*;
import eu.fbk.threedom.structs.LinkedList;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class PcNorm {

    private static final int REQUIRED_PARAMS_COUNT = 7;

    private BBox bbox;
    @Getter private LinkedList points;

    @Setter @Getter private String[] header, properties;
    @Setter @Getter private HashMap<String, Double> propsStats;

    private static Point point;
    @Setter @Getter private static Point min;

    // timer
    private static long start;


    public PcNorm(File file) {

        bbox = new BBox();
        points = new LinkedList();
        header = new String[100];
        properties = new String[100 - REQUIRED_PARAMS_COUNT];
        propsStats = new HashMap<>();

        point = new Point(0, 0, 0);
        min = new Point(0, 0,0);

        //////////////////////////////
        // parse PHOTOGRAMMETRIC file
        parseData(file, FileType.PHOTOGRAMMETRIC);
        // head -> n -> .. -> n -> null
        updateStatistics(null);
        LlNode endNode = points.head();


        System.out.println("\nruntime statistics");
        propsStats.entrySet().forEach(entry->{
            System.out.println(".." + entry.getKey() + " " + entry.getValue());
        });
    }


    public void parseData(File data, FileType fileType) {
        System.out.println("\nparse file " + data.getName());

        FileInputStream inputStream;

        try {
            start = System.currentTimeMillis();
            inputStream = new FileInputStream(data);

            Scanner sc = new Scanner(inputStream, "UTF-8");
            String line = sc.nextLine(); // header
            if(line.isEmpty()) return;

            ///////////////////////////////////////////////
            // parse header if present (first row)
            ///////////////////////////////////////////////////////
            if (line.startsWith("// "))
                line = line.replace("// ", "");
            else if (line.startsWith("//"))
                line = line.replace("//", "");
            else line = "";

            String[] token = line.split(" ");

            // arrays of properties names
            this.properties = Arrays.copyOfRange(token, REQUIRED_PARAMS_COUNT, token.length); // cut "x y z r g b class"
            fileType.setProps(properties);
            this.header = token;

            //if(Main.DEBUG) {
            System.out.println("..header " + Arrays.toString(header));
            System.out.println("..properties " + Arrays.toString(properties));
            //}

            for (String prop : properties) {
                // initialize statistic hashmap
                propsStats.put(prop + "_N", 0d);
                propsStats.put(prop + "_sum", 0d);
                propsStats.put(prop + "_mean", 0d);
                propsStats.put(prop + "_std", 0d);
            }


            ///////////////////////////////////////////////
            // parse all data
            ///////////////////////////////////////////////////////
            //int count = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.startsWith("//") || line.isEmpty()) continue;

                token = line.split(" ");

                Point p = null;

                // X Y Z R G B Class
                p = new Point(
                        FileType.PHOTOGRAMMETRIC, Float.parseFloat(token[0]) - this.min.getX(), // x
                        Float.parseFloat(token[1]) - this.min.getY(), // y
                        Float.parseFloat(token[2]) - this.min.getZ(), // z
                        Integer.parseInt(token[3]),
                        Integer.parseInt(token[4]),
                        Integer.parseInt(token[5]));
                p.setClassification(PointClassification.parse(Integer.parseInt(token[6].substring(0, 1))));


                /////////////////////////
                // for each property
                for (int t = 0; t < properties.length; t++) {
                    // add the new value
                    // skip if value is "nan"
                    if(token[REQUIRED_PARAMS_COUNT + t].equals("nan"))
                        continue;

                    String prop = properties[t];
                    double val = Double.parseDouble(token[REQUIRED_PARAMS_COUNT + t]);

                    // add the value inside the point properies array
                    p.setDoubleProp(t, val);

                    // update sum and arithmetic mean
                    propsStats.put(prop + "_N", propsStats.get(prop + "_N") + 1);
                    propsStats.put(prop + "_sum", propsStats.get(prop + "_sum") + val);
                    propsStats.put(prop + "_mean", propsStats.get(prop + "_sum") / propsStats.get(prop + "_N"));
                }

                points.addAtBeginning(p);
            }

            Stats.printElapsedTime(start, "file read");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateStatistics(LlNode exitNode){
        LlNode n = points.head();

        ///////////////////////////////////////////////
        // evaluate standard deviation
        ///////////////////////////////////////////////////////
        // cycle on all points
        while(n != null) {
            Point p = (Point)n.value();

            // for each property
//            for(String prop : props) {
            for(int i=0; i < properties.length; i++) {
                String prop = properties[i];
//                if(p.getProp(prop) == -Float.MAX_VALUE)

                // check "nan" values
                if(p.getProp(i) == -Float.MAX_VALUE)
                    continue;

                double val = p.getDoubleProp(i);
                double mean = propsStats.get(prop+"_mean");
                double std =  Math.pow((val - mean), 2);
                propsStats.put(prop+"_std", propsStats.get(prop+"_std") + std);

                continue;
            }

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }

        // evaluate the standard deviation for each property
        for(String prop : properties)
            propsStats.put(prop+"_std", Math.sqrt(propsStats.get(prop+"_std") / propsStats.get(prop+"_N")));


        ///////////////////////////////////////////////
        // normalize all properties values
        ///////////////////////////////////////////////////////
        // cycle on points
        n = points.head();
        while(n != null) {
            Point p = (Point) n.value();
            // for each property
            for(int i=0; i < properties.length; i++) {
                String prop = properties[i];
                double val = p.getDoubleProp(i);

                //TODO: normalize values between 0 and 1
                double x = (2 * (val - propsStats.get(prop+"_mean"))) / propsStats.get(prop+"_std");
                double norm_val = 1 / (1 + Math.exp(-x));
                p.setDoubleNormProp(i, norm_val);

                if(Main.DEBUG)
                    System.out.println("...." + prop + ": " + val + " -> " + norm_val);
            }

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }
    }

    public List<Point> getPoints(FileType fileType){
        List<Point> list = new ArrayList<>();

        LlNode n = points.head();
        while(n != null) {
            Point p = (Point)n.value();
            if(p.getType() == fileType)
                list.add(p);

            // exit condition
            if(!n.hasNext() ) break;
            n = n.next();
        }

        return list;
    }

    public List<Point> getPoints(FileType fileType, PointClassification pointType){
        List<Point> list = new ArrayList<>();

        LlNode n = points.head();
        while(n != null) {
            Point p = (Point)n.value();
            if(p.getType() == fileType && p.getClassification() == pointType)
                list.add(p);

            // exit condition
            if(!n.hasNext() ) break;
            n = n.next();
        }

        return list;
    }
}
