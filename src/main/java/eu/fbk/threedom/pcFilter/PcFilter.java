package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pcFilter.utils.LinkedList;
import eu.fbk.threedom.pcFilter.utils.LlNode;
import eu.fbk.threedom.pcFilter.utils.VoxelGrid;
import eu.fbk.threedom.pcFilter.utils.Voxel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PcFilter {

    private List<String> file1Data, file2Data;
    private float voxelSide;
    @Setter @Getter private VoxelGrid vGrid;

    private BBox bbox;
    private LinkedList points;

    private String[] header;

    public PcFilter(List<String> file1Data, List<String> file2Data, float voxelSide) {
        this.file1Data = file1Data;
        this.file2Data = file2Data;
        this.voxelSide = voxelSide;

        bbox = new BBox();
        points = new LinkedList();
        this.header = new String[2];

        // parse text files
        if(Main.DEBUG)
            System.out.println("\nparse file 1");
        parseData(file1Data, 1);
        if(Main.DEBUG)
            System.out.println("..header " + this.header[0]);

        if(Main.DEBUG)
            System.out.println("\nparse file 2");
        parseData(file2Data, 2);
        if(Main.DEBUG)
            System.out.println("..header " + this.header[1]) ;

        // instantiate the voxel grid
        vGrid = new VoxelGrid(points, bbox, this.voxelSide);
    }

    public void parseData(List<String> data, int fileType){

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
        this.header[fileType - 1] = Arrays.toString(token);


        ///////////////////////////////////////////////
        // parse all data
        ///////////////////////////////////////////////////////
        for (int i = 0; i < data.size() - 1 ; i++) {
            // check if it is a comment or empty line
            if(data.get(i+1).startsWith("//") || data.get(i+1).isEmpty()) continue;

            token = data.get(i+1).split(" ");
            float x, y, z, intensity;
            int r, g, b;
//            if(Main.DEBUG)
//                for (int j = 0; j < token.length; j++)
//                    System.out.print(token[j] + " " + ( (j == token.length-1) ? "\n" : "")) ;

            x = Float.parseFloat(token[0]); y = Float.parseFloat(token[1]); z = Float.parseFloat(token[2]);
            r = Integer.parseInt(token[3]); g = Integer.parseInt(token[4]); b = Integer.parseInt(token[5]);
            Point p = new Point(fileType, x, y, z, r, g, b);
            p.setIntensity(Float.parseFloat(token[6]));
            points.addAtBeginning(p);

            // update bounding box with the new point
            bbox.extendTo(p);
        }

        if(Main.DEBUG) {
            //System.out.println("\n.. " + ll.toString());
            System.out.println(".." + bbox.toString());
        }
    }

//    public void generatVoxels(){
//
//        // instantiate the voxel grid
//        vGrid = new VoxelGrid(bbox, this.voxelSide);
//        numVoxel = vGrid.getSize();
//
//        if(Main.DEBUG)
//            System.out.println("\nbuild Voxels (" + numVoxel + ")");
//
//        ///////////////////////////////////////////////////////
//        // iterate on the linked list and update/create voxels
//        ///////////////////////////////////////////////////////
//        LlNode n = ll.head();
//        while(n != null) {
//            Point p = (Point)n.value();
////            if(Main.DEBUG) System.out.println("\t" + n.toString());
//
//            int id = vGrid.getVoxelId(p.x, p.y, p.z);
////            if(Main.DEBUG) System.out.println("\t" + ".. goes in voxel " + id);
//
//            if(id >= 0 && id < vGrid.getSize()) {
//                if(vGrid.getVoxel(id) == null) {
////                    if(Main.DEBUG) System.out.println("\t.. create voxel " + id);
//                    Voxel vox = new Voxel(id);
//                    vox.setHead(n); vox.setTail(n);
//                    vGrid.setVoxel(id, vox);
//                } else
//                    vGrid.getVoxel(id).getTail().setNext(n);
//
//                vGrid.getVoxel(id).setTail(n);
//            }
//
//            if(!n.hasNext()) break;
//            n = n.next();
//        }
//    }

    public List<Point> getPoints(int voxelId){
//        if(Main.DEBUG) System.out.println("\nget voxels (" + id + ")");
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
