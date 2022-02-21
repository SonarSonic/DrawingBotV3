package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.StringConverter;
import javafx.util.converter.LongStringConverter;

import java.util.function.BiConsumer;

public class LongSetting<C> extends AbstractNumberSetting<C, Long> {

    protected LongSetting(LongSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public LongSetting(Class<C> clazz, String category, String settingName, long defaultValue, BiConsumer<C, Long> setter) {
        super(clazz, category, settingName, defaultValue, new LongStringConverter(), setter);
    }

    public LongSetting(Class<C> pfmClass, String category, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        super(pfmClass, category, settingName, defaultValue, minValue, maxValue, new LongStringConverter(), rand -> rand.nextLong(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
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
        return new LongSetting<>(this);
    }

}