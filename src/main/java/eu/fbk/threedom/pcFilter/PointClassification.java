package eu.fbk.threedom.pcFilter;

public enum PointClassification {

    ROOF(0),
    FACADE(1),
    STREET(2);

    public int type;

    PointClassification(int type){
        this.type = type;
    }

    public static PointClassification parse(int type){
        switch (type) {
            case 0: return ROOF;
            case 1: return FACADE;
            case 2: return STREET;
            default: return null;
        }
    }
}
