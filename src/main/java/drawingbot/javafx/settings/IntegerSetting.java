package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.BiConsumer;

public class IntegerSetting<C> extends AbstractNumberSetting<C, Integer> {

    protected IntegerSetting(IntegerSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public IntegerSetting(Class<C> clazz, String category, String settingName, Integer defaultValue, BiConsumer<C, Integer> setter) {
        super(clazz, category, settingName, defaultValue, new IntegerStringConverter(), setter);
    }

    public IntegerSetting(Class<C> pfmClass, String category, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        super(pfmClass, category, settingName, defaultValue, minValue, maxValue, new IntegerStringConverter(), rand -> rand.nextInt(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
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
        return new IntegerSetting<>(this);
    }

}