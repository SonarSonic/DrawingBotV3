package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.awt.geom.AffineTransform;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public class JsonAdapterAffineTransform implements JsonSerializer<AffineTransform>, JsonDeserializer<AffineTransform> {

    public Type type = TypeToken.getArray(Double.class).getType();

    @Override
    public AffineTransform deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Double[] flatMatrixBoxed = context.deserialize(json, type);
        double[] flatMatrix = Stream.of(flatMatrixBoxed).mapToDouble(Double::doubleValue).toArray();
        return new AffineTransform(flatMatrix);
    }

    @Override
    public JsonElement serialize(AffineTransform src, Type typeOfSrc, JsonSerializationContext context) {
        double[] flatMatrix = new double[6];
        src.getMatrix(flatMatrix);
        return context.serialize(flatMatrix, type);
    }
}
