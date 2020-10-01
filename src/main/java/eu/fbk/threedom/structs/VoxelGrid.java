/**
 * Hybrid Registration (C) 2019 is a command line software designed to
 * analyze, co-register and filter airborne point clouds acquired by LiDAR sensors
 * and photogrammetric algorithm.
 * Copyright (C) 2019  Michele Welponer, mwelponer@gmail.com (Fondazione Bruno Kessler)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <https://www.gnu.org/licenses/> and file GPL3.txt
 *
 * -------------
 * IntelliJ Program arguments:
 * $ContentRoot$/resources/f1.txt $ContentRoot$/resources/f2.txt 1f -w -v
 */
package eu.fbk.threedom.structs;

import eu.fbk.threedom.pc.BBox;
import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.Point;
import eu.fbk.threedom.pc.PointClassification;
import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3d;
import java.util.*;

public class VoxelGrid {

    private BBox bbox;
    private double voxelSide;
    private int width, height, depth;
    private LinkedList points;
    @Getter @Setter private int size;
    @Getter @Setter private double shift;
    @Getter @Setter private Voxel[] voxels;

    private List<Set<Integer>> voxelsList;
    @Getter HashSet<Integer> voxelWithPoints;

    @Setter @Getter private HashMap<String, Float> propsStats;


    public VoxelGrid(LinkedList points, BBox bbox, double voxelSide){
        this.bbox = bbox;
        this.voxelSide = voxelSide;
        this.points = points;
        Vector3d bbSize = bbox.size();

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
    public int getVoxelId(double x, double y, double z) {
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
        double newBboxMinX = bbox.getMin().x - (int)(bbox.getMin().x / voxelSide) * voxelSide;
        double newBboxMinY = bbox.getMin().y - (int)(bbox.getMin().y / voxelSide) * voxelSide;
        double newBboxMinZ = bbox.getMin().z - (int)(bbox.getMin().z / voxelSide) * voxelSide;

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

    public int getVoxelId(Point p){
        return getVoxelId(p.x, p.y, p.z);
    }

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

    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType){
        List<Point> list = new ArrayList<>();
        Voxel vox = getVoxel(voxelId);

        if(vox == null)
            return null;

        LlNode n = vox.getHead();
        while(n != null) {
            Point p = (Point)n.value();
            if(p.getType() == fileType && p.getClassification() == pointType)
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

    public List<Point> getPoints(FileType fileType, int voxelId, PointClassification pointType,
                                 boolean scoreCheck, Point coordShift, boolean verbose){
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
                    if(verbose) {
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

    public Set<Integer> getVoxels(FileType fileType, String[] pointTypes){
        Set<Integer> voxels = getVoxels(fileType);

        for(String cls : pointTypes)
            voxels.retainAll(getVoxels(fileType, PointClassification.valueOf(cls)));

        return voxels;
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
