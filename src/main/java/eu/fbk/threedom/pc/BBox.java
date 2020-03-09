package eu.fbk.threedom.pc;

import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class BBox {

    private static final float MIN_VALUE = Float.MIN_VALUE;
    private static final float MAX_VALUE = Float.MAX_VALUE;

    @Setter @Getter private Point max;
    @Setter @Getter private Point min;

    public BBox(){
        max = new Point(MIN_VALUE, MIN_VALUE, MIN_VALUE);
        min = new Point(MAX_VALUE, MAX_VALUE, MAX_VALUE);
    }

    public BBox(Point min, Point max){
        this.max = max;
        this.min = min;
    }

    public Point center(){
        return max.addPoint(min).mulPoint(0.5f);
    }

    public Vector3d size() {
        return max.subPoint(min);
    }

    public boolean contains(Point p)
    {
        if (p.x >= this.min.x && p.x <= this.max.x &&
            p.y >= this.min.y && p.y <= this.max.y &&
            p.z >= this.min.z && p.z <= this.max.z)
            return true;

        return false;
    }

    public void reset(){
        max = new Point(MIN_VALUE, MIN_VALUE, MIN_VALUE);
        min = new Point(MAX_VALUE, MAX_VALUE, MAX_VALUE);
    }

    public String toString(){
        return "bbox [ " + min.toString() + ", " + max.toString() + " ]";
    }

    // Extends the boundaries with a new point
    public void extendTo(Point p)
    {
        min.x = Math.min(min.x, p.x);
        min.y = Math.min(min.y, p.y);
        min.z = Math.min(min.z, p.z);
        max.x = Math.max(max.x, p.x);
        max.y = Math.max(max.y, p.y);
        max.z = Math.max(max.z, p.z);
    }

    public static void main(String[] args){
        BBox b = new BBox();
        b.extendTo(new Point(0, 0, 0));
        b.extendTo(new Point(1, 1, 1));
        b.extendTo(new Point(1, 2, 3));
        b.extendTo(new Point(5, 0, 0));
        System.out.println(b.toString());

        System.out.println("\ncenter: " + b.center().toString());

        System.out.println("\n" + b.contains(new Point(0.5f, 0.5f, 0.5f)));
        System.out.println(b.contains(new Point(-1f, 1f, 2f)));
    }
}
