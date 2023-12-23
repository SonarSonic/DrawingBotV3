package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.observables.ObservableDrawingSet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapterDrawingSets implements JsonSerializer<DrawingSets>, JsonDeserializer<DrawingSets> {

    public static final Type drawingSetsType = TypeToken.getParameterized(ArrayList.class, ObservableDrawingSet.class).getType();

    @Override
    public JsonElement serialize(DrawingSets src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("drawingSets", context.serialize(new ArrayList<>(src.getDrawingSetSlots()), drawingSetsType));
        jsonObject.addProperty("activeSet", src.getActiveSetSlot());
        return jsonObject;
    }

    @Override
    public DrawingSets deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        List<ObservableDrawingSet> drawingSetsList = context.deserialize(jsonObject.get("drawingSets"), drawingSetsType);
        int activeSlot = jsonObject.get("activeSet").getAsInt();

        DrawingSets drawingSets = new DrawingSets(drawingSetsList);
        drawingSets.setActiveDrawingSet(drawingSetsList.get(activeSlot));
        return drawingSets;
    }

}
