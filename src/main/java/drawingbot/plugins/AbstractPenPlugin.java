package drawingbot.plugins;

import drawingbot.api.IPlugin;
import drawingbot.drawing.DrawingPen;
import drawingbot.registry.MasterRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPenPlugin implements IPlugin {

    public final Map<String, DrawingPen> manufacturerCodes = new LinkedHashMap<>();

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

    @Override
    public void registerDrawingTools() {
        registerPens();
        registerPenSets();
    }

    public abstract void registerPens();

    public abstract void registerPenSets();

}
