package eu.fbk.threedom.pcFilter;

public enum FileType {

    PHOTOGRAMMETRIC(0),
    LYDAR(1);

    public int type;

    FileType(int type){
        this.type = type;
    }

    public static FileType parse(int type){
        switch (type) {
            case 0: return PHOTOGRAMMETRIC;
            case 1: return LYDAR;
            default: return null;
        }
    }
}
