package drawingbot.javafx.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.jetbrains.annotations.Nullable;

public class PropertyAccessor<TARGET, DATA> extends PropertyAccessorAbstract {

    public Class<TARGET> clazz;
    public Class<DATA> type;

    public PropertyAccessor(Class<TARGET> clazz, Class<DATA> type, String key) {
        super(key);
        this.clazz = clazz;
        this.type = type;
    }

    public boolean canAccess(Object obj) {
        return clazz.isInstance(obj);
    }

    @Override
    public JsonElement readProperty(Object obj, Gson gson) {
        DATA data = getData(clazz.cast(obj));
        if(data == null){
            return JsonNull.INSTANCE;
        }
        return gson.toJsonTree(data, type);
    }

    @Override
    public void writeProperty(Object obj, JsonElement value, Gson gson) {
        DATA data = gson.fromJson(value, type);
        setData(clazz.cast(obj), data);
    }

    @Nullable
    public DATA getData(TARGET target){
        return null;
    }

    public void setData(TARGET target, @Nullable DATA data){

    }

    public String getRegistryKey() {
        return clazz.getSimpleName() + ":" + key;
    }
}
