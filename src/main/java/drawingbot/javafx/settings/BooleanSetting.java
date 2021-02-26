package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.util.converter.BooleanStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class BooleanSetting<C> extends GenericSetting<C, Boolean> {

    public BooleanSetting(Class<C> pfmClass, String settingName, Boolean defaultValue, boolean shouldLock, BiConsumer<C, Boolean> setter) {
        super(pfmClass, settingName, defaultValue, new BooleanStringConverter(), ThreadLocalRandom::nextBoolean, shouldLock, value -> value, setter);
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
        return new BooleanSetting<>(clazz, settingName.getValue(), defaultValue, lock.get(), setter);
    }
}