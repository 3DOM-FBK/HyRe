package eu.fbk.threedom.pcFilter.utils;
import lombok.Getter;
import lombok.Setter;

public class Voxel {

    @Getter @Setter private LlNode head;
    @Getter @Setter private LlNode tail;
    @Getter @Setter private int id;

    // TODO: hashmap with sums: sum of all points property value ex. intensitySum

    public Voxel(int id){
        this.id = id;
        head = null;
        tail = null;
    }
}
