package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class DoubleSetting<C> extends AbstractNumberSetting<C, Double> {

    public static StringConverter<Double> stringConverter = new DoubleStringConverter();
    public int precision = 3;

    protected DoubleSetting(DoubleSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
        this.precision = toCopy.precision;
    }

    public DoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue) {
        super(clazz, Double.class, category, settingName, defaultValue);
    }

    public DoubleSetting(Class<C> pfmClass, String category, String settingName, double defaultValue, double minValue, double maxValue){
        super(pfmClass, Double.class, category, settingName, defaultValue, minValue, maxValue);
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
    protected Double defaultValidate(Double value) {
        return !isRanged ? value : Utils.clamp(value, minValue, maxValue);
    }

    @Override
    protected Double defaultRandomise(ThreadLocalRandom random) {
        return !isRanged ? random.nextDouble() : random.nextDouble(safeMinValue, safeMaxValue);
    }

    @Override
    protected StringConverter<Double> defaultStringConverter() {
        return stringConverter;
    }

    @Override
    public GenericSetting<C, Double> copy() {
        return new DoubleSetting<>(this);
    }
}