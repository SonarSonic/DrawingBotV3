package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.editors.IEditorFactory;
import javafx.util.StringConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ListSetting<C, O> extends GenericSetting<C, ArrayList<O>> {

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

    public final Class<O> objectType;
    public final Type listType;

    protected ListSetting(ListSetting<C, O> toCopy) {
        super(toCopy, toCopy.defaultValue);
        this.objectType = toCopy.objectType;
        this.listType = toCopy.listType;
    }

    public ListSetting(Class<C> clazz, Class<O> objectType, String category, String settingName, ArrayList<O> defaultValue) {
        super(clazz, (Class<ArrayList<O>>) defaultValue.getClass(), category, settingName, defaultValue);
        this.objectType = objectType;
        this.listType = TypeToken.getParameterized(ArrayList.class, objectType).getType();
    }

    @Override
    public IEditorFactory<ArrayList<O>> defaultEditorFactory() {
        return Editors::createGenericDummyEditor;
    }

    @Override
    protected StringConverter<ArrayList<O>> defaultStringConverter() {
        return (StringConverter<ArrayList<O>>) stringConverter;
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
    public GenericSetting<C, ArrayList<O>> copy() {
        return new ListSetting<>(this);
    }
}
