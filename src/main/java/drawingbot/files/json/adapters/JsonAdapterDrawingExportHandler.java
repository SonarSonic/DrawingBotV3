package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.files.DrawingExportHandler;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.lang.reflect.Type;

public class JsonAdapterDrawingExportHandler implements JsonSerializer<DrawingExportHandler>, JsonDeserializer<DrawingExportHandler> {

    @Override
    public DrawingExportHandler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        DrawingExportHandler handler = MasterRegistry.INSTANCE.drawingExportHandlers.get(json.getAsString());
        if(handler == null){
            return Register.EXPORT_SVG;
        }
        return handler;
    }

    @Override
    public JsonElement serialize(DrawingExportHandler src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getRegistryName());
    }

}