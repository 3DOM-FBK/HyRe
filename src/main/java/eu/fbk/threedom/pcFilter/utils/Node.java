package eu.fbk.threedom.pcFilter.utils;

public abstract class Node implements NodeInterface {
    private Object data;

    public Node(Object data){ this.data = data; }

    @Override
    public Object value() { return data; }
}