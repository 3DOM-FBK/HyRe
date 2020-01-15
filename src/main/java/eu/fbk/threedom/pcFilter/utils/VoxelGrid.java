package eu.fbk.threedom.pcFilter.utils;

import eu.fbk.threedom.pcFilter.Main;
import eu.fbk.threedom.pcFilter.BBox;
import eu.fbk.threedom.pcFilter.Point;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3f;

public class VoxelGrid {

    private BBox bbox;
    private float voxelSide;
    private int width, height, depth;
    private LinkedList points;
    @Getter @Setter private int size;
    @Getter @Setter private Voxel[] voxels;


    public VoxelGrid(LinkedList points, BBox bbox, float voxelSide){
        this.bbox = bbox;
        this.voxelSide = voxelSide;
        this.points = points;
        Vector3f bbSize = bbox.size();

        width = (int) (bbSize.x / voxelSide + 1);
        height = (int) (bbSize.y / voxelSide + 1);
        depth = (int) (bbSize.z / voxelSide + 1);

        this.size = width * height * depth;

        voxels = new Voxel[this.size];

        generatVoxels();
    }

    public void generatVoxels(){
        if(Main.DEBUG)
            System.out.println("\ngenerate Voxels (" + size + ")");

        ///////////////////////////////////////////////////////
        // iterate on the linked list and update/create voxels
        ///////////////////////////////////////////////////////
        LlNode n = points.head();
        while(n != null) {
            Point p = (Point)n.value();
//            if(Main.DEBUG) System.out.println("\t" + n.toString());

            int id = getVoxelId(p.x, p.y, p.z);
//            if(Main.DEBUG) System.out.println("\t" + ".. goes in voxel " + id);

            if(id >= 0 && id < this.size) {
                if(getVoxel(id) == null) {
//                    if(Main.DEBUG) System.out.println("\t.. create voxel " + id);
                    Voxel vox = new Voxel(id);
                    vox.setHead(n); vox.setTail(n);
                    setVoxel(id, vox);
                } else
                    getVoxel(id).getTail().setNext(n);

                getVoxel(id).setTail(n);
            }

            if(!n.hasNext()) break;
            n = n.next();
        }
    }


    public int id(int x, int y, int z) {
        return x + y * width + z * width*height;
    }

    /**
     * return the id of the voxel containing the coordinate x, y, z
     * voxel are count starting from id = 0
     * @param x
     * @param y
     * @param z
     * @return
     */
    public int getVoxelId(float x, float y, float z) {
        int xv = (int) ((x - bbox.getMin().x) /voxelSide);
        int yv = (int) ((y - bbox.getMin().y) /voxelSide);
        int zv = (int) ((z - bbox.getMin().z) /voxelSide);
        int key = id(xv, yv, zv);
        //System.out.println("key: " + key);
        if (key < 0 || key >= size) return -1;

        return key;
    }

    public Voxel getVoxel(float x, float y, float z) {
        int xv = (int) ((x - bbox.getMin().x) /voxelSide);
        int yv = (int) ((y - bbox.getMin().y) /voxelSide);
        int zv = (int) ((z - bbox.getMin().z) /voxelSide);
        int key = id(xv, yv, zv);
        //System.out.println("key: " + key);
        if (key < 0 || key >= size) return null;

        return voxels[key];
    }

    public Voxel getVoxel(int key) {
        return voxels[key];
    }

    public Voxel setVoxel(int key, Voxel v) {
        return voxels[key] = v;
    }

    public static void main(String[] args){
        Point p1 = new Point(0, 0, 0);
        Point p2 = new Point(3, 2, 3);

        LinkedList points = new LinkedList();
        points.addAtBeginning(p1);
        points.addAtBeginning(p2);

        BBox bbox = new BBox(p1, p2);
        System.out.println("\nbbox\n.." + bbox.toString());

        VoxelGrid vg = new VoxelGrid(points, bbox, 1f);
        System.out.println("\nvoxel id: " + vg.getVoxelId(2.5f, 0.5f, 1.5f));
    }
}
