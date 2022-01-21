package drawingbot.files.presets;

import com.google.gson.*;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.CustomPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterDrawingPen implements JsonSerializer<IDrawingPen>, JsonDeserializer<IDrawingPen> {

    @Override
    public IDrawingPen deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        DrawingPen drawingPen = new DrawingPen();
        drawingPen.type = jsonObject.get("type").getAsString();
        drawingPen.name = jsonObject.get("name").getAsString();
        drawingPen.argb = jsonObject.get("argb").getAsInt();
        drawingPen.distributionWeight = jsonObject.has("distributionWeight") ? jsonObject.get("distributionWeight").getAsInt() : 100;
        drawingPen.strokeSize = jsonObject.has("strokeSize") ? jsonObject.get("strokeSize").getAsFloat() : 1F;
        drawingPen.isEnabled = !jsonObject.has("isEnabled") || jsonObject.get("isEnabled").getAsBoolean();

        DrawingPen actualPen = MasterRegistry.INSTANCE.getDrawingPenFromRegistryName(drawingPen.getCodeName());
        if(actualPen instanceof CustomPen){
            return actualPen;
        }
        if(drawingPen.equals(actualPen)){
            return actualPen;
        }
        return drawingPen;
    }

    @Override
    public JsonElement serialize(IDrawingPen src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getType());
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("argb", src.getARGB());
        jsonObject.addProperty("distributionWeight", src.getDistributionWeight());
        jsonObject.addProperty("strokeSize", src.getStrokeSize());
        jsonObject.addProperty("isEnabled", src.isEnabled());
        return jsonObject;
    }

}
