package drawingbot.javafx.settings;

import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.concurrent.ThreadLocalRandom;

public class ColourSetting<C> extends GenericSetting<C, Color> {

    public static StringConverter<Color> stringConverter = new StringConverter<>() {
        @Override
        public String toString(Color object) {
            return String.valueOf(ImageTools.getARGBFromColor(object));
        }

        @Override
        public Color fromString(String string) {
            return ImageTools.getColorFromARGB(Integer.parseInt(string));
        }
    };

    protected ColourSetting(ColourSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public ColourSetting(Class<C> clazz, String category, String settingName, Color defaultValue) {
        super(clazz, Color.class, category, settingName, defaultValue);
    }

    @Override
    protected Color defaultRandomise(ThreadLocalRandom random) {
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 255);
    }

    @Override
    protected StringConverter<Color> defaultStringConverter() {
        return stringConverter;
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        //graphics
        ColorPicker colorPicker = new ColorPicker();

        //bindings
        colorPicker.valueProperty().bindBidirectional(value);
        colorPicker.setOnAction(e -> sendUserEditedEvent());
        return colorPicker;
    }

    @Override
    public GenericSetting<C, Color> copy() {
        return new ColourSetting<>(this);
    }
}