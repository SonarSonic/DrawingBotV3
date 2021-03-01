package drawingbot.javafx.settings;

import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class ColourSetting<C> extends GenericSetting<C, Color> {

    public ColourSetting(Class<C> pfmClass, String settingName, Color defaultValue, boolean shouldLock, BiConsumer<C, Color> setter) {
        super(pfmClass, settingName, defaultValue, new StringConverter<>() {
            @Override
            public String toString(Color object) {
                return String.valueOf(ImageTools.getARGBFromColor(object));
            }

            @Override
            public Color fromString(String string) {
                return ImageTools.getColorFromARGB(Integer.parseInt(string));
            }
        }, random -> new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 255), shouldLock, value -> value, setter);
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        //graphics
        ColorPicker colorPicker = new ColorPicker();

        //bindings
        colorPicker.valueProperty().bindBidirectional(value);
        return colorPicker;
    }

    @Override
    public GenericSetting<C, Color> copy() {
        return new ColourSetting<>(clazz, settingName.getValue(), defaultValue, lock.get(), setter);
    }
}