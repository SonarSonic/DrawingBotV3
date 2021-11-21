package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.BiConsumer;

public class IntegerSetting<C> extends RangedNumberSetting<C, Integer> {

    public IntegerSetting(Class<C> pfmClass, String settingName, int defaultValue, int minValue, int maxValue, boolean shouldLock, BiConsumer<C, Integer> setter){
        super(pfmClass, settingName, defaultValue, minValue, maxValue, new IntegerStringConverter(), rand -> rand.nextInt(minValue, maxValue), shouldLock, value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    @Override
    public void setRandomizerRange(Integer safeMinValue, Integer safeMaxValue) {
        this.randomiser = rand -> rand.nextInt(safeMinValue, safeMaxValue);
    }

    @Override
    public Integer fromNumber(Number number) {
        return number.intValue();
    }

    @Override
    public GenericSetting<C, Integer> copy() {
        return new IntegerSetting<>(clazz, settingName.getValue(), defaultValue, minValue, maxValue, lock.get(), setter).setMajorTick(majorTick).setSnapToTicks(snapToTicks).setSafeRange(safeMinValue, safeMaxValue).setRandomiser(randomiser);
    }

}