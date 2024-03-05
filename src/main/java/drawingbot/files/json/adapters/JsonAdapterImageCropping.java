package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.image.format.ImageCropping;
import drawingbot.utils.EnumRotation;

import java.lang.reflect.Type;

public class JsonAdapterImageCropping  implements JsonSerializer<ImageCropping>, JsonDeserializer<ImageCropping> {

    @Override
    public JsonElement serialize(ImageCropping src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("imageRotation", context.serialize(src.getImageRotation(), EnumRotation.class));
        jsonObject.addProperty("imageFlipHorizontal", src.shouldFlipHorizontal());
        jsonObject.addProperty("imageFlipVertical", src.shouldFlipVertical());
        jsonObject.addProperty("cropStartX", src.getCropStartX());
        jsonObject.addProperty("cropStartY", src.getCropStartY());
        jsonObject.addProperty("cropEndX", src.getCropWidth());
        jsonObject.addProperty("cropEndY", src.getCropHeight());
        return jsonObject;
    }

    @Override
    public ImageCropping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ImageCropping imageCropping = new ImageCropping();
        JsonObject jsonObject = json.getAsJsonObject();
        imageCropping.setImageRotation(context.deserialize(jsonObject.get("imageRotation"), EnumRotation.class));
        imageCropping.setFlipHorizontal(jsonObject.get("imageFlipHorizontal").getAsBoolean());
        imageCropping.setFlipVertical(jsonObject.get("imageFlipVertical").getAsBoolean());
        imageCropping.setCropStartX(jsonObject.get("cropStartX").getAsDouble());
        imageCropping.setCropStartY(jsonObject.get("cropStartY").getAsDouble());
        imageCropping.setCropWidth(jsonObject.get("cropEndX").getAsDouble());
        imageCropping.setCropHeight(jsonObject.get("cropEndY").getAsDouble());
        return imageCropping;
    }

}
