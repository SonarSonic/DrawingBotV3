package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.GsonHelper;
import drawingbot.image.ImageTools;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;

import java.lang.reflect.Type;

public class JsonAdapterDrawingPen implements JsonSerializer<IDrawingPen>, JsonDeserializer<IDrawingPen> {

    @Override
    public JsonElement serialize(IDrawingPen src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getType());
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("argb", src.getARGB());
        jsonObject.addProperty("distributionWeight", src.getDistributionWeight());
        jsonObject.addProperty("strokeSize", src.getStrokeSize());
        jsonObject.addProperty("isEnabled", src.isEnabled());

        if(src.hasColorSplitterData()){
            jsonObject.addProperty("colorSplitMultiplier", src.getColorSplitMultiplier());
            jsonObject.addProperty("colorSplitOpacity", src.getColorSplitOpacity());
            jsonObject.addProperty("colorSplitOffsetX", src.getColorSplitOffsetX());
            jsonObject.addProperty("colorSplitOffsetY", src.getColorSplitOffsetY());
        }
        return jsonObject;
    }

    @Override
    public IDrawingPen deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        DrawingPen drawingPen = new DrawingPen();
        drawingPen.type = GsonHelper.getStringOrDefault(jsonObject, "type", DBConstants.PRESET_MISSING_NAME);
        drawingPen.name = GsonHelper.getStringOrDefault(jsonObject, "name", DBConstants.PRESET_MISSING_NAME);
        drawingPen.argb = GsonHelper.getIntOrDefault(jsonObject, "argb", ImageTools.getARGB(255, 0, 0, 0));
        drawingPen.distributionWeight = GsonHelper.getIntOrDefault(jsonObject, "distributionWeight", 100);
        drawingPen.strokeSize = GsonHelper.getFloatOrDefault(jsonObject,"strokeSize", 1F);
        drawingPen.isEnabled = GsonHelper.getBooleanOrDefault(jsonObject, "isEnabled", true);

        if(jsonObject.has("colorSplitMultiplier")){
            drawingPen.hasColourSplitterData = true;
            drawingPen.colorSplitMultiplier = GsonHelper.getFloatOrDefault(jsonObject,"colorSplitMultiplier", DBPreferences.INSTANCE.defaultColorSplitterPenMultiplier.get());
            drawingPen.colorSplitOpacity = GsonHelper.getFloatOrDefault(jsonObject,"colorSplitOpacity", DBPreferences.INSTANCE.defaultColorSplitterPenOpacity.get());
            drawingPen.colorSplitOffsetX = GsonHelper.getFloatOrDefault(jsonObject,"colorSplitOffsetX", 0F);
            drawingPen.colorSplitOffsetY = GsonHelper.getFloatOrDefault(jsonObject,"colorSplitOffsetY", 0F);
        }

        IDrawingPen actualPen = MasterRegistry.INSTANCE.getDrawingPenFromRegistryName(drawingPen.getCodeName());

        if(drawingPen.equals(actualPen)){
            return actualPen;
        }

        if(actualPen != null && actualPen.getSpecialColorHandler() != null){
            drawingPen.specialColorHandler = actualPen.getSpecialColorHandler();
        }

        return drawingPen;
    }

}
