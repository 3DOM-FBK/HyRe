package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pcFilter.utils.LinkedList;
import eu.fbk.threedom.pcFilter.utils.LlNode;
import eu.fbk.threedom.pcFilter.utils.VoxelGrid;
import eu.fbk.threedom.pcFilter.utils.Voxel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PcFilter {

    private List<String> file1Data, file2Data;
    private float voxelSide;
    @Setter @Getter private VoxelGrid vGrid;

    private BBox bbox;
    private LinkedList points;

    private String[][] header, properties;
    private HashMap<String, Float> propsStats;

    public PcFilter(List<String> file1Data, List<String> file2Data, float voxelSide) {
        this.file1Data = file1Data;
        this.file2Data = file2Data;
        this.voxelSide = voxelSide;

        bbox = new BBox();
        points = new LinkedList();
        this.header = new String[2][];
        this.properties = new String[2][];
        this.propsStats = new HashMap<>();

        // parse text files
        parseData(file1Data, 0);
        // head -> n -> .. -> n -> null
        updateStatistics(0, null);
        LlNode endNode = points.head();

        parseData(file2Data, 1);
        // head -> n -> .. -> n -> endNode -> n .. -> n -> null
        updateStatistics(1, endNode);

        if(Main.DEBUG) {
            System.out.println("\nstatistics");
            propsStats.entrySet().forEach(entry->{
                System.out.println(".." + entry.getKey() + " " + entry.getValue());
            });
        }

        // instantiate the voxel grid
        vGrid = new VoxelGrid(points, bbox, this.voxelSide);
    }

    public void parseData(List<String> data, int fileType){
        if(Main.DEBUG)
            System.out.println("\nparse file " + fileType);

        ///////////////////////////////////////////////
        // parse header if present (first row)
        ///////////////////////////////////////////////////////
        String line = data.get(0);

        if(line.startsWith("// "))
            line = line.replace("// ",  "");
        else if(line.startsWith("//"))
            line = line.replace("//",  "");
        else line = "";

        String[] token = line.split(" ");
        // arrays of properties names
        String[] props = Arrays.copyOfRange(token, 6, token.length); // cut x y z r g b
        this.header[fileType] = token;
        this.properties[fileType] = props;

        if(Main.DEBUG) {
            System.out.println("..header " + Arrays.toString(header[fileType]));
//            System.out.println("..properties " + this.properties[fileType]);
        }

        // initialize statistics
        for(String prop : props){
            propsStats.put(prop+"_N", 0f);
            propsStats.put(prop+"_sum", 0f);
            propsStats.put(prop+"_mean", 0f);
            propsStats.put(prop+"_std", 0f);
        }

        ///////////////////////////////////////////////
        // parse all data
        ///////////////////////////////////////////////////////
        for (int i = 0; i < data.size() - 1 ; i++) {
            // check if it is a comment or empty line
            if(data.get(i+1).startsWith("//") || data.get(i+1).isEmpty()) continue;

            token = data.get(i+1).split(" ");
            float x, y, z;
            int r, g, b;

            x = Float.parseFloat(token[0]); y = Float.parseFloat(token[1]); z = Float.parseFloat(token[2]);
            r = Integer.parseInt(token[3]); g = Integer.parseInt(token[4]); b = Integer.parseInt(token[5]);

            Point p = new Point(fileType, x, y, z, r, g, b);

            // for each property
            for(int t=6; t < header[fileType].length; t++) {
                // add the new value
                String prop = header[fileType][t];
                float val = Float.parseFloat(token[t]);
                p.setProp(prop, val); //System.out.println(prop + " " + val);

                // update sum and arithmetic mean
                propsStats.put(prop+"_N", propsStats.get(prop+"_N") + 1);
                propsStats.put(prop+"_sum", propsStats.get(prop+"_sum") + val);
                propsStats.put(prop+"_mean", propsStats.get(prop+"_sum") / propsStats.get(prop+"_N") );
            }

            points.addAtBeginning(p);

            // update bounding box with the new point
            bbox.extendTo(p);
        }

        if(Main.DEBUG) {
            //System.out.println("\n.. " + ll.toString());
            System.out.println(".." + bbox.toString());
        }
    }

    public void updateStatistics(int fileType, LlNode exitNode){
        LlNode n = points.head();
        int N = 0;
        String[] props = this.properties[fileType];

        // cycle on points
        while(n != null) {
            N++;
            Point p = (Point)n.value();

            // for each property
            for(String prop : props) {
                float val = p.getProp(prop);
                float mean = propsStats.get(prop+"_mean");
                float std =  (float)Math.pow(val - mean, 2);
                propsStats.put(prop+"_std", propsStats.get(prop+"_std") + std);
            }

            // exit condition
            if(!n.hasNext() || n.next() == exitNode) break;
            n = n.next();
        }

        // evaluate the standard deviation
        for(String prop : props)
            propsStats.put(prop+"_std", (float)Math.sqrt(propsStats.get(prop+"_std") / N));

        // TODO: normalize each property value according to std & mean
//        // cycle on points
//        while(n != null) {
//            N++;
//            Point p = (Point)n.value();
//
//            // for each property
//            for(String prop : props) {

    }


    public List<Point> getPoints(int voxelId){
        List<Point> list = new ArrayList<>();
        Voxel vox = vGrid.getVoxel(voxelId);

        if(vox == null)
            return null;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();
            list.add(p);

            // exit condition
            if(!n.hasNext() || n == vox.getTail()) break;
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
}
