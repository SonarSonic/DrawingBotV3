package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.CustomPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.GsonHelper;
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
        drawingPen.argb = jsonObject.get("argb").getAsInt();
        drawingPen.distributionWeight = jsonObject.has("distributionWeight") ? jsonObject.get("distributionWeight").getAsInt() : 100;
        drawingPen.strokeSize = jsonObject.has("strokeSize") ? jsonObject.get("strokeSize").getAsFloat() : 1F;
        drawingPen.isEnabled = !jsonObject.has("isEnabled") || jsonObject.get("isEnabled").getAsBoolean();

        if(jsonObject.has("colorSplitMultiplier")){
            drawingPen.hasColourSplitterData = true;
            drawingPen.colorSplitMultiplier = jsonObject.get("colorSplitMultiplier").getAsFloat();
            drawingPen.colorSplitOpacity = jsonObject.get("colorSplitOpacity").getAsFloat();
            drawingPen.colorSplitOffsetX = jsonObject.get("colorSplitOffsetX").getAsFloat();
            drawingPen.colorSplitOffsetY = jsonObject.get("colorSplitOffsetY").getAsFloat();
        }

        DrawingPen actualPen = MasterRegistry.INSTANCE.getDrawingPenFromRegistryName(drawingPen.getCodeName());
        if(actualPen instanceof CustomPen){
            return actualPen;
        }
        if(drawingPen.equals(actualPen)){
            return actualPen;
        }
        return drawingPen;
    }

}
