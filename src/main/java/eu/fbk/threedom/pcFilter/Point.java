package eu.fbk.threedom.pcFilter;

import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3f;

public class Point extends Vector3f {

    @Setter @Getter private int r;
    @Setter @Getter private int g;
    @Setter @Getter private int b;

    @Getter @Setter private float intensity;
    @Getter @Setter private int type;


    public Point(Vector3f v){
        super.x = v.x; super.y = v.y; super.z = v.z;
    }

    public Point(float x, float y, float z) {
        super.x = x; super.y = y; super.z = z;
        this.r = 0; this.g = 0; this.b = 0;
    }

    public Point(float x, float y, float z, int r, int g, int b) {
        super.x = x; super.y = y; super.z = z;
        this.r = r; this.g = g; this.b = b;
    }

    public Point(int type, float x, float y, float z, int r, int g, int b) {
        this.type = type;
        super.x = x; super.y = y; super.z = z;
        this.r = r; this.g = g; this.b = b;
    }

    public String toString(){
        return "point(" + x + ", " + y + ", " + z + ")";
    }

    public String toString(boolean rgb){
        if(rgb)
            return "point(" + x + ", " + y + ", " + z + "), rgb " + r + ":"+ g + ":" + b;

        return toString();
    }

    public float length(Point p){
        return (float)Math.sqrt(this.dot(p));
    }

    public Point addPoint(Point p){
        return  new Point(this.x + p.x, this.y + p.y, this.z + p.z);
    }

    public Point subPoint(Point p){
        return  new Point(this.x - p.x, this.y - p.y, this.z - p.z);
    }

    public Point mulPoint(float s){
        return  new Point(this.x * s, this.y * s, this.z * s);
    }

    public Point divPoint(float s){
        return  new Point(this.x * ( 1.0f / s ), this.y * ( 1.0f / s ), this.z * ( 1.0f / s ));
    }

    public static void main(String[] args){
        Point p = new Point(1, 2, 3);
        p.setIntensity(666.0f);

        System.out.println(p.toString());
        System.out.println("\tintensity: " + p.getIntensity());

        Point p1 = new Point(1, 1, 1);
        Point p2 = new Point(2, 2, 2);
        System.out.println("\n" + p1.toString());
        System.out.println(p2.toString());
        System.out.println("\tdist: " + p1.length(p2));
    }
}
