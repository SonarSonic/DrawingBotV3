package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.converter.DefaultStringConverter;

import java.util.function.BiConsumer;

public class StringSetting<C> extends GenericSetting<C, String> {

    public StringSetting(Class<C> pfmClass, String category, String settingName, String defaultValue, boolean shouldLock, BiConsumer<C, String> setter) {
        super(pfmClass, category, settingName, defaultValue, new DefaultStringConverter(), (random) -> defaultValue, shouldLock, value -> value, setter);
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(value);
        return textField;
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new StringSetting<>(clazz, category, settingName.getValue(), defaultValue, lock.get(), setter);
    }
}
