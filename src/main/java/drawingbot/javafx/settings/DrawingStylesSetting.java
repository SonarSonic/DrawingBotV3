package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingStyleSet;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.util.StringConverter;

import java.util.function.BiConsumer;

public class DrawingStylesSetting<C> extends GenericSetting<C, DrawingStyleSet> {

    public DrawingStylesSetting(Class<C> pfmClass, String settingName, DrawingStyleSet defaultValue, boolean shouldLock, BiConsumer<C, DrawingStyleSet> setter) {
        super(pfmClass, settingName, defaultValue, new StringConverter<>() {
            @Override
            public String toString(DrawingStyleSet object) {
                return object.styles.toString();
            }

            @Override
            public DrawingStyleSet fromString(String string) {
                throw new UnsupportedOperationException("Drawing styles can not be set by the user");
            }
        }, null, shouldLock, value -> value, setter);
    }

    @Override
    public boolean hasEditableTextField(){
        return false;
    }

    @Override
    public JsonElement getValueAsJsonElement() {
        return JsonLoaderManager.createDefaultGson().toJsonTree(this.getValue());
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
        //bindings
        //checkBox.selectedProperty().bindBidirectional(value);
        return button;
    }

    @Override
    public GenericSetting<C, DrawingStyleSet> copy() {
        return new DrawingStylesSetting<>(clazz, settingName.getValue(), defaultValue, lock.get(), setter);
    }
}