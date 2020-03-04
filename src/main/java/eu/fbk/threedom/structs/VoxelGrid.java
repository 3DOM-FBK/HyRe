package eu.fbk.threedom.structs;

import eu.fbk.threedom.pc.BBox;
import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import eu.fbk.threedom.pcFilter.Main;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3f;
import java.util.*;

public class VoxelGrid {

    private BBox bbox;
    private float voxelSide;
    private int width, height, depth;
    private LinkedList points;
    @Getter @Setter private int size;
    @Getter @Setter private float shift;
    @Getter @Setter private Voxel[] voxels;

    private List<Set<Integer>> voxelsList;
    @Getter HashSet<Integer> voxelWithPoints;

    @Setter @Getter private HashMap<String, Float> propsStats;


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

        voxelsList = new ArrayList();
        for(int i = 0; i < PointClassification.values().length * 2; i++)
            voxelsList.add(new HashSet<Integer>());

        voxelWithPoints = new HashSet<>();

        generateVoxels();
    }

    public void generateVoxels(){
        System.out.println("\ngenerate voxel structure\n..voxelSide: " + voxelSide);
        System.out.println("..voxelGrid dimension " + width + " x " + height + " x " + depth);
        System.out.println("..voxels to generate " + size);

        ///////////////////////////////////////////////////////
        // iterate on the linked list and update/create voxels
        ///////////////////////////////////////////////////////
        LlNode n = points.head();
        while(n != null) {
            Point p = (Point)n.value();
            int id = getVoxelId(p.x, p.y, p.z);

            if(id != -1) {
                if(!voxelWithPoints.contains(id)) voxelWithPoints.add(id);

                FileType fileType = p.getType();

                switch (p.getClassification()) {
                    case C0:
                        if (fileType.type == 0) voxelsList.get(0).add(id); else voxelsList.get(3).add(id);
                        break;
                    case C1:
                        if (fileType.type == 0) voxelsList.get(1).add(id); else voxelsList.get(4).add(id);
                        break;
                    case C2:
                        if (fileType.type == 0) voxelsList.get(2).add(id); else voxelsList.get(5).add(id);
                        break;
                }

                if (id >= 0 && id < this.size) {
                    if (getVoxel(id) == null) {
                        Voxel vox = new Voxel(id);
                        vox.setHead(n);
                        vox.setTail(n);
                        setVoxel(id, vox);
                    } else getVoxel(id).getTail().setNext(n);

                    getVoxel(id).setTail(n);
                }
            }

            if(!n.hasNext()) break;
            n = n.next();
        }

        System.out.println("..voxels with at least one point " + voxelWithPoints.size());

//        Set<Integer> totalSet = voxelsList.get(0);
//        for (int i=1; i < voxelsList.size(); i++)
//            for(int j : voxelsList.get(i))
//                if(!totalSet.contains(j))
//                    totalSet.add(j);
//
//        System.out.println("..voxels with at least one point " + totalSet.size());
    }


    public int id(int x, int y, int z) {
        return x + (y * width) + (z * width*height);
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
//        // old lake version
//        int xv = (int) (((x - bbox.getMin().x) / voxelSide));
//        int yv = (int) (((y - bbox.getMin().y) / voxelSide));
//        int zv = (int) (((z - bbox.getMin().z) / voxelSide));

//        // test from roberto suggestion
//        int xv = (int) (((x - bbox.getMin().x) / voxelSide)+0.5);
//        int yv = (int) (((y - bbox.getMin().y) / voxelSide)+0.5);
//        int zv = (int) (((z - bbox.getMin().z) / voxelSide)+0.5);

        // mike (correct?) version
        // find the coordinates of where to move the min of the bounding box
        float newBboxMinX = bbox.getMin().x - (int)(bbox.getMin().x / voxelSide) * voxelSide;
        float newBboxMinY = bbox.getMin().y - (int)(bbox.getMin().y / voxelSide) * voxelSide;
        float newBboxMinZ = bbox.getMin().z - (int)(bbox.getMin().z / voxelSide) * voxelSide;

        int xv = (int) ( (x - bbox.getMin().x + newBboxMinX) / voxelSide);
        int yv = (int) ( (y - bbox.getMin().y + newBboxMinY) / voxelSide);
        int zv = (int) ( (z - bbox.getMin().z + newBboxMinZ) / voxelSide);

//        // final version: we move the bounding box at the beginning
//        int xv = (int) ( x / voxelSide);
//        int yv = (int) ( y / voxelSide);
//        int zv = (int) ( z / voxelSide);

        int key = id(xv, yv, zv);
        //System.out.println("key: " + key);
        if (key < 0 || key >= size) return -1;

        return key;
    }

//    public Voxel getVoxel(float x, float y, float z) {
////        int xv = (int) ((x - bbox.getMin().x) /voxelSide);
////        int yv = (int) ((y - bbox.getMin().y) /voxelSide);
////        int zv = (int) ((z - bbox.getMin().z) /voxelSide);
//        int xv = (int) (x / voxelSide);
//        int yv = (int) (y / voxelSide);
//        int zv = (int) (z / voxelSide);
//
//        int key = id(xv, yv, zv);
//        //System.out.println("key: " + key);
//        if (key < 0 || key >= size) return null;
//
//        return voxels[key];
//    }

    public Voxel getVoxel(int key) {
        return voxels[key];
    }

    public Voxel setVoxel(int key, Voxel v) {
        return voxels[key] = v;
    }

    public List<Point> getPoints(FileType fileType){
        Set voxelSet = getVoxels(fileType);
        List<Point> list = new ArrayList<>();

        for(Object v : voxelSet)
            list.addAll(getPoints(fileType, (int)v));

        return list;
    }

    public List<Point> getPoints(int voxelId){
        List<Point> list = new ArrayList<>();
        Voxel vox = getVoxel(voxelId);

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

    /**
     *
     * @param fileType defines if it is a photogrammetric point (0) or a lidar point
     * @param voxelId
     * @return
     */
    public List<Point> getPoints(FileType fileType, int voxelId){
        List<Point> list = new ArrayList<>();
        Voxel vox = getVoxel(voxelId);

        if(vox == null)
            return null;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();
            if(p.getType() == fileType)
                list.add(p);

            // exit condition
            if(!n.hasNext() || n == vox.getTail()) break;
            n = n.next();
        }

        return list;
    }

    public List<Point> getPoints(FileType fileType, int voxelId, boolean scoreCheck){
        List<Point> list = new ArrayList<>();
        Voxel vox = getVoxel(voxelId);

        if(vox == null)
            return null;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();

            if(scoreCheck) {
                if (p.getType() == fileType && p.getScore() <= p.getThreshold())
                    list.add(p);
            }else
                if(p.getType() == fileType)
                    list.add(p);

            // exit condition
            if(!n.hasNext() || n == vox.getTail()) break;
            n = n.next();
        }

        return list;
    }

    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType, boolean scoreCheck, Point coordShift){
        List<Point> list = new ArrayList<>();
        Voxel vox = getVoxel(voxelId);

        if(vox == null)
            return null;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();

            if(scoreCheck) {
                if (p.getType() == fileType && p.getClassification() == pointType && p.getScore() <= p.getThreshold()) {
                    list.add(p);
                    if(Main.DEBUG) {
                        System.out.println("........" + p.toString(coordShift));
                        System.out.println("..........fileType: " + p.getType());
                        System.out.println("..........fileClass: " + p.getClassification());
                        System.out.println("..........score: " + p.getScore());
                        System.out.println("..........threshold: " + p.getThreshold());
                    }
                }
            }else
                if(p.getType() == fileType && p.getClassification() == pointType) {
                    list.add(p);
                }

            // exit condition
            if(!n.hasNext() || n == vox.getTail()) break;
            n = n.next();
        }

        return list;
    }

//    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType, float threshold){
//        List<Point> list = new ArrayList<>();
//        Voxel vox = getVoxel(voxelId);
//
//        if(vox == null)
//            return null;
//
//        LlNode n = vox.getHead();
//        while(n != null) {
//            Point p = (Point)n.value();
//            if(p.getType() == fileType && p.getClassification() == pointType && p.getScore() <= threshold)
//                list.add(p);
//
//            // exit condition
//            if(!n.hasNext() || n == vox.getTail()) break;
//            n = n.next();
//        }
//
//        return list;
//    }

    public int getNumberOfPoint(FileType fileType, int voxelId, PointClassification pointType){
        Voxel vox = getVoxel(voxelId);

        if(vox == null)
            return -1;

        int count = 0;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();
            if(p.getType() == fileType && p.getClassification() == pointType)
                count++;

            // exit condition
            if(!n.hasNext() || n == vox.getTail()) break;
            n = n.next();
        }

        return count;
    }

    public Set<Integer> getVoxels(FileType[] fileTypes){
        Set voxelsSet = new LinkedHashSet<Integer>();

        for(FileType ft : fileTypes) {
            if (ft == FileType.PHOTOGRAMMETRIC) {
                voxelsSet.addAll(voxelsList.get(0));
                voxelsSet.addAll(voxelsList.get(1));
                voxelsSet.addAll(voxelsList.get(2));
            }

            if (ft == FileType.LIDAR) {
                voxelsSet.addAll(voxelsList.get(3));
                voxelsSet.addAll(voxelsList.get(4));
                voxelsSet.addAll(voxelsList.get(5));
            }
        }

        return voxelsSet;
    }

    public Set<Integer> getVoxels(FileType fileType){
        Set voxelsSet = new LinkedHashSet<Integer>();

        if(fileType == FileType.PHOTOGRAMMETRIC){
            voxelsSet.addAll(voxelsList.get(0));
            voxelsSet.addAll(voxelsList.get(1));
            voxelsSet.addAll(voxelsList.get(2));
        }

        if(fileType == FileType.LIDAR){
            voxelsSet.addAll(voxelsList.get(3));
            voxelsSet.addAll(voxelsList.get(4));
            voxelsSet.addAll(voxelsList.get(5));
        }

        return voxelsSet;
    }

    public Set<Integer> getVoxels(FileType fileType, PointClassification pointType){
        switch (pointType){
            case C0:
                if(fileType == FileType.PHOTOGRAMMETRIC) return voxelsList.get(0); else return voxelsList.get(3);
            case C1:
                if(fileType == FileType.PHOTOGRAMMETRIC) return voxelsList.get(1); else return voxelsList.get(4);
            case C2:
                if(fileType == FileType.PHOTOGRAMMETRIC) return voxelsList.get(2); else return voxelsList.get(5);
            default: return null;
        }
    }

    public static void main(String[] args){
//        Point p1 = new Point(0, 0, 0);
//        Point p2 = new Point(3, 2, 3);
//
//        LinkedList points = new LinkedList();
//        points.addAtBeginning(p1);
//        points.addAtBeginning(p2);
//
//        BBox bbox = new BBox(p1, p2);
//        System.out.println("\nbbox\n.." + bbox.toString());
//
//        VoxelGrid vg = new VoxelGrid(points, bbox, 1f);
//        System.out.println("\nvoxel id: " + vg.getVoxelId(2.5f, 0.5f, 1.5f));
    }
}
