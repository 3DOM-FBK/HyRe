package eu.fbk.threedom.pcFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreasholdCollection {

    private HashMap<FileType, ArrayList<Threashold>> collection;

    public ThreasholdCollection(){
        collection = new HashMap<>();
    }

    public void add(FileType fileType, PointClassification classType, float value){
        ArrayList list;

        if(!collection.containsKey(fileType))
            list = new ArrayList();
        else
            list = collection.get(fileType);

        list.add(new Threashold(fileType, classType, value));
        collection.put(fileType, list);
    }

    public void add(Threashold t){
        ArrayList list;

        if(!collection.containsKey(t.getFiletype()))
            list = new ArrayList();
        else
            list = collection.get(t.getFiletype());

        list.add(new Threashold(t.getFiletype(), t.getClassType(), t.getValue()));
        collection.put(t.getFiletype(), list);
    }

    public Threashold get(FileType fileType, PointClassification classType){
        ArrayList<Threashold> list = collection.get(fileType);

        if(list != null)
            for(Threashold t : list)
                if(t.getClassType() == classType)
                    return t;

        return new Threashold(fileType, classType, Float.MAX_VALUE);
    }

    public static void main(String[] args){

        Threashold t00 = new Threashold(FileType.PHOTOGRAMMETRIC, PointClassification.ROOF, 0.5f);
        Threashold t01 = new Threashold(FileType.PHOTOGRAMMETRIC, PointClassification.FACADE, 0.5f);
        Threashold t02 = new Threashold(FileType.PHOTOGRAMMETRIC, PointClassification.STREET, 0.5f);

        ThreasholdCollection tc = new ThreasholdCollection();
        tc.add(t00);
        tc.add(t01);
        tc.add(t02);

        System.out.println(tc.get(FileType.PHOTOGRAMMETRIC, PointClassification.STREET).getValue());
    }
}
