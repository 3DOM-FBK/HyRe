package eu.fbk.threedom.pc;

import lombok.Getter;
import lombok.Setter;

import javax.vecmath.Vector3f;

public class Point extends Vector3f {

//    @Setter @Getter private int id;

    @Setter @Getter private int r;
    @Setter @Getter private int g;
    @Setter @Getter private int b;

    //@Getter @Setter private float intensity;
    @Getter @Setter private FileType type; // 0 photogrammetric, 1 lidar
    @Getter @Setter private PointClassification classification; // 0 1 2

    @Getter @Setter private float score;

    private float[] propertiesValues;
    private float[] propertiesNormValues;

    private double[] double_propertiesValues;
    private double[] double_propertiesNormValues;

    public Point(float x, float y, float z) {
        super.x = x; super.y = y; super.z = z;
        this.r = 0; this.g = 0; this.b = 0;

//        propsDictionary = new HashMap<>();
    }

    public Point(/*int id, */FileType type,  float x, float y, float z) {
//        this.id = id;
        this.type = type;
        super.x = x; super.y = y; super.z = z;
        this.r = 0; this.g = 0; this.b = 0;

        propertiesValues = new float[type.getProps().length];
        propertiesNormValues = new float[type.getProps().length];

        double_propertiesValues = new double[type.getProps().length];
        double_propertiesNormValues = new double[type.getProps().length];
    }

    public Point(/*int id,*/ FileType type, float x, float y, float z, int r, int g, int b) {
//        this.id = id;
        this.type = type;
        super.x = x; super.y = y; super.z = z;
        this.r = r; this.g = g; this.b = b;

        propertiesValues = new float[type.getProps().length];
        propertiesNormValues = new float[type.getProps().length];

        double_propertiesValues = new double[type.getProps().length];
        double_propertiesNormValues = new double[type.getProps().length];
    }

    public void move(float x, float y, float z){
        super.x = x; super.y = y; super.z = z;
    }

    public String toStringOutput(boolean normalized, Point min){
        StringBuilder sb = new StringBuilder();

        if(this.type == FileType.PHOTOGRAMMETRIC) {
            sb.append(  String.valueOf(getX() + min.getX()) + " " +
                        String.valueOf(getY() + min.getY()) + " " +
                        String.valueOf(getZ() + min.getZ()) + " " +
                        String.valueOf(getR()) + " " +
                        String.valueOf(getG()) + " " +
                        String.valueOf(getB()) + " "    );
        }

        if(this.type == FileType.LIDAR) {
            sb.append(  String.valueOf(getX() + min.getX()) + " " +
                        String.valueOf(getY() + min.getY()) + " " +
                        String.valueOf(getZ() + min.getZ()) + " "    );
        }

        if(normalized)
            for(float prop : propertiesNormValues)
                sb.append(String.valueOf(prop) + " ");
        else
            for(float prop : propertiesValues)
                sb.append(String.valueOf(prop) + " ");

        return sb.toString();
    }

    public String toStringDoubleOutput(boolean normalized, Point min){
        StringBuilder sb = new StringBuilder();

        if(this.type == FileType.PHOTOGRAMMETRIC) {
            sb.append(  String.valueOf(getX() + min.getX()) + " " +
                    String.valueOf(getY() + min.getY()) + " " +
                    String.valueOf(getZ() + min.getZ()) + " " +
                    String.valueOf(getR()) + " " +
                    String.valueOf(getG()) + " " +
                    String.valueOf(getB()) + " "    );
        }

        if(this.type == FileType.LIDAR) {
            sb.append(  String.valueOf(getX() + min.getX()) + " " +
                    String.valueOf(getY() + min.getY()) + " " +
                    String.valueOf(getZ() + min.getZ()) + " "    );
        }

        if(normalized)
            for(double prop : double_propertiesNormValues)
                sb.append(String.valueOf(prop) + " ");
        else
            for(double prop : double_propertiesValues)
                sb.append(String.valueOf(prop) + " ");

        return sb.toString();
    }

    public String toString(){
        return "point(" + x + ", " + y + ", " + z + ")";
    }

    public String toString(boolean rgb){
        if(rgb)
            return "point(" + x + ", " + y + ", " + z + "), rgb " + r + ":"+ g + ":" + b;

        return toString();
    }

//    public void setProp(String property, Float value){
//        propsDictionary.put(property, value);
//    }
//
//    public float getProp(String property){
//        if(!propsDictionary.containsKey(property))
//            return -Float.MAX_VALUE;
//        return propsDictionary.get(property);
//    }

    public void setProp(int propertyIndex, Float value){
        propertiesValues[propertyIndex] = value;
    }
    public void setNormProp(int propertyIndex, Float value){
        propertiesNormValues[propertyIndex] = value;
    }

    public void setDoubleProp(int propertyIndex, Double value){ double_propertiesValues[propertyIndex] = value;}
    public void setDoubleNormProp(int propertyIndex, Double value){double_propertiesNormValues[propertyIndex] = value;    }

    public float getProp(int propertyIndex){return propertiesValues[propertyIndex];}
    public float getNormProp(int propertyIndex){return propertiesNormValues[propertyIndex];}

    public double getDoubleProp(int propertyIndex){return double_propertiesValues[propertyIndex];}
    public double getDoubleNormProp(int propertyIndex){return double_propertiesNormValues[propertyIndex];}

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
        Point p = new Point(FileType.PHOTOGRAMMETRIC, 1, 2, 3);
        p.setProp(0, 666.0f);

        System.out.println(p.toString());
        System.out.println("\tintensity: " + p.getProp(0));

        Point p1 = new Point(1, 1, 1);
        Point p2 = new Point(2, 2, 2);
        System.out.println("\n" + p1.toString());
        System.out.println(p2.toString());
        System.out.println("\tdist: " + p1.length(p2));
    }
}
