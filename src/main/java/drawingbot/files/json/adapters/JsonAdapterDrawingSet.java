package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapterDrawingSet implements JsonSerializer<IDrawingSet>, JsonDeserializer<IDrawingSet> {

    public static final Type penListType = TypeToken.getParameterized(ArrayList.class, IDrawingPen.class).getType();

    @Override
    public JsonElement serialize(IDrawingSet src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getType());
        jsonObject.addProperty("name", src.getName());
        jsonObject.add("pens", context.serialize(src.getPens(), penListType));

        if(src instanceof IColorManagedDrawingSet srcSet){
            ColorSeparationHandler handler = srcSet.getColorSeparationHandler();
            ColorSeparationSettings settings = srcSet.getColorSeparationSettings();
            if(handler != null){
                jsonObject.add("colorHandler", context.serialize(handler, ColorSeparationHandler.class));
                if(settings != null && handler.getSettingsClass() != null){
                    jsonObject.add("colorSettings", context.serialize(settings, handler.getSettingsClass()));
                }
            }
        }
        return jsonObject;
    }

    @Override
    public IDrawingSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        String name = jsonObject.get("name").getAsString();
        List<IDrawingPen> pens = context.deserialize(jsonObject.get("pens"), penListType);
        DrawingSet drawingSet = new DrawingSet(type, name, pens);

        if(jsonObject.has("colorHandler")){
            drawingSet.colorHandler = context.deserialize(jsonObject.get("colorHandler"), ColorSeparationHandler.class);
            if(jsonObject.has("colorSettings") && drawingSet.getColorSeparationHandler().getSettingsClass() != null){
                drawingSet.colorSettings = context.deserialize(jsonObject.get("colorSettings"), drawingSet.getColorSeparationHandler().getSettingsClass());
            }
        }
        return drawingSet;
    }

}
