package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.FloatStringConverter;

import java.util.function.BiConsumer;

public class FloatSetting<C> extends RangedNumberSetting<C, Float> {

    public FloatSetting(Class<C> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, boolean shouldLock, BiConsumer<C, Float> setter){
        super(pfmClass, settingName, defaultValue, minValue, maxValue, new FloatStringConverter(), rand -> (float)rand.nextDouble(minValue, maxValue), shouldLock, value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    @Override
    public Float fromNumber(Number number) {
        return (float)Utils.roundTo(number.doubleValue(), 0.001D);
    }

    @Override
    public GenericSetting<C, Float> copy() {
        return new FloatSetting<>(clazz, settingName.getValue(), defaultValue, minValue, maxValue, lock.get(), setter);
    }
}