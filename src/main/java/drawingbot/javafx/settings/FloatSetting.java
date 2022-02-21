package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;

import java.util.function.BiConsumer;

public class FloatSetting<C> extends AbstractNumberSetting<C, Float> {

    public int precision = 3;

    protected FloatSetting(FloatSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
        this.precision = toCopy.precision;
    }

    public FloatSetting(Class<C> clazz, String category, String settingName, Float defaultValue, BiConsumer<C, Float> setter) {
        super(clazz, category, settingName, defaultValue, new FloatStringConverter(), setter);
    }

    public FloatSetting(Class<C> pfmClass, String category, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter){
        super(pfmClass, category, settingName, defaultValue, minValue, maxValue, new FloatStringConverter(), rand -> (float)rand.nextDouble(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    @Override
    public void setRandomizerRange(Float safeMinValue, Float safeMaxValue) {
        this.randomiser = rand -> (float)rand.nextDouble(safeMinValue, safeMaxValue);
    }

    public FloatSetting<C> setPrecision(int precision){
        this.precision = precision;
        return this;
    }

    @Override
    public Float fromNumber(Number number) {
        return (float)Utils.roundToPrecision(number.doubleValue(), precision);
    }

    @Override
    public GenericSetting<C, Float> copy() {
        return new FloatSetting<>(this);
    }
}