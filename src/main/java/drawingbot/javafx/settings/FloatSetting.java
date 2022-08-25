package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.LongStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class FloatSetting<C> extends AbstractNumberSetting<C, Float> {

    public static StringConverter<Float> stringConverter = new FloatStringConverter();
    public int precision = 3;

    protected FloatSetting(FloatSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
        this.precision = toCopy.precision;
    }

    public FloatSetting(Class<C> clazz, String category, String settingName, Float defaultValue) {
        super(clazz, Float.class, category, settingName, defaultValue);
    }

    public FloatSetting(Class<C> pfmClass, String category, String settingName, float defaultValue, float minValue, float maxValue){
        super(pfmClass, Float.class, category, settingName, defaultValue, minValue, maxValue);
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
    protected Float defaultValidate(Float value) {
        return !isRanged ? value : Utils.clamp(value, minValue, maxValue);
    }

    @Override
    protected Float defaultRandomise(ThreadLocalRandom random) {
        return !isRanged ? random.nextFloat() : random.nextFloat(safeMinValue, safeMaxValue);
    }

    @Override
    protected StringConverter<Float> defaultStringConverter() {
        return stringConverter;
    }

    @Override
    public GenericSetting<C, Float> copy() {
        return new FloatSetting<>(this);
    }
}