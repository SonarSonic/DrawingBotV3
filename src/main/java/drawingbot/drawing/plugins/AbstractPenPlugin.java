package drawingbot.drawing.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.registry.MasterRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPenPlugin {

    public Map<String, DrawingPen> manufacturerCodes = new HashMap<>();

    public void registerPenWithCode(String code, DrawingPen drawingPen){
        manufacturerCodes.put(code, drawingPen);
        MasterRegistry.INSTANCE.registerDrawingPen(drawingPen);
    }

    public DrawingPen getPenFromCode(String code){
        return manufacturerCodes.get(code);
    }

    public List<DrawingPen> getDrawingPensFromCodes(Object ...codes){
        List<DrawingPen> drawingPenList = new ArrayList<>();
        for(Object code : codes){
            DrawingPen pen = getPenFromCode(code.toString());
            if(pen != null){
                drawingPenList.add(pen);
            }
        }
        return drawingPenList;
    }

    public abstract void registerPens();

    public abstract void registerPenSets();

}
