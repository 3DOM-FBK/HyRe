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
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class PcFilter {

    private float voxelSide;
    @Setter @Getter private static VoxelGrid vGrid;

    private BBox bbox;
    @Getter private LinkedList points;

    @Setter @Getter private String[][] header, properties;
    @Setter @Getter private HashMap<String, Double> propsStats;

    private static Point point;
    @Setter @Getter private static Point newBboxMin;
    @Setter @Getter private static Point coordShift;

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
        newBboxMin = new Point(0, 0, 0);
        File[] data = {file1Data, file2Data};

        //min = (voxelSide != 0) ? findMin(data) : new Point(0, 0, 0);
        min = findMin(data);


        //System.out.println("&&&&&&&&&&&& BboxMin " + bbox.getMin());
        //System.out.println("&&&&&&&&&&&& BboxMax " + bbox.getMax());

        //////////////////////////////////////////////////////////
        // translate the boundingbox to the new position
        if(voxelSide != 0) {
            // find the components of the shift vector
            double vectorShiftX = (int) (min.x / voxelSide) * voxelSide;
            double vectorShiftY = (int) (min.y / voxelSide) * voxelSide;
            double vectorShiftZ = (int) (min.z / voxelSide) * voxelSide;
            coordShift = new Point(vectorShiftX, vectorShiftY, vectorShiftZ);

            //System.out.println("&&&&&&&&&&&& newBboxMin " + bbox.getMin());
            //System.out.println("&&&&&&&&&&&& newBboxMax " + bbox.getMax());
        }else{
            coordShift = min;
        }

        // apply the shift vector to the bbox
        bbox.setMin(bbox.getMin().subPoint(coordShift));
        bbox.setMax(bbox.getMax().subPoint(coordShift));
        //////////////////////////////////////////////////////////


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
            //vGrid = new VoxelGrid(points, bbox, this.voxelSide);

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
            String line = null; // header
            try {
                inputStream = new FileInputStream(f);

                Scanner sc = new Scanner(inputStream, "UTF-8");

                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    if (line.startsWith("//") || line.isEmpty() || line == null) continue;

                    String[] token = line.split(" ");

                    point.move(Float.parseFloat(token[0]),
                            Float.parseFloat(token[1]),
                            Float.parseFloat(token[2]));

                    //System.out.println(".." + point);

                    bbox.extendTo(point);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (ArrayIndexOutOfBoundsException e){
                System.out.println("error reading line : " + line);
                e.printStackTrace();
            }
        }

        Stats.printElapsedTime(start, "..bounding box min is " + bbox.getMin().toString());

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
                if(propsStats.containsKey(prop + "_N")){
                    System.out.println("Error: two columns inside input files share the same name");
                    System.exit(0);
                }

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
                if (fileType == FileType.PHOTOGRAMMETRIC) {
                    p = new Point(
                            fileType, Double.parseDouble(token[0]),
                            Double.parseDouble(token[1]) ,
                            Double.parseDouble(token[2]),
                            Integer.parseInt(token[3]),
                            Integer.parseInt(token[4]),
                            Integer.parseInt(token[5]));
                    //System.out.println("min " + min);
                    //System.out.println("newBboxMin " + newBboxMin);

                    p.move(p.subPoint(coordShift));

                    p.setClassification(PointClassification.parse(Integer.parseInt(token[6].substring(0, 1))));

                // X Y Z Class
                } else if (fileType == FileType.LIDAR) {
                    p = new Point(
                            fileType,
                            Double.parseDouble(token[0]),
                            Double.parseDouble(token[1]) ,
                            Double.parseDouble(token[2]) );

                    p.move(p.subPoint(coordShift));

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
                    double val = Double.parseDouble(token[shift + t]);

                    // TODO: range conversion (now manually set here)
                    if(prop.equalsIgnoreCase("ScanAngleRank")) {
                        val = Math.abs(val);
                    }

                    // add the value inside the point properties array
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

                double val = p.getProp(i);
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
        for(String prop : props)
            propsStats.put(prop+"_std", Math.sqrt(propsStats.get(prop+"_std") / propsStats.get(prop+"_N")));


        ///////////////////////////////////////////////
        // normalize all properties values
        ///////////////////////////////////////////////////////

        // load the config file
        JSONObject config  = eu.fbk.threedom.pcFilter.Main.config;
        JSONArray fileTypes = config.getJSONArray("fileTypes");
        JSONObject fileTypeObj = (JSONObject) fileTypes.get(fileType.ordinal());
        JSONArray classTypes = fileTypeObj.getJSONArray("classTypes");

        // cycle on points
        n = points.head();
        while(n != null) {
            Point p = (Point) n.value();
            // for each property
            for(int i=0; i<props.length; i++) {
                String prop = props[i];

                double val = p.getProp(i);

                // normalize values between 0 and 1
                double x = (2 * (val - propsStats.get(prop+"_mean"))) / propsStats.get(prop+"_std");
                float norm_val = 1 / (1 + (float)Math.exp(-x));
                p.setNormProp(i, norm_val);
                //System.out.println("prop: " + prop + " -> " + val + "(norm. " + norm_val + ")");

                // = 1 / (1 + exp (-2 / st.dev * (val - media) ))

                if(Main.DEBUG) {
                    System.out.println("...." + prop + ": " + val + " -> " + norm_val);
                    //System.out.println("......mean " + propsStats.get(prop+"_mean"));
                    //System.out.println("......std " + propsStats.get(prop+"_std"));
                }
            }

            JSONObject classTypeObj = (JSONObject) classTypes.get(p.getClassification().ordinal());
            String formula = classTypeObj.getString("formula");
            float threshold = classTypeObj.getFloat("threshold");

            p.setScore(evaluateScore(p, formula));
            p.setThreshold(threshold);

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }
    }


    public double evaluateScore(Point p, String formula){
        StringBuilder sb = new StringBuilder();

        //System.out.println("\nWorking on Bug\n..formula : " + formula);

        for(String str : formula.split(" ")){
            // if it is a property, retrieve its value
            if(propsStats.containsKey(str + "_N")) {
                float value = p.getNormProp(getPropertyIndex(p.getType(), str));
                sb.append(String.valueOf(value));
            }else
                sb.append(str);
        }

        //System.out.println("..formula (numbers): " + sb);

        double result = 0;

        try{
            Expression.eval(sb.toString());
        }catch(RuntimeException rte){
            result = 1;
        }

        //return Expression.eval(sb.toString());
        return result;
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

    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pc){
        return vGrid.getPoints(fileType, voxelId, pc);
    }

    public List<Point> getPoints(FileType fileType, int voxelId, boolean scoreCheck){
        return vGrid.getPoints(fileType, voxelId, scoreCheck);
    }

    public List<Point> getPoints(int voxelId){
        return vGrid.getPoints(voxelId);
    }

    /**
     *
     * @param fileType
     * @param voxelId
     * @param pointType
     * @param scoreCheck evaluate the comparison between score and threshold
     * @return
     */
    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType, boolean scoreCheck, boolean verbose){
        return vGrid.getPoints(fileType, voxelId, pointType, scoreCheck, coordShift, verbose);
    }


    public List<Point> getPoints(){
        List<Point> list = new ArrayList<>();

        LlNode n = points.head();
        while(n != null) {
            list.add((Point)n.value());

            // exit condition
            if(!n.hasNext() ) break;
            n = n.next();
        }

        return list;
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

    public List<Point> getPoints(FileType fileType, PointClassification pointType){
        Set<Integer> voxelSet = getVGrid().getVoxels(fileType, pointType);

        if(voxelSet == null) return null;
        List<Point> points = new ArrayList<>();

        // extract values from voxels
        for (int v : voxelSet)
            points.addAll( (ArrayList<Point>) getPoints(fileType, v, pointType) );

        return points;
    }

    public int getVoxelId(Point p){
        return getVGrid().getVoxelId(p);
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
