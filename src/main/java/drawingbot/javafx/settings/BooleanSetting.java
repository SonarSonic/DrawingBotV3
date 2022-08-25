package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class BooleanSetting<C> extends GenericSetting<C, Boolean> {

    public static StringConverter<Boolean> stringConverter = new BooleanStringConverter();

    public BooleanSetting(BooleanSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public BooleanSetting(Class<C> pfmClass, String category, String settingName, Boolean defaultValue) {
        super(pfmClass, Boolean.class, category, settingName, defaultValue);
    }

    @Override
    protected Boolean defaultRandomise(ThreadLocalRandom random) {
        return random.nextBoolean();
    }

    @Override
    protected StringConverter<Boolean> defaultStringConverter() {
        return stringConverter;
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        //graphics
        CheckBox checkBox = new CheckBox();


        //bindings
        checkBox.selectedProperty().bindBidirectional(value);
        return checkBox;
    }

    @Override
    public GenericSetting<C, Boolean> copy() {
        return new BooleanSetting<>(this);
    }
}