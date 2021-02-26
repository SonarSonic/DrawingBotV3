package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.LongExpression;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.LongStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LongSetting<C> extends RangedNumberSetting<C, Long> {

    public LongSetting(Class<C> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, boolean shouldLock, BiConsumer<C, Long> setter){
        super(pfmClass, settingName, defaultValue, minValue, maxValue, new LongStringConverter(), rand -> rand.nextLong(minValue, maxValue), shouldLock, value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    @Override
    public GenericSetting<C, Long> copy() {
        return new LongSetting<>(clazz, settingName.getValue(), defaultValue, minValue, maxValue, lock.get(), setter);
    }

    @Override
    public Long fromNumber(Number number) {
        return number.longValue();
    }
}