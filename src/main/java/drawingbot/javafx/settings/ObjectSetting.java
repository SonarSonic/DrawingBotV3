package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.editors.IEditorFactory;
import javafx.util.StringConverter;

public class ObjectSetting<C, O> extends GenericSetting<C, O> {

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

    protected ObjectSetting(ObjectSetting<C, O> toCopy) {
        super(toCopy, toCopy.defaultValue);
    }

    public ObjectSetting(Class<C> clazz, Class<O> objectType, String category, String settingName, O defaultValue) {
        super(clazz, objectType, category, settingName, defaultValue);
    }

    @Override
    public IEditorFactory<O> defaultEditorFactory() {
        return Editors::createGenericDummyEditor;
    }

    @Override
    protected O defaultValidate(O value) {
        return type.isInstance(value) ? value : defaultValue;
    }

    @Override
    protected StringConverter<O> defaultStringConverter() {
        return (StringConverter<O>) stringConverter;
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
    public GenericSetting<C, O> copy() {
        return new ObjectSetting<>(this);
    }
}
