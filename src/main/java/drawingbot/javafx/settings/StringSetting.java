package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StringSetting<C> extends GenericSetting<C, String> {

    public StringSetting(Class<C> pfmClass, String settingName, String defaultValue, boolean shouldLock, BiConsumer<C, String> setter) {
        super(pfmClass, settingName, defaultValue, new DefaultStringConverter(), (random) -> defaultValue, shouldLock, value -> value, setter);
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(value);
        return textField;
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new StringSetting<>(clazz, settingName.getValue(), defaultValue, lock.get(), setter);
    }
}
