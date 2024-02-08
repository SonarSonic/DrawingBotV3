package drawingbot.plugins;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IPlugin;
import drawingbot.drawing.DrawingPen;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPenPlugin implements IPlugin {

    public final Map<String, IDrawingPen> manufacturerCodes = new LinkedHashMap<>();
    public final BooleanProperty enabled = new SimpleBooleanProperty(true);

    public abstract String getPenManufacturer();

    @Override
    public String getRegistryName() {
        return Utils.getSafeRegistryName(getPenManufacturer());
    }

    @Override
    public String getDisplayName() {
        return "%s Pen Plugin".formatted(getPenManufacturer());
    }

    @Override
    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void registerPenWithCode(String code, IDrawingPen drawingPen){
        manufacturerCodes.put(code, drawingPen);
        MasterRegistry.INSTANCE.registerDrawingPen(drawingPen);
    }

    public IDrawingPen getPenFromCode(String code){
        return manufacturerCodes.get(code);
    }

    public List<IDrawingPen> getDrawingPensFromCodes(Object ...codes){
        List<IDrawingPen> drawingPenList = new ArrayList<>();
        for(Object code : codes){
            IDrawingPen pen = getPenFromCode(code.toString());
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
