package eu.fbk.threedom.pcFilter;

public enum PointClassification {

//    C0(0),
//    C1(1),
//    C2(2),
    C0(0),
    C1(1),
    C2(2),
    C3(3),
    C4(4),
    C5(5),
    C6(6),
    C7(7),
    C8(8),
    C9(9);

    public int type;

    PointClassification(int type){
        this.type = type;
    }

    public static PointClassification parse(int type){
        switch (type) {
            case 0: return C0; //C0;
            case 1: return C1; //C1;
            case 2: return C2; //C2;
            case 3: return C3;
            case 4: return C4;
            case 5: return C5;
            case 6: return C6;
            case 7: return C7;
            case 8: return C8;
            case 9: return C9;
            default: return null;
        }
    }
}
