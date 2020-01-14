package eu.it.fbk.threedom.pcFilter;

import eu.it.fbk.threedom.pcFilter.utils.LinkedList;
import eu.it.fbk.threedom.pcFilter.utils.LlNode;
import eu.it.fbk.threedom.pcFilter.utils.VoxelGrid;
import eu.it.fbk.threedom.pcFilter.utils.Voxel;
import lombok.Getter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PcFilter {

    private List<String> file1Data, file2Data;
    private BBox bbox;
    private float voxelSide;
    private VoxelGrid vol;
    private LinkedList ll;
    @Getter private int numVoxel;
    private String[] header;

    public PcFilter(List<String> file1Data, List<String> file2Data, float voxelSide) {
        this.header = new String[2];
        this.file1Data = file1Data;
        this.file2Data = file2Data;
        this.bbox = new BBox();
        this.voxelSide = voxelSide;

        ll = new LinkedList();

        parseData(file1Data, 1);
        if(Main.DEBUG)
            System.out.print("..header: " + this.header[0]);
        parseData(file2Data, 2);
        if(Main.DEBUG)
            System.out.print("..header: " + this.header[1]);

        if(Main.DEBUG) {
            //System.out.println("\nlinkedList\n\t" + ll.toString());
            System.out.println("\nbbox\n.." + bbox.toString());
        }

        generatVoxels();
    }

    public void parseData(List<String> data, int fileType){

        ///////////////////////////////////////////////
        // parse header (first row)
        ///////////////////////////////////////////////////////
        String line = data.get(0);
        String[] token = line.split(" ");
        token[0] = token[0].replace("/", "");
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
            ll.addAtBeginning(p);

            // update bounding box with the new point
            bbox.extendTo(p);
        }
    }

    public void generatVoxels(){

        // instantiate the voxel grid
        vol = new VoxelGrid(bbox, this.voxelSide);
        numVoxel = vol.getSize();

        if(Main.DEBUG)
            System.out.println("\nbuild Voxels (" + numVoxel + ")");

        ///////////////////////////////////////////////////////
        // iterate on the linked list and update/create voxels
        ///////////////////////////////////////////////////////
        LlNode n = ll.head();
        while(n != null) {
            Point p = (Point)n.value();
//            if(Main.DEBUG) System.out.println("\t" + n.toString());

            int id = vol.getVoxelId(p.x, p.y, p.z);
//            if(Main.DEBUG) System.out.println("\t" + ".. goes in voxel " + id);

            if(id >= 0 && id < vol.getSize()) {
                if(vol.getVoxel(id) == null) {
//                    if(Main.DEBUG) System.out.println("\t.. create voxel " + id);
                    Voxel vox = new Voxel(id);
                    vox.setHead(n); vox.setTail(n);
                    vol.setVoxel(id, vox);
                } else
                    vol.getVoxel(id).getTail().setNext(n);

                vol.getVoxel(id).setTail(n);
            }

            if(!n.hasNext()) break;
            n = n.next();
        }
    }

    public List<Point> getPoints(int voxelId){
//        if(Main.DEBUG) System.out.println("\nget voxels (" + id + ")");
        List<Point> list = new ArrayList<>();
        Voxel vox = vol.getVoxel(voxelId);

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
        Voxel[] voxels = vol.getVoxels();

        for (Voxel v : voxels){
            if(v == null) continue;

            sb.append("\n\tvoxel " + v.getId());
            LlNode n = v.getHead();
            while(n != null) {
                sb.append("\n\t\tpoint " + n.value().toString());

                // exit condition
                if(!n.hasNext() || n == v.getTail()) break;
                n = n.next();
            }
        }

        return sb.toString();
    }
}
