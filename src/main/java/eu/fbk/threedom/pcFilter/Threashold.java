package eu.fbk.threedom.pcFilter;

import lombok.Getter;
import lombok.Setter;

public class Threashold {

    @Setter @Getter
    private FileType filetype;
    @Setter @Getter
    private PointClassification classType;
    @Setter @Getter
    private float value;

    public Threashold(FileType filetype, PointClassification classType, float value){
        this.filetype = filetype;
        this.classType = classType;
        this.value = value;
    }

    public String toString(){
        return filetype.name() + ":" + classType.name() + ":" + String.valueOf(value);
    }
}
