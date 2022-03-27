package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.util.function.BiConsumer;

public class ObjectSetting<C, O> extends GenericSetting<C, O> {

    protected ObjectSetting(ObjectSetting<C, O> toCopy) {
        super(toCopy, toCopy.defaultValue);
    }

    public ObjectSetting(Class<C> clazz, Class<O> objectType, String category, String settingName, O defaultValue, BiConsumer<C, O> setter) {
        super(clazz, objectType, category, settingName, defaultValue, new StringConverter<>() {
            @Override
            public String toString(O object) {
                return object.toString();
            }

            @Override
            public O fromString(String string) {
                throw new UnsupportedOperationException("Unidentified objects can't be deserialized");
            }
        }, value -> objectType.isInstance(value) ? value : defaultValue, setter);
    }

    @Override
    public JsonElement getValueAsJsonElement(Object value) {
        return JsonLoaderManager.createDefaultGson().toJsonTree(value);
    }

    @Override
    public O getValueFromJsonElement(JsonElement element) {
        return JsonLoaderManager.createDefaultGson().fromJson(element, type);
    }

    @Override
    protected Node createJavaFXNode(boolean label) {
        return new Label("Object");
    }

    @Override
    public GenericSetting<C, O> copy() {
        return new ObjectSetting<>(this);
    }
}
