package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pc.BBox;
import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import eu.fbk.threedom.structs.LlNode;
import eu.fbk.threedom.structs.Voxel;
import eu.fbk.threedom.structs.VoxelGrid;
import eu.fbk.threedom.utils.*;
import eu.fbk.threedom.structs.LinkedList;
import eu.fbk.threedom.pcNorm.Main;
import lombok.Getter;
import lombok.Setter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class PcFilter {

    private float voxelSide;
    @Setter @Getter private VoxelGrid vGrid;

    private BBox bbox;
    @Getter private LinkedList points;

    @Setter @Getter private String[][] header, properties;
    @Setter @Getter private HashMap<String, Float> propsStats;

    private static Point point;
    @Setter @Getter private static Point min;

    // timer
    private static long start;


    public PcFilter(File file1Data, File file2Data, float voxelSide) {
        this.voxelSide = voxelSide;

        bbox = new BBox();
        points = new LinkedList();
        this.header = new String[2][];

        this.properties = new String[2][];
        this.propsStats = new HashMap<>();

        //this.dataHm = new HashMap<>();

        point = new Point(0, 0, 0);
        File[] data = {file1Data, file2Data};

        min = (voxelSide != 0) ? findMin(data) : new Point(0, 0,0);

        //////////////////////////////
        // parse PHOTOGRAMMETRIC file
        parseData(file1Data, FileType.PHOTOGRAMMETRIC);
        // head -> n -> .. -> n -> null
        updateStatistics(FileType.PHOTOGRAMMETRIC, null);
        LlNode endNode = points.head();

        //////////////////////////////
        // parse LIDAR file
        parseData(file2Data, FileType.LIDAR);
        // head -> n -> .. -> n -> endNode -> n .. -> n -> null
        updateStatistics(FileType.LIDAR, endNode);

        System.out.println("\nruntime statistics");
        propsStats.entrySet().forEach(entry->{
            System.out.println(".." + entry.getKey() + " " + entry.getValue());
        });

        // instantiate the voxel grid
        if(voxelSide != 0){
            start = System.currentTimeMillis();
            vGrid = new VoxelGrid(points, bbox, this.voxelSide);
            Stats.printElapsedTime(start, "..voxel grid created");
        }
    }

    public Point findMin(File[] data){
        System.out.println("\nfinding boundingBox..");

        //BBox bbox = new BBox();
        FileInputStream inputStream = null;

        start = System.currentTimeMillis();

        for(File f : data) {
            try {
                inputStream = new FileInputStream(f);

                Scanner sc = new Scanner(inputStream, "UTF-8");
                String line; // header

                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    if (line.startsWith("//") || line.isEmpty() || line == null) continue;

                    String[] token = line.split(" ");

                    point.move(Float.parseFloat(token[0]),
                            Float.parseFloat(token[1]),
                            Float.parseFloat(token[2]));

                    bbox.extendTo(point);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Stats.printElapsedTime(start, "bounding box min is " + bbox.getMin().toString());

        return bbox.getMin();
    }

    public String[] getHeader(FileType type){
        return header[type.ordinal()];
    }

    public int getPropertyIndex(FileType fileType, String prop){
        String[] props = this.properties[fileType.ordinal()];

        for(int i=0; i<props.length; i++)
            if(props[i].equalsIgnoreCase(prop)) return i;

        return -1;
    }


    public void parseData(File data, FileType fileType) {
        System.out.println("\nparse " + fileType + " file");

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
            int shift = 0;
            if (fileType == FileType.PHOTOGRAMMETRIC) { // photogrammetric file
                props = Arrays.copyOfRange(token, 7, token.length); // cut "x y z r g b class"
                shift = 7;
            }else{
                props = Arrays.copyOfRange(token, 4, token.length); // cut "x y z class"
                shift = 4;
            }

            fileType.setProps(props);

            this.header[fileType.ordinal()] = token;
            this.properties[fileType.ordinal()] = props;

            //if(Main.DEBUG) {
            System.out.println("..header " + Arrays.toString(header[fileType.ordinal()]));
            System.out.println("..properties " + Arrays.toString(properties[fileType.ordinal()]));
            //}

            for (String prop : props) {
                // initialize statistic hashmap
                propsStats.put(prop + "_N", 0f);
                propsStats.put(prop + "_sum", 0f);
                propsStats.put(prop + "_mean", 0f);
                propsStats.put(prop + "_std", 0f);
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
                if (fileType == FileType.PHOTOGRAMMETRIC) {
                    p = new Point(
                            fileType, Float.parseFloat(token[0]) - this.min.getX(), // x
                            Float.parseFloat(token[1]) - this.min.getY(), // y
                            Float.parseFloat(token[2]) - this.min.getZ(), // z
                            Integer.parseInt(token[3]),
                            Integer.parseInt(token[4]),
                            Integer.parseInt(token[5]));
                    p.setClassification(PointClassification.parse(Integer.parseInt(token[6].substring(0, 1))));

                    // X Y Z Class
                } else if (fileType == FileType.LIDAR) {
                    p = new Point(
                            fileType,
                            Float.parseFloat(token[0]) - this.min.getX(),
                            Float.parseFloat(token[1]) - this.min.getY(),
                            Float.parseFloat(token[2]) - this.min.getZ());
                    p.setClassification(PointClassification.parse(Integer.parseInt(token[3].substring(0, 1))));
                }

                /////////////////////////
                // for each property
                for (int t = 0; t < props.length; t++) {
                    // add the new value
                    // skip if value is "nan"
                    if(token[shift + t].equals("nan"))
                        continue;

                    String prop = props[t];
                    float val = Float.parseFloat(token[shift + t]);

                    // TODO: range conversion (now manually set here)
                    if(prop.equalsIgnoreCase("ScanAngleRank")) {
                        val = Math.abs(val);
                    }

                    // add the value inside the point properies array
                    p.setProp(t, val);

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

    public void updateStatistics(FileType fileType, LlNode exitNode){
        LlNode n = points.head();
        //int N = 0;
        String[] props = this.properties[fileType.ordinal()];

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

                // check "nan" values
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
            for(int i=0; i<props.length; i++) {
                String prop = props[i];

                float val = p.getProp(i);
                // TODO: for ELEONORA
                //float val = p.getProp(i) * 1000000;

//                // TODO: range conversion (now manually set here)
//                if(prop.equalsIgnoreCase("ScanAngleRank")) {
//                    val = Math.abs(val);
//                    p.setProp(i, val);
//                }

                // normalize values between 0 and 1
                float x = (2 * (val - propsStats.get(prop+"_mean"))) / propsStats.get(prop+"_std");
                float norm_val = 1 / (1 + (float)Math.exp(-x));
                p.setNormProp(i, norm_val);

                if(Main.DEBUG)
                    System.out.println("...." + prop + ": " + val + " -> " + norm_val);
            }


            //TODO: commented for ELEONORA
            float score = 0;
            if(fileType == FileType.PHOTOGRAMMETRIC) {
                switch(p.getClassification()) {
                    case C0:
                        score = p.getNormProp(getPropertyIndex(fileType, "PIntensity")) +
                                (1 - p.getNormProp(getPropertyIndex(fileType, "NumberOfReturns")));
                        break;
                    case C1:
                    case C2:
                        score = p.getNormProp(getPropertyIndex(fileType, "PIntensity"));
                        break;
                    default:
                        break;
                }
            }

            if(fileType == FileType.LIDAR) {
                switch(p.getClassification()) {
                    case C0:
                    case C2:
                        score = 1 - p.getNormProp(getPropertyIndex(fileType, "LIntensity")) +
                                p.getNormProp(getPropertyIndex(fileType, "dZVariance")) +
                                1 - p.getNormProp(getPropertyIndex(fileType, "EchoRatio")) +
                                p.getNormProp(getPropertyIndex(fileType, "ScanAngleRank"));
                        break;
                    case C1:
                        score = 1 - p.getNormProp(getPropertyIndex(fileType, "LIntensity")) +
                                p.getNormProp(getPropertyIndex(fileType, "dZVariance")) +
                                p.getNormProp(getPropertyIndex(fileType, "ScanAngleRank"));
                        break;
                }
            }

            p.setScore(score);

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }
    }


    /**
     *
     * @param fileType defines if it is a photogrammetric point (0) or 1 lidar point
     * @param voxelId
     * @return
     */
    public List<Point> getPoints(FileType fileType, int voxelId){
        return vGrid.getPoints(fileType, voxelId);
    }

    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType){
        return vGrid.getPoints(fileType, voxelId, pointType);
    }

    public List<Point> getPoints(FileType fileType, boolean voxelGrid){
        if(voxelGrid)
            return vGrid.getPoints(fileType);

        // else
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

    public List<Point> getPoints(FileType fileType, PointClassification pointType, boolean voxelGrid){
        if(voxelGrid)
            return vGrid.getPoints(fileType);

        // else
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

    static String parseFormula(String formula){
        // ( 1 - LIntensity ) + dZVariance + ( 1 - EchoRatio ) + ScanAngleRank

        List<String> out = new ArrayList<>();

        String[] token = formula.split( " ");

        for(String s : token){
            if(s.equalsIgnoreCase("(") || s.equalsIgnoreCase(")"))
                out.add(s);


        }

        return out.toString();
    }
}
