package drawingbot.files.json;

import com.google.gson.*;

import java.math.BigDecimal;
import java.math.BigInteger;

//TODO MOVE ME TO CORE LIBRARY
public class GsonHelper {

    private GsonBuilder builder;
    private Gson gson;

    public GsonHelper(){}

    public void setupGsonBuilder(GsonBuilder builder){}

    public Gson getDefaultGson(){
        if(gson == null){
            gson = getDefaultGsonBuilder().create();
        }
        return gson;
    }

    public GsonBuilder getDefaultGsonBuilder(){
        if(builder == null){
            builder = new GsonBuilder();
            setupGsonBuilder(builder);
        }
        return builder;
    }

    public static <O> O getObjectOrDefault(JsonObject jsonObject, JsonDeserializationContext context, String key, Class<O> clazz, O def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : context.deserialize(jsonElement, clazz);
    }

    public static boolean getBooleanOrDefault(JsonObject jsonObject, String key, boolean def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsBoolean();
    }

    public static Number getNumberOrDefault(JsonObject jsonObject, String key, Number def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsNumber();
    }

    public static String getStringOrDefault(JsonObject jsonObject, String key, String def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsString();
    }

    public static double getDoubleOrDefault(JsonObject jsonObject, String key, double def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsDouble();
    }

    public static float getFloatOrDefault(JsonObject jsonObject, String key, float def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsFloat();
    }

    public static int getIntOrDefault(JsonObject jsonObject, String key, int def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsInt();
    }

    public static byte getByteOrDefault(JsonObject jsonObject, String key, byte def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsByte();
    }

    public static BigDecimal getBigDecimalOrDefault(JsonObject jsonObject, String key, BigDecimal def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsBigDecimal();
    }

    public static BigInteger getBigIntegerOrDefault(JsonObject jsonObject, String key, BigInteger def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsBigInteger();
    }

    public static short getShortOrDefault(JsonObject jsonObject, String key, short def){
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? def : jsonElement.getAsShort();
    }

}
