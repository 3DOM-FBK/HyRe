package eu.it.fbk.threedom.pcFilter;

import eu.it.fbk.threedom.pcFilter.utils.LinkedList;
import eu.it.fbk.threedom.pcFilter.utils.LlNode;
import eu.it.fbk.threedom.pcFilter.utils.VoxelGrid;
import eu.it.fbk.threedom.pcFilter.utils.Voxel;

import javax.vecmath.Vector3f;
import java.util.List;

public class PcFilter {

    private List<String> data;
    private BBox bbox;
    private float voxelSide;
    private VoxelGrid vol;

    public PcFilter(List<String> data, float voxelSide) {
        this.data = data;
        this.bbox = new BBox();
        this.voxelSide = voxelSide;
    }

    public void parseData(){
        // parse header (first row)
        String line = data.get(0);
        String[] token = line.split(" ");
        token[0] = token[0].replace("/", "");

        for(String s : token)
            System.out.print(s + ", ");
        System.out.println("\n");

        LinkedList ll = new LinkedList();

        for (int i = 0; i < 10 /*data.size() - 1*/ ; i++) {
            token = data.get(i+1).split(" ");
            float x, y, z;
            int r, g, b;
            for (int j = 0; j < token.length; j++)
                System.out.print(token[j] + " " + ( (j == token.length-1) ? "\n" : "")) ;

            x = Float.parseFloat(token[0]); y = Float.parseFloat(token[1]); z = Float.parseFloat(token[2]);
            r = Integer.parseInt(token[3]); g = Integer.parseInt(token[4]); b = Integer.parseInt(token[5]);
            Point p = new Point(x, y, z, r, g, b);
            ll.addAtEnd(p);

            // update bounding box with the new point
            bbox.extendTo(p);
        }
        System.out.println("\n" + ll.toString());


        // istantiate the voxel volume
        //VoxelGrid vol = new VoxelGrid(getNumVoxels(bbox.size()));
        vol = new VoxelGrid(bbox, 0.1f);


        // iterate on the ll
        LlNode n = ll.head();
        while(n != null) {
            System.out.println(n.toString());


            int id = vol.getVoxelId(((Point)n.value()).x, ((Point)n.value()).y, ((Point)n.value()).z);

            if(id >= 0 && id < vol.getNumVoxels()) {

                if(vol.getVoxels()[id] == null)
                {
                    Voxel vox = new Voxel(id);
                    vox.setHead(n);
                    vol.getVoxels()[id] = vox;
                }
                else
                {
                    vol.getVoxels()[id].getTail().setNext(n);
                }

                vol.getVoxels()[id].setTail(n);
            }

            if(!n.hasNext()) break;
            n = n.next();
        }

        // iterate on the ll and update voxel array
//        for (VoxelPoint* it = (VoxelPoint*) points.begin(); it != NULL; it = it->next) {
//            float x = it->data->data.x;
//            float y = it->data->data.y;
//            float z = it->data->data.z;
//            Voxel* v = getVoxel(x, y, z);
//
//            // Create voxel if needed
//            if (v->numPoints == 0) {
//                v->start = it;
//                actualVoxelNum++;
//            } else {
//                v->end->next = it;
//            }
//
//            v->end = it;
//            v->numPoints++;
//        }
    }

    public Voxel getVoxel(Point p){
        return vol.getVoxel(p.x, p.y, p.z);
    }

    public String toString(){
        Voxel[] voxels = vol.getVoxels();

        for (Voxel v : voxels){
            System.out.println("/nvoxel " + v.getId());

            LlNode n = v.getHead();
            while(n != null) {
                System.out.println(n.toString());

                if(!n.hasNext()) break;
                n = n.next();
            }
        }

        return null;
    }

//    public static void main(String[] args){
//    }
}
