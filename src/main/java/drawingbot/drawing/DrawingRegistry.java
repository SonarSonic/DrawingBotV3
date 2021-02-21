package drawingbot.drawing;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.image.ImageTools;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.*;

public class DrawingRegistry {

    public static String specialType = "Special";
    public static String userType = "User";

    public static DrawingRegistry INSTANCE = new DrawingRegistry();

    public ObservableMap<String, ObservableList<IDrawingPen>> registeredPens;
    public ObservableMap<String, ObservableList<IDrawingSet<IDrawingPen>>> registeredSets;

    public DrawingRegistry(){
        registeredPens = FXCollections.observableMap(new LinkedHashMap<>());
        registeredSets = FXCollections.observableMap(new LinkedHashMap<>());

        CopicPenPlugin.registerPens(this);
        CopicPenPlugin.registerPenSets(this);

        ///add special sub menu types
        registeredSets.putIfAbsent(specialType, FXCollections.observableArrayList());
        registeredSets.putIfAbsent(userType, FXCollections.observableArrayList());
        registeredPens.putIfAbsent(specialType, FXCollections.observableArrayList());
        registeredPens.putIfAbsent(userType, FXCollections.observableArrayList());

        DrawingPen originalColourPen = new DrawingPen(specialType, "Original Colour", -1){
            @Override
            public int getCustomARGB(int pfmARGB) {
                return pfmARGB;
            }
        };
        DrawingPen originalGrayscalePen = new DrawingPen(specialType, "Original Grayscale", -1){
            @Override
            public int getCustomARGB(int pfmARGB) {
                return ImageTools.grayscaleFilter(pfmARGB);
            }
        };
        registerDrawingPen(originalColourPen);
        registerDrawingPen(originalGrayscalePen);

        registerDrawingSet(new DrawingSet(specialType,"Original Colour", List.of(originalColourPen)));
        registerDrawingSet(new DrawingSet(specialType,"Original Grayscale", List.of(originalGrayscalePen)));

    }

    public String getDefaultPenType(){
        return "Copic Original";
    }

    public IDrawingPen getDefaultPen(String type){
        return registeredPens.get(type).stream().findFirst().orElse(null);
    }

    public String getDefaultSetType(){
        return "Copic";
    }

    public IDrawingSet<IDrawingPen> getDefaultSet(String type){
        return registeredSets.get(type).stream().findFirst().orElse(null);
    }

    public void registerDrawingPen(DrawingPen pen){
        if(registeredPens.get(pen.getName()) != null){
            DrawingBotV3.logger.warning("DUPLICATE PEN UNIQUE ID: " + pen.getName());
            return;
        }
        registeredPens.putIfAbsent(pen.getType(), FXCollections.observableArrayList());
        registeredPens.get(pen.getType()).add(pen);
    }

    public void registerDrawingSet(IDrawingSet<IDrawingPen> penSet){
        if(registeredSets.get(penSet.getName()) != null){
            DrawingBotV3.logger.warning("DUPLICATE DRAWING SET NAME: " + penSet.getName());
            return;
        }
        registeredSets.putIfAbsent(penSet.getType(), FXCollections.observableArrayList());
        registeredSets.get(penSet.getType()).add(penSet);
    }

    public IDrawingSet<IDrawingPen> getDrawingSetFromCodeName(String codeName){
        String[] split = codeName.split(":");
        return registeredSets.get(split[0]).stream().filter(s -> s.getCodeName().equals(codeName)).findFirst().orElse(null);
    }

    public IDrawingPen getDrawingPenFromCodeName(String codeName){
        String[] split = codeName.split(":");
        return registeredPens.get(split[0]).stream().filter(p -> p.getCodeName().equals(codeName)).findFirst().orElse(null);
    }

    public List<IDrawingPen> getDrawingPensFromCodes(String[] codes){
        List<IDrawingPen> pens = new ArrayList<>();
        for(String code : codes){
            IDrawingPen pen = getDrawingPenFromCodeName(code);
            if(pen != null){
                pens.add(pen);
            }else{
                DrawingBotV3.logger.warning("Couldn't find a pen with the code: " + code);
            }
        }
        return pens;
    }
}