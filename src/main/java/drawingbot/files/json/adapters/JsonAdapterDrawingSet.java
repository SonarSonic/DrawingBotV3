package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.ColorSeparationSettings;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapterDrawingSet implements JsonSerializer<DrawingSet>, JsonDeserializer<DrawingSet> {

    public static final Type penListType = TypeToken.getParameterized(ArrayList.class, DrawingPen.class).getType();

    @Override
    public JsonElement serialize(DrawingSet src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.type);
        jsonObject.addProperty("name", src.name);
        jsonObject.add("pens", context.serialize(src.pens, penListType));

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
    public DrawingSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        String name = jsonObject.get("name").getAsString();
        List<DrawingPen> pens = context.deserialize(jsonObject.get("pens"), penListType);
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
