package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class LongSetting<C> extends AbstractNumberSetting<C, Long> {

    public static StringConverter<Long> stringConverter = new LongStringConverter();

    protected LongSetting(LongSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public LongSetting(Class<C> clazz, String category, String settingName, long defaultValue) {
        super(clazz, Long.class, category, settingName, defaultValue);
    }

    public LongSetting(Class<C> pfmClass, String category, String settingName, long defaultValue, long minValue, long maxValue){
        super(pfmClass, Long.class, category, settingName, defaultValue, minValue, maxValue);
    }

    @Override
    public Long fromNumber(Number number) {
        return number.longValue();
    }

    @Override
    protected Long defaultValidate(Long value) {
        return !isRanged ? value : Utils.clamp(value, minValue, maxValue);
    }

    @Override
    protected Long defaultRandomise(ThreadLocalRandom random) {
        return !isRanged ? random.nextLong() : random.nextLong(safeMinValue, safeMaxValue);
    }

    @Override
    protected StringConverter<Long> defaultStringConverter() {
        return stringConverter;
    }

    @Override
    public GenericSetting<C, Long> copy() {
        return new LongSetting<>(this);
    }

}