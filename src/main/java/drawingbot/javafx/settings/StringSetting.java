package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.converter.DefaultStringConverter;

import java.util.function.BiConsumer;

public class StringSetting<C> extends GenericSetting<C, String> {

    protected StringSetting(GenericSetting<C, String> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public StringSetting(Class<C> pfmClass, String category, String settingName, String defaultValue, BiConsumer<C, String> setter) {
        super(pfmClass, String.class, category, settingName, defaultValue, new DefaultStringConverter(), value -> value, setter);
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(value);
        return textField;
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new StringSetting<>(this);
    }
}
