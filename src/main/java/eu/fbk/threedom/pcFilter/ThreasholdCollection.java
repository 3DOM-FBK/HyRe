package eu.fbk.threedom.pcFilter;

import eu.fbk.threedom.pc.FileType;
import eu.fbk.threedom.pc.PointClassification;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

public class ThreasholdCollection {

    @Setter @Getter
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

//    public List<Threashold> get(FileType fileType){
//        ArrayList<Threashold> list = collection.get(fileType);
//
//        if(list != null)
//            return list;
//
//        return new Threashold(fileType, classType, Float.MAX_VALUE);
//    }

//    public Threashold[] get(FileType fileType){
//        ArrayList<Threashold> list = collection.get(fileType);
//
//        return (Threashold[])list.toArray();
//    }

    public static void main(String[] args){

        Threashold t00 = new Threashold(FileType.PHOTOGRAMMETRIC, PointClassification.C0, 0.5f);
        Threashold t01 = new Threashold(FileType.PHOTOGRAMMETRIC, PointClassification.C1, 0.5f);
        Threashold t02 = new Threashold(FileType.PHOTOGRAMMETRIC, PointClassification.C2, 0.5f);

        ThreasholdCollection tc = new ThreasholdCollection();
        tc.add(t00);
        tc.add(t01);
        tc.add(t02);

        System.out.println(tc.get(FileType.PHOTOGRAMMETRIC, PointClassification.C2).getValue());
    }
}
