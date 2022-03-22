package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ListSetting<C, O> extends GenericSetting<C, ArrayList<O>> {

    public final Class<O> objectType;
    public final Type listType;

    protected ListSetting(ListSetting<C, O> toCopy) {
        super(toCopy, toCopy.defaultValue);
        this.objectType = toCopy.objectType;
        this.listType = toCopy.listType;
    }

    public ListSetting(Class<C> clazz, Class<O> objectType, String category, String settingName, ArrayList<O> defaultValue, BiConsumer<C, ArrayList<O>> setter) {
        super(clazz, null, category, settingName, defaultValue, new StringConverter<>() {
            @Override
            public String toString(ArrayList<O> object) {
                return object.toString();
            }

            @Override
            public ArrayList<O> fromString(String string) {
                throw new UnsupportedOperationException("Unidentified objects can't be deserialized");
            }
        }, value -> objectType.isInstance(value) ? value : defaultValue, setter);
        this.objectType = objectType;
        this.listType = TypeToken.getParameterized(ArrayList.class, objectType).getType();
    }

    @Override
    public JsonElement getValueAsJsonElement(Object value) {
        return JsonLoaderManager.createDefaultGson().toJsonTree(value, listType);
    }

    @Override
    public ArrayList<O> getValueFromJsonElement(JsonElement element) {
        return JsonLoaderManager.createDefaultGson().fromJson(element, listType);
    }

    @Override
    protected Node createJavaFXNode(boolean label) {
        return new Label("Object");
    }

    @Override
    public GenericSetting<C, ArrayList<O>> copy() {
        return new ListSetting<>(this);
    }
}
