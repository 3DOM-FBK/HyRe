package eu.fbk.threedom.pc;

import lombok.Getter;
import lombok.Setter;

public enum FileType {

    PHOTOGRAMMETRIC(0),
    LIDAR(1);

    public int type;

    @Getter @Setter
    public String[] props;

    FileType(int type){
        this.type = type;
    }

    public static FileType parse(int type){
        switch (type) {
            case 0: return PHOTOGRAMMETRIC;
            case 1: return LIDAR;
            default: return null;
        }
    }
}
