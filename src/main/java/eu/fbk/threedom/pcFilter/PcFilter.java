package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pcFilter.utils.*;
import eu.fbk.threedom.pcFilter.utils.LinkedList;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class PcFilter {

//    private List<String> file1Data, file2Data;
    private float voxelSide;
    @Setter @Getter private VoxelGrid vGrid;

    private BBox bbox;
    private LinkedList points;

    private @Setter @Getter String[][] header, properties;
    private HashMap<String, Float> propsStats;

    private @Setter @Getter HashMap<String, ArrayList<Float>> dataHm;

    private static Point point;

    // timer
    private static long start;
    private static long time;

    public PcFilter(File file1Data, File file2Data, float voxelSide) {
//        this.file1Data = file1Data;
//        this.file2Data = file2Data;
        this.voxelSide = voxelSide;

        bbox = new BBox();
        points = new LinkedList();
        this.header = new String[2][];

        this.properties = new String[2][];
        this.propsStats = new HashMap<>();

        this.dataHm = new HashMap<>();

        point = new Point(0, 0, 0);

        //////////////////////////////
        // parse PHOTOGRAMMETRIC file
        parseData(file1Data, FileType.PHOTOGRAMMETRIC);
        // head -> n -> .. -> n -> null
        updateStatistics(0, null);
        LlNode endNode = points.head();

        //////////////////////////////
        // parse LYDAR file
        parseData(file2Data, FileType.LYDAR);
        // head -> n -> .. -> n -> endNode -> n .. -> n -> null
        updateStatistics(1, endNode);

        System.out.println("\nruntime statistics");
        propsStats.entrySet().forEach(entry->{
            System.out.println(".." + entry.getKey() + " " + entry.getValue());
        });

        // instantiate the voxel grid
        vGrid = new VoxelGrid(points, bbox, this.voxelSide);
    }

    public Point findMin(File data){
        BBox bbox = new BBox();
        FileInputStream inputStream = null;

        try {
            start = System.currentTimeMillis();

            inputStream = new FileInputStream(data);

            Scanner sc = new Scanner(inputStream, "UTF-8");
            String line = sc.nextLine(); // header

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.startsWith("//") || line.isEmpty() || line == null) continue;

                String[] token = line.split(" ");

                point.move( Float.parseFloat(token[0]),
                            Float.parseFloat(token[1]),
                            Float.parseFloat(token[2]) );

                bbox.extendTo(point);
            }

            Stats.printElapsedTime(start, "..bbox min evaluated");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bbox.getMin();
    }


    public void parseData(File data, FileType fileType) {
        System.out.println("\nparse " + fileType + " file");

        Point min = findMin(data);

        FileInputStream inputStream = null;
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
            String[] props;
            if (fileType == FileType.PHOTOGRAMMETRIC) // photogrammetric file
                props = Arrays.copyOfRange(token, 7, token.length); // cut "x y z r g b classe"
            else
                props = Arrays.copyOfRange(token, 4, token.length); // cut "x y z classe"

            fileType.setProps(props);

            this.header[fileType.ordinal()] = token;
            this.properties[fileType.ordinal()] = props;

            //if(Main.DEBUG) {
            System.out.println("..header " + Arrays.toString(header[fileType.ordinal()]));
            System.out.println("..properties " + Arrays.toString(properties[fileType.ordinal()]));
            //}

            //TODO: remove HM
            for (String prop : props) {
                // initialize statistics
                propsStats.put(prop + "_N", 0f);
                propsStats.put(prop + "_sum", 0f);
                propsStats.put(prop + "_mean", 0f);
                propsStats.put(prop + "_std", 0f);

                // initialize data hashmap
                dataHm.put(prop, new ArrayList<Float>());
            }


            ///////////////////////////////////////////////
            // parse all data
            ///////////////////////////////////////////////////////
            //int count = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.startsWith("//") || line.isEmpty()) continue;

                token = line.split(" ");

                int shift = 0;
                Point p = null;

                // X Y Z R G B Class
                if (fileType == FileType.PHOTOGRAMMETRIC) {
                    p = new Point(
                            fileType, Float.parseFloat(token[0]) - min.getX(), // x
                            Float.parseFloat(token[1]) - min.getY(), // y
                            Float.parseFloat(token[2]) - min.getZ(), // z
                            Integer.parseInt(token[3]),
                            Integer.parseInt(token[4]),
                            Integer.parseInt(token[5]));
                    p.setClassification(PointClassification.parse(Integer.parseInt(token[6].substring(0, 1))));
                    shift = 7;

                    // X Y Z Class
                } else if (fileType == FileType.LYDAR) {
                    p = new Point(
                            fileType,
                            Float.parseFloat(token[0]) - min.getX(),
                            Float.parseFloat(token[1]) - min.getY(),
                            Float.parseFloat(token[2]) - min.getZ());
                    p.setClassification(PointClassification.parse(Integer.parseInt(token[3].substring(0, 1))));
                    shift = 4;
                }

                /////////////////////////
                // for each property
                for (int t = shift; t < header[fileType.ordinal()].length; t++) {
                    // add the new value
                    // skip if value is "nan"
                    if(token[t].equals("nan"))
                        continue;

                    String prop = header[fileType.ordinal()][t];
                    float val = Float.parseFloat(token[t]);

                    // add the value inside the point properies array
                    p.setProp(t-shift, val);
//                    p.setProp(prop, val); // version with the HM

                    //System.out.println(prop + " " + val);

                    // update sum and arithmetic mean
                    propsStats.put(prop + "_N", propsStats.get(prop + "_N") + 1);
                    propsStats.put(prop + "_sum", propsStats.get(prop + "_sum") + val);
                    propsStats.put(prop + "_mean", propsStats.get(prop + "_sum") / propsStats.get(prop + "_N"));
                }

                points.addAtBeginning(p);

                // update bounding box with the new point
                bbox.extendTo(p);
                //}
            }

            //if(Main.DEBUG) {
            System.out.println(".." + bbox.toString());
            //}

            Stats.printElapsedTime(start, "..file read");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateStatistics(int fileType, LlNode exitNode){
        LlNode n = points.head();
        //int N = 0;
        String[] props = this.properties[fileType];

        ///////////////////////////////////////////////
        // evaluate standard deviation
        ///////////////////////////////////////////////////////
        // cycle on all points
        while(n != null) {
            Point p = (Point)n.value();

            // for each property
//            for(String prop : props) {
            for(int i=0; i<props.length; i++) {
                String prop = props[i];
//                if(p.getProp(prop) == -Float.MAX_VALUE)
                if(p.getProp(i) == -Float.MAX_VALUE)
                    continue;
                float val = p.getProp(i);
                float mean = propsStats.get(prop+"_mean");
                float std =  (float)Math.pow((val - mean), 2);
                propsStats.put(prop+"_std", propsStats.get(prop+"_std") + std);

                continue;
            }

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }

        // evaluate the standard deviation for each property
        for(String prop : props)
            propsStats.put(prop+"_std", (float)Math.sqrt(propsStats.get(prop+"_std") / propsStats.get(prop+"_N")));


        ///////////////////////////////////////////////
        // normalize all properties values
        ///////////////////////////////////////////////////////
        // cycle on points
        n = points.head();
        while(n != null) {
            Point p = (Point) n.value();

            // for each property
//            for (String prop : props) {
            for(int i=0; i<props.length; i++) {
                String prop = props[i];
//                float val = p.getProp(prop);
                float val = p.getProp(i);

                // normalize values between 0 and 1
                float x = (2 * (val - propsStats.get(prop+"_mean"))) / propsStats.get(prop+"_std");
                float norm_val = 1 / (1 + (float)Math.exp(-x));
//                p.setProp(prop, norm_val);
                p.setProp(i, norm_val);

                // save the normalized value in the right arraylist
                ArrayList al = dataHm.get(prop);
                al.add(norm_val);
                dataHm.put(prop, al);

                if(Main.DEBUG)
                    System.out.println("...." + prop + ": " + val + " -> " + norm_val);
            }

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }
    }


    /**
     *
     * @param fileType defines if it is a photogrammetric point (0) or 1 lydar point
     * @param voxelId
     * @return
     */
    public List<Point> getPoints(FileType fileType, int voxelId){
        return vGrid.getPoints(fileType, voxelId);
    }

    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType){
        return vGrid.getPoints(fileType, voxelId, pointType);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("voxelGrid");
        Voxel[] voxels = vGrid.getVoxels();

        for (Voxel v : voxels){
            if(v == null) continue;

            sb.append("\n\tvoxel " + v.getId());
            LlNode n = v.getHead();
            while(n != null) {
                sb.append("\n\t\t.." + n.value().toString());

                // exit condition
                if(!n.hasNext() || n == v.getTail()) break;
                n = n.next();
            }
        }

        return sb.toString();
    }
}
