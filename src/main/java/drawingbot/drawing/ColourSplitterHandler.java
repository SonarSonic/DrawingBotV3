package drawingbot.drawing;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.presets.JsonAdapterColourSplitter;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@JsonAdapter(JsonAdapterColourSplitter.class)
public class ColourSplitterHandler {

    public final String name;
    public final Function<BufferedImage, List<BufferedImage>> splitFunction;
    public final Function<ColourSplitterHandler, DrawingSet> createDrawingSet;
    public final List<String> outputNames;
    public DrawingSet drawingSet;

    public ColourSplitterHandler(String name, Function<BufferedImage, List<BufferedImage>> splitFunction, Function<ColourSplitterHandler, DrawingSet> createDrawingSet, List<String> outputNames){
        this.name = name;
        this.splitFunction = splitFunction;
        this.createDrawingSet = createDrawingSet;
        this.outputNames = outputNames;
    }

    public boolean isDefault(){
        return name.equals("Default");
    }

    public DrawingSet getDrawingSet(){
        return drawingSet;
    }

    public int getSplitCount(){
        return outputNames.size();
    }

    public static DrawingSet createDefaultDrawingSet(ColourSplitterHandler splitter){
        if(!splitter.isDefault()){
            List<DrawingPen> pens = new ArrayList<>();
            for(int i = 0; i < splitter.outputNames.size(); i++){
                DrawingPen pen = MasterRegistry.INSTANCE.getDrawingPenFromRegistryName(DBConstants.DRAWING_TYPE_SPECIAL + ":" + splitter.outputNames.get(i));
                pens.add(pen);
            }
            return new ColourSplitterDrawingSet(splitter, DBConstants.DRAWING_TYPE_SPECIAL,splitter.name + " Seperation", pens);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public static class ColourSplitterDrawingSet extends DrawingSet{

        public final transient ColourSplitterHandler splitter;

        public ColourSplitterDrawingSet(ColourSplitterHandler splitter) {
            super();
            this.splitter = splitter;
        }

        public ColourSplitterDrawingSet(ColourSplitterHandler splitter, String type, String name, List<DrawingPen> pens) {
            super(type, name, pens);
            this.splitter = splitter;
        }
    }
}
