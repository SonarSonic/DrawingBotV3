package drawingbot.javafx.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public abstract class PropertyAccessorAbstract {

    public String key;

    public PropertyAccessorAbstract(String key) {
        super();
        this.key = key;
    }

    public abstract boolean canAccess(Object obj);

    public abstract JsonElement readProperty(Object obj, Gson gson);

    public abstract void writeProperty(Object obj, JsonElement value, Gson gson);

    public abstract String getRegistryKey();

    public String getKey() {
        return key;
    }

}
