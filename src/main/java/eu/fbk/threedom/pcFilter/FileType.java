package eu.fbk.threedom.pcFilter;

import lombok.Getter;
import lombok.Setter;

public enum FileType {

    PHOTOGRAMMETRIC(0),
    LYDAR(1);

    public int type;

    @Getter @Setter
    public String[] props;

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
