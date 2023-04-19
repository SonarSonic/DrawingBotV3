package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import com.sun.javafx.binding.BidirectionalBinding;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.util.StringConverter;
import javafx.util.converter.LongStringConverter;

import java.util.concurrent.ThreadLocalRandom;

public class LongSetting<C> extends AbstractNumberSetting<C, Long> {

    public static StringConverter<Long> stringConverter = new LongStringConverter();

    protected LongSetting(LongSetting<C> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public LongSetting(Class<C> clazz, String category, String settingName, long defaultValue) {
        super(clazz, Long.class, category, settingName, defaultValue);
    }

    public LongSetting(Class<C> clazz, String category, String settingName, long defaultValue, long minValue, long maxValue){
        super(clazz, Long.class, category, settingName, defaultValue, minValue, maxValue);
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

    //////////////////////////

    public Long getValueFromJsonElement(JsonElement element){
        return element.getAsLong();
    }

    //////////////////////////

    private LongProperty property = null;

    public LongProperty asLongProperty(){
        if(property == null){
            property = new SimpleLongProperty(getValue());
            BidirectionalBinding.bindNumber(property, valueProperty());
        }
        return property;
    }

    //////////////////////////

    private DoubleProperty propertyD = null;

    public DoubleProperty asDoubleProperty(){
        if(propertyD == null){
            propertyD = new SimpleDoubleProperty(getValue());

            propertyD.setValue(getValue());
            propertyD.getValue();
            propertyD.addListener((observable, oldValue, newValue) -> valueProperty().set(newValue.longValue()));
            valueProperty().addListener((observable, oldValue, newValue) -> propertyD.set(newValue.doubleValue()));
        }
        return propertyD;
    }

}