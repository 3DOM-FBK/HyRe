package eu.it.fbk.threedom.pcFilter;

import eu.it.fbk.threedom.pcFilter.utils.LinkedList;
import eu.it.fbk.threedom.pcFilter.utils.LlNode;
import eu.it.fbk.threedom.pcFilter.utils.VoxelGrid;
import eu.it.fbk.threedom.pcFilter.utils.Voxel;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

public class PcFilter {

    private List<String> data;
    private BBox bbox;
    private float voxelSide;
    private VoxelGrid vol;
    @Getter private int numVoxel;

//    // timer
//    private static long start;
//    private static long time;

    public PcFilter(List<String> data) {
        this.data = data;
        this.bbox = new BBox();
    }

    public void parseData(float voxelSide){
        this.voxelSide = voxelSide;

        // parse header (first row)
        String line = data.get(0);
        String[] token = line.split(" ");
        token[0] = token[0].replace("/", "");

        System.out.print("..header: ");
        for(String s : token)
            System.out.print(s + " ");
        System.out.println("\n");

        LinkedList ll = new LinkedList();

//        start = System.currentTimeMillis();
        for (int i = 0; i < data.size() - 1 ; i++) {
            // check if it is a comment or empty line
            if(data.get(i+1).startsWith("//") || data.get(i+1).isEmpty())
                continue;

            token = data.get(i+1).split(" ");
            float x, y, z;
            int r, g, b;
//            if(Main.DEBUG)
//                for (int j = 0; j < token.length; j++)
//                    System.out.print(token[j] + " " + ( (j == token.length-1) ? "\n" : "")) ;

            x = Float.parseFloat(token[0]); y = Float.parseFloat(token[1]); z = Float.parseFloat(token[2]);
            r = Integer.parseInt(token[3]); g = Integer.parseInt(token[4]); b = Integer.parseInt(token[5]);
            Point p = new Point(x, y, z, r, g, b);
            ll.addAtBeginning(p);

            // update bounding box with the new point
            bbox.extendTo(p);
        }
//        printElapsedTime(start, "..file parsed");


        if(Main.DEBUG) {
            //System.out.println("\nlinkedList\n\t" + ll.toString());
            System.out.println("\nbbox\n.." + bbox.toString());
        }


        // instantiate the voxel grid
        vol = new VoxelGrid(bbox, this.voxelSide);
        numVoxel = vol.getSize();

        if(Main.DEBUG)
            System.out.println("\nbuild Voxels (" + numVoxel + ")");

        // iterate on the linked list and update/create voxels
        LlNode n = ll.head();
//        start = System.currentTimeMillis();
        while(n != null) {
            Point p = (Point)n.value();
//            if(Main.DEBUG)
//                System.out.println("\t" + n.toString());

            int id = vol.getVoxelId(p.x, p.y, p.z);
//            if(Main.DEBUG)
//                System.out.println("\t" + ".. goes in voxel " + id);

            if(id >= 0 && id < vol.getSize()) {
                if(vol.getVoxel(id) == null) {
//                    if(Main.DEBUG)
//                        System.out.println("\t.. create voxel " + id);
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
//        printElapsedTime(start, "..voxel grid updated");
    }

    public List<Point> getVoxelPoints(int id){
//        if(Main.DEBUG)
//            System.out.println("\nget voxels (" + id + ")");

        List<Point> list = new ArrayList<>();

        Voxel vox = vol.getVoxel(id);

        if(vox == null)
            return null;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();
            //System.out.println("\t\tpoint " + p.toString());
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

//        System.out.println("\nvoxelGrid");

        for (Voxel v : voxels){
            if(v == null) continue;

            sb.append("\n\tvoxel " + v.getId());
//            System.out.println("\tvoxel " + v.getId());

            LlNode n = v.getHead();
            while(n != null) {
                sb.append("\n\t\tpoint " + n.value().toString());
//                System.out.println("\t\tpoint " + n.value().toString());


                // exit condition
                if(!n.hasNext() || n == v.getTail()) break;
                n = n.next();
            }
        }

        return sb.toString();
    }

//    public static String convertSecondsToHMmSs(long seconds) {
//        long s = seconds % 60;
//        long m = (seconds / 60) % 60;
//        long h = (seconds / (60 * 60)) % 24;
//        return String.format("%dh:%02dm:%02ds", h,m,s);
//    }
//
//    public void printElapsedTime(long start, String message){
//        time = (System.currentTimeMillis() - start) / 1000;
//        System.out.println(message + " (" + convertSecondsToHMmSs(time) + ")");
//    }
}
