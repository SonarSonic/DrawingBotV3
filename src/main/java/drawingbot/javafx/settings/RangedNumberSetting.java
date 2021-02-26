package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class RangedNumberSetting<C, V extends Number> extends GenericSetting<C, V> {

    public V defaultValue;
    public V minValue;
    public V maxValue;

    protected RangedNumberSetting(Class<C> clazz, String settingName, V defaultValue, V minValue, V maxValue, StringConverter<V> stringConverter, Function<ThreadLocalRandom, V> randomiser, boolean shouldLock, Function<V, V> validator, BiConsumer<C, V> setter) {
        super(clazz, settingName, defaultValue, stringConverter, randomiser, shouldLock, validator, setter);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        //graphics
        Slider slider = new Slider();
        slider.setMin(minValue.doubleValue());
        slider.setMax(maxValue.doubleValue());
        if(label){
            slider.setMajorTickUnit(Math.min(Integer.MAX_VALUE, Math.abs(maxValue.doubleValue()-minValue.doubleValue())));
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
        }
        slider.setValue(value.getValue().doubleValue());

        //bindings
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            setValue(fromNumber(newValue));
        });

        value.addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.doubleValue());
        });

        return slider;
    }

    public abstract V fromNumber(Number number);
}