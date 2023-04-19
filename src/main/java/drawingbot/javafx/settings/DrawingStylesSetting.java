package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingStyleSet;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.util.StringConverter;

public class DrawingStylesSetting<C> extends GenericSetting<C, DrawingStyleSet> {

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

    protected DrawingStylesSetting(GenericSetting<C, DrawingStyleSet> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public DrawingStylesSetting(Class<C> clazz, String category, String settingName, DrawingStyleSet defaultValue) {
        super(clazz, DrawingStyleSet.class, category, settingName, defaultValue);
    }

    @Override
    protected StringConverter<DrawingStyleSet> defaultStringConverter() {
        return (StringConverter<DrawingStyleSet>) stringConverter;
    }

    @Override
    public boolean hasEditableTextField(){
        return false;
    }

    @Override
    public JsonElement getValueAsJsonElement(Object value) {
        return JsonLoaderManager.createDefaultGson().toJsonTree(value);
    }

    @Override
    public DrawingStyleSet getValueFromJsonElement(JsonElement element) {
        return JsonLoaderManager.createDefaultGson().fromJson(element, DrawingStyleSet.class);
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        //graphics
        Button button = new Button("Configure Styles");

        button.setOnAction((e) -> DrawingBotV3.INSTANCE.controller.mosaicController.openWidget(this, this.getValue()));
        return button;
    }

    @Override
    public GenericSetting<C, DrawingStyleSet> copy() {
        return new DrawingStylesSetting<>(this);
    }
}