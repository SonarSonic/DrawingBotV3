package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.DoubleStringConverter;

import java.util.function.BiConsumer;

public class DoubleSetting<C> extends AbstractNumberSetting<C, Double> {

    public int precision = 3;

    protected DoubleSetting(DoubleSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
        this.precision = toCopy.precision;
    }

    public DoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, BiConsumer<C, Double> setter) {
        super(clazz, Double.class, category, settingName, defaultValue, new DoubleStringConverter(), setter);
    }

    public DoubleSetting(Class<C> pfmClass, String category, String settingName, double defaultValue, double minValue, double maxValue, BiConsumer<C, Double> setter){
        super(pfmClass, Double.class, category, settingName, defaultValue, minValue, maxValue, new DoubleStringConverter(), rand -> rand.nextDouble(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    @Override
    public void setRandomizerRange(Double safeMinValue, Double safeMaxValue) {
        this.randomiser = rand -> rand.nextDouble(safeMinValue, safeMaxValue);
    }

    public DoubleSetting<C> setPrecision(int precision){
        this.precision = precision;
        return this;
    }

    @Override
    public Double fromNumber(Number number) {
        return Utils.roundToPrecision(number.doubleValue(), precision);
    }

    @Override
    public GenericSetting<C, Double> copy() {
        return new DoubleSetting<>(this);
    }
}