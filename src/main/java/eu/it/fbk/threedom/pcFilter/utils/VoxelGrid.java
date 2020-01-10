package eu.it.fbk.threedom.pcFilter.utils;

import eu.it.fbk.threedom.pcFilter.BBox;
import eu.it.fbk.threedom.pcFilter.Point;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3f;

public class VoxelGrid {

    private BBox bbox;
    private float voxelSide;
    private int width, height, depth;

    @Getter @Setter private Voxel[] voxels;


    public VoxelGrid(BBox bbox, float voxelSide){
        this.bbox = bbox;
        this.voxelSide = voxelSide;
        Vector3f size = bbox.size();

        width = (int) (size.x / voxelSide + 1);
        height = (int) (size.y / voxelSide + 1);
        depth = (int) (size.z / voxelSide + 1);

        voxels = new Voxel[getNumVoxels()];
    }

    public int getNumVoxels(){
        return width * height * depth;
    }

    public int id(int x, int y, int z) {
        Vector3f size = bbox.size();
        return x + y * width + z * width*height;
    }

    public int getVoxelId(float x, float y, float z) {
        int xv = (int) ((x - bbox.getMin().x) /voxelSide);
        int yv = (int) ((y - bbox.getMin().y) /voxelSide);
        int zv = (int) ((z - bbox.getMin().z) /voxelSide);
        int key = id(xv, yv, zv);
        System.out.println("key: " + key);
        if (key < 0 || key >= getNumVoxels()) return -1;

        return key;
    }

    public Voxel getVoxel(float x, float y, float z) {
        int xv = (int) ((x - bbox.getMin().x) /voxelSide);
        int yv = (int) ((y - bbox.getMin().y) /voxelSide);
        int zv = (int) ((z - bbox.getMin().z) /voxelSide);
        int key = id(xv, yv, zv);
        System.out.println("key: " + key);
        if (key < 0 || key >= getNumVoxels()) return null;

        return voxels[key];
    }

    public static void main(String[] args){
        BBox bb = new BBox();
        bb.extendTo(new Point(0, 0, 0));
        bb.extendTo(new Point(5, 2, 3));
        System.out.println(bb.toString());

        VoxelGrid vg = new VoxelGrid(bb, 0.5f);
        System.out.println("\nvoxel id: " + vg.getVoxel(0.7f, 0.3f, 0.3f));
    }
}
