package eu.it.fbk.threedom.pcFilter.utils;

import eu.it.fbk.threedom.pcFilter.utils.LlNode;
import lombok.Getter;
import lombok.Setter;

public class Voxel {

    @Getter @Setter private LlNode head;
    @Getter @Setter private LlNode tail;
    @Getter @Setter private int id;

    public Voxel(int id){
        this.id = id;
        head = null;
        tail = null;
    }
}
