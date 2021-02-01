package drawingbot.drawing;

import java.util.*;

public class DrawingRegistry {

    public static DrawingRegistry INSTANCE = new DrawingRegistry();

    public LinkedHashMap<String, DrawingPen> registeredPens; //key = manufacturers code, value = the pen
    public LinkedHashMap<String, DrawingSet> registeredSets; //key = unique set name, value = the pen

    public DrawingRegistry(){
        registeredPens = new LinkedHashMap<>();
        registeredSets = new LinkedHashMap<>();
        CopicPenPlugin.registerPens(this);
        CopicPenPlugin.registerPenSets(this);
    }

    public DrawingPen getDefaultPen(){
        return getDrawingPenFromName("Copic Original 100 Black");
    }

    public DrawingSet getDefaultSet(){
        return getDrawingSetFromName("Copic Dark Greys");
    }

    public void registerDrawingPen(DrawingPen pen){
        if(registeredPens.get(pen.getName()) != null){
            System.out.println("DUPLICATE PEN UNIQUE ID: " + pen.getName());
            return;
        }
        registeredPens.put(pen.getName(), pen);
    }

    public void registerDrawingSet(DrawingSet penSet){
        if(registeredSets.get(penSet.getName()) != null){
            System.out.println("DUPLICATE DRAWING SET NAME: " + penSet.getName());
            return;
        }
        registeredSets.put(penSet.getName(), penSet);
    }

    public DrawingSet getDrawingSetFromName(String name){
        return registeredSets.get(name);
    }

    public DrawingPen getDrawingPenFromName(String name){
        return registeredPens.get(name);
    }

    public List<IDrawingPen> getDrawingPensFromCodes(String[] codes){
        List<IDrawingPen> pens = new ArrayList<>();
        for(String code : codes){
            Optional<DrawingPen> pen =  registeredPens.values().stream().filter(p -> p.getName().contains(code)).findFirst();
            if(pen.isPresent()){
                pens.add(pen.get());
            }else{
                System.out.println("Couldn't find a pen with the code: " + code);
            }

        }
        return pens;
    }

}