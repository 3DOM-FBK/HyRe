package eu.fbk.threedom.structs;

public class LlNode extends Node {
    private LlNode next;

    public LlNode(Object data){ super(data); }

    public void setNext(LlNode next){ this.next = next; }
    public boolean hasNext(){return (next == null) ? false : true;}
    public LlNode next(){ return next; }

    public String toString(){
        return String.valueOf(this.value());
    }
}