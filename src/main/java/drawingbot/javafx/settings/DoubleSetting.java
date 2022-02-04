package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.DoubleStringConverter;

import java.util.function.BiConsumer;

public class DoubleSetting<C> extends RangedNumberSetting<C, Double> {

    public int precision = 3;

    public DoubleSetting(Class<C> pfmClass, String category, String settingName, double defaultValue, double minValue, double maxValue, boolean shouldLock, BiConsumer<C, Double> setter){
        super(pfmClass, category, settingName, defaultValue, minValue, maxValue, new DoubleStringConverter(), rand -> rand.nextDouble(minValue, maxValue), shouldLock, value -> Utils.clamp(value, minValue, maxValue), setter);
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
        return new DoubleSetting<>(clazz, category, settingName.getValue(), defaultValue, minValue, maxValue, lock.get(), setter).setPrecision(precision).setMajorTick(majorTick).setSnapToTicks(snapToTicks).setSafeRange(safeMinValue, safeMaxValue).setRandomiser(randomiser);
    }
}