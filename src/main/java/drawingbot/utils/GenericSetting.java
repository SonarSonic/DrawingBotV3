package drawingbot.utils;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import processing.core.PApplet;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**a simple setting which can be easily altered in java fx, which can be randomised, reset, converted to and from a string*/
public class GenericSetting<C, V> {

    public final Class<C> clazz; //the class this setting can be applied to
    public final V defaultValue; //the default value for the setting
    public final Function<V, V> validator; //the validator checks a value and returns a valid setting
    public final Function<ThreadLocalRandom, V> randomiser; //the randomiser returns a valid random value
    public final BiConsumer<C, V> setter; //the setter sets the value in the class

    public final SimpleStringProperty settingName; //the settings name
    public final SimpleObjectProperty<V> value; //the current value
    public final StringConverter<V> stringConverter; //to convert the value to and from a string for javafx

    public static <C> GenericSetting<C, Boolean> createBooleanSetting(Class<C> pfmClass, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new BooleanStringConverter(), ThreadLocalRandom::nextBoolean, value -> value, setter);
    }

    public static <C> GenericSetting<C, Integer> createRangedIntSetting(Class<C> pfmClass, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new IntegerStringConverter(), rand -> rand.nextInt(minValue, maxValue), value -> PApplet.constrain(value, minValue, maxValue), setter);
    }

    public static <C> GenericSetting<C, Float> createRangedFloatSetting(Class<C> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new FloatStringConverter(), rand -> (float)rand.nextDouble(minValue, maxValue), value -> PApplet.constrain(value, minValue, maxValue), setter);
    }

    public static <C> GenericSetting<C, Long> createRangedLongSetting(Class<C> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new LongStringConverter(), rand -> rand.nextLong(minValue, maxValue), value -> (value < minValue) ? minValue : ((value > maxValue) ? maxValue : defaultValue), setter);
    }

    private GenericSetting(Class<C> clazz, String settingName, V defaultValue, StringConverter<V> stringConverter, Function<ThreadLocalRandom, V> randomiser, Function<V, V> validator, BiConsumer<C, V> setter) {
        this.clazz = clazz;
        this.settingName = new SimpleStringProperty(settingName);
        this.defaultValue = defaultValue;
        this.value = new SimpleObjectProperty<>(defaultValue);
        this.stringConverter = stringConverter;
        this.randomiser = randomiser;
        this.validator = validator;
        this.setter = setter;
    }

    public boolean isInstance(Object instance){
        return this.clazz.isInstance(instance);
    }

    public boolean isAssignableFrom(Class<?> clazz){
        return this.clazz.isAssignableFrom(clazz);
    }

    public String getValueAsString(){
        return stringConverter.toString(value.get());
    }

    public void setValueFromString(String obj){
        value.setValue(stringConverter.fromString(obj));
    }

    public void applySetting(Object instance){
        if(clazz.isInstance(instance)){
            setter.accept((C)instance, value.get());
        }
    }

    public void resetSetting(){
        value.setValue(defaultValue);
    }

    public void randomiseSetting(ThreadLocalRandom random){
        value.setValue(randomiser.apply(random));
    }

    public GenericSetting<C, V> copy(){
        return new GenericSetting<>(clazz, settingName.getValue(), defaultValue, stringConverter, randomiser, validator, setter);
    }
}