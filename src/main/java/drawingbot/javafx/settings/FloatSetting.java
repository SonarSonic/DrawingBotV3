package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.FloatStringConverter;

import java.util.function.BiConsumer;

public class FloatSetting<C> extends RangedNumberSetting<C, Float> {

    public int precision = 3;

    public FloatSetting(Class<C> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, boolean shouldLock, BiConsumer<C, Float> setter){
        super(pfmClass, settingName, defaultValue, minValue, maxValue, new FloatStringConverter(), rand -> (float)rand.nextDouble(minValue, maxValue), shouldLock, value -> Utils.clamp(value, minValue, maxValue), setter);
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
        return new FloatSetting<>(clazz, settingName.getValue(), defaultValue, minValue, maxValue, lock.get(), setter).setPrecision(precision).setMajorTick(majorTick).setSnapToTicks(snapToTicks);
    }
}