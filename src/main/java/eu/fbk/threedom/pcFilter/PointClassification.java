package eu.fbk.threedom.pcFilter;

public enum PointClassification {

    ROOF(0),
    FACADE(1),
    STREET(2),
    C3(3),
    C4(4),
    C5(5),
    C6(6);


    public int type;

    PointClassification(int type){
        this.type = type;
    }

    public static PointClassification parse(int type){
        switch (type) {
            case 0: return ROOF;
            case 1: return FACADE;
            case 2: return STREET;
            case 3: return C3;
            case 4: return C4;
            case 5: return C5;
            case 6: return C6;
            default: return null;
        }
    }
}
