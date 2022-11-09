package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapSetting<C, K, V> extends GenericSetting<C, ObservableMap<K, V>> {

    public static StringConverter<?> stringConverter = new StringConverter<>() {

        @Override
        public String toString(Object object) {
            return object.toString();
        }

        @Override
        public Object fromString(String string) {
            throw new UnsupportedOperationException("Unidentified objects can't be deserialized");
        }
    };

    public final Class<K> keyType;
    public final Class<V> valueType;
    public final Type mapType;
    public final List<InvalidationListener> mapListeners;

    protected MapSetting(MapSetting<C, K, V> toCopy) {
        super(toCopy, toCopy.defaultValue);
        this.keyType = toCopy.keyType;
        this.valueType = toCopy.valueType;
        this.mapType = toCopy.mapType;
        this.mapListeners = new ArrayList<>();
    }

    public MapSetting(Class<C> clazz, Class<K> keyType, Class<V> valueType, String category, String settingName, ObservableMap<K, V> defaultValue) {
        super(clazz, (Class<ObservableMap<K, V>>) defaultValue.getClass(), category, settingName, defaultValue);
        this.keyType = keyType;
        this.valueType = valueType;
        this.mapType = TypeToken.getParameterized(HashMap.class, keyType, valueType).getType();
        this.mapListeners = new ArrayList<>();
        this.value.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                mapListeners.forEach(oldValue::removeListener);
            }
            if(newValue != null){
                mapListeners.forEach(newValue::addListener);
            }
        });
    }

    public MapSetting<C, K, V> addMapListener(InvalidationListener listener){
        mapListeners.add(listener);
        return this;
    }

    public MapSetting<C, K, V> removeMapListener(InvalidationListener listener){
        mapListeners.remove(listener);
        return this;
    }

    @Override
    protected StringConverter<ObservableMap<K, V>> defaultStringConverter() {
        return (StringConverter<ObservableMap<K, V>>) stringConverter;
    }

    @Override
    public JsonElement getValueAsJsonElement(Object value) {
        if(value instanceof ObservableMap){
            ObservableMap<K, V> map = (ObservableMap<K, V>) value;
            HashMap<K, V> hashMap = new HashMap<>(map);
            return JsonLoaderManager.createDefaultGson().toJsonTree(hashMap, mapType);
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public ObservableMap<K, V> getValueFromJsonElement(JsonElement element) {
        HashMap<K, V> map = JsonLoaderManager.createDefaultGson().fromJson(element, mapType);
        return FXCollections.observableMap(map);
    }

    @Override
    protected Node createJavaFXNode(boolean label) {
        return new Label("MAP");
    }

    @Override
    public GenericSetting<C, ObservableMap<K, V>> copy() {
        return new MapSetting<>(this);
    }
}
