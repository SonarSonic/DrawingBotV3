package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.LongStringConverter;

import java.util.function.BiConsumer;

public class LongSetting<C> extends RangedNumberSetting<C, Long> {

    public LongSetting(Class<C> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, boolean shouldLock, BiConsumer<C, Long> setter){
        super(pfmClass, settingName, defaultValue, minValue, maxValue, new LongStringConverter(), rand -> rand.nextLong(minValue, maxValue), shouldLock, value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    @Override
    public void setRandomizerRange(Long safeMinValue, Long safeMaxValue) {
        this.randomiser = rand -> rand.nextLong(safeMinValue, safeMaxValue);
    }

    @Override
    public Long fromNumber(Number number) {
        return number.longValue();
    }

    @Override
    public GenericSetting<C, Long> copy() {
        return new LongSetting<>(clazz, settingName.getValue(), defaultValue, minValue, maxValue, lock.get(), setter).setMajorTick(majorTick).setSnapToTicks(snapToTicks).setSafeRange(safeMinValue, safeMaxValue).setRandomiser(randomiser);
    }

}