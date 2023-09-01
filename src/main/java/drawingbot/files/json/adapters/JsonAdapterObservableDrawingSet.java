package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.ColorSeparationSettings;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.collections.FXCollections;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapterObservableDrawingSet extends JsonAdapterAbstract<ObservableDrawingSet>{

    public static List<GenericSetting<?, ?>> settings;

    static{
        settings = new ArrayList<>();
        settings.add(GenericSetting.createStringSetting(ObservableDrawingSet.class, "type", "", i -> i.type));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingSet.class, "name", "", i -> i.name));
        settings.add(GenericSetting.createListSetting(ObservableDrawingSet.class, ObservableDrawingPen.class,"pens", new ArrayList<>(), i -> i.pens));
        settings.add(GenericSetting.createOptionSetting(ObservableDrawingSet.class, EnumDistributionOrder.class, "distributionOrder", FXCollections.observableArrayList(EnumDistributionOrder.values()), EnumDistributionOrder.DARKEST_FIRST, i -> i.distributionOrder));
        settings.add(GenericSetting.createOptionSetting(ObservableDrawingSet.class, EnumDistributionType.class, "distributionType", FXCollections.observableArrayList(EnumDistributionType.values()), EnumDistributionType.EVEN_WEIGHTED, i -> i.distributionType));
    }

    @Override
    public JsonElement serialize(ObservableDrawingSet src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = (JsonObject) super.serialize(src, typeOfSrc, context);
        ColorSeparationHandler handler = src.getColorSeparationHandler();
        ColorSeparationSettings settings = src.getColorSeparationSettings();
        if(handler != null){
            jsonObject.add("colorHandler", context.serialize(handler, ColorSeparationHandler.class));
            if(settings != null && handler.getSettingsClass() != null){
                jsonObject.add("colorSettings", context.serialize(settings, handler.getSettingsClass()));
            }
        }
        return jsonObject;
    }

    @Override
    public ObservableDrawingSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ObservableDrawingSet drawingSet = super.deserialize(json, typeOfT, context);

        if(jsonObject.has("colorHandler")){
            ColorSeparationHandler handler = context.deserialize(jsonObject.get("colorHandler"), ColorSeparationHandler.class);
            ColorSeparationSettings settings = null;
            if(jsonObject.has("colorSettings") && drawingSet.getColorSeparationHandler().getSettingsClass() != null){
                settings = context.deserialize(jsonObject.get("colorSettings"), drawingSet.getColorSeparationHandler().getSettingsClass());
            }
            drawingSet.setColorSeparation(handler, settings);
        }

        //// LEGACY PROJECT SUPPORT - START \\\\
        if(jsonObject.has("colourSeperator")){
            ColorSeparationHandler handler = context.deserialize(jsonObject.get("colourSeperator"), ColorSeparationHandler.class);
            ColorSeparationSettings settings = handler != null ? handler.getDefaultSettings() : null;
            drawingSet.setColorSeparation(handler, settings);
        }
        //// LEGACY PROJECT SUPPORT - END \\\\

        return drawingSet;
    }

    @Override
    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    @Override
    public ObservableDrawingSet getInstance() {
        return new ObservableDrawingSet();
    }
}
