package drawingbot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import javafx.util.converter.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**a simple setting which can be easily altered in java fx, which can be randomised, reset, converted to and from a string & parsed to json*/
public class GenericSetting<C, V> {

    public final Class<C> clazz; //the class this setting can be applied to
    public final SimpleStringProperty settingName; //the settings name
    public final StringConverter<V> stringConverter; //to convert the value to and from a string for javafx
    public final Function<ThreadLocalRandom, V> randomiser; //the randomiser returns a valid random value
    public final Function<V, V> validator; //the validator checks a value and returns a valid setting
    public final BiConsumer<C, V> setter; //the setter sets the value in the class

    public final V defaultValue; //the default value for the setting
    public final SimpleObjectProperty<V> value; //the current value

    public static <C> GenericSetting<C, Boolean> createBooleanSetting(Class<C> pfmClass, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new BooleanStringConverter(), ThreadLocalRandom::nextBoolean, value -> value, setter);
    }

    public static <C> GenericSetting<C, String> createStringSetting(Class<C> pfmClass, String settingName, String defaultValue, BiConsumer<C, String> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new DefaultStringConverter(), (random) -> defaultValue, value -> value, setter);
    }

    public static <C> GenericSetting<C, Integer> createRangedIntSetting(Class<C> pfmClass, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new IntegerStringConverter(), rand -> rand.nextInt(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    public static <C> GenericSetting<C, Float> createRangedFloatSetting(Class<C> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new FloatStringConverter(), rand -> (float)rand.nextDouble(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
    }

    public static <C> GenericSetting<C, Long> createRangedLongSetting(Class<C> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        return new GenericSetting<>(pfmClass, settingName, defaultValue, new LongStringConverter(), rand -> rand.nextLong(minValue, maxValue), value -> Utils.clamp(value, minValue, maxValue), setter);
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

    public void setValue(Object v){
        value.setValue((V)v);
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

    public static JsonObject writeJsonObject(List<GenericSetting<?, ?>> settingList){
        JsonObject jsonObject = new JsonObject();
        for(GenericSetting<?, ?> setting : settingList){
            if(setting.value.get() != setting.defaultValue){
                jsonObject.addProperty(setting.toSafeName(setting.settingName.getValue()), setting.getValueAsString());
            }
        }
        return jsonObject;
    }

    public static void readJsonObject(JsonObject jsonObject, List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            JsonElement value = jsonObject.get(setting.toSafeName(setting.settingName.getValue()));
            if(value != null){
                setting.setValueFromString(value.getAsString());
            }else{
                setting.resetSetting();
            }
        }
    }

    public static ObservableList<GenericSetting<?, ?>> copy(ObservableList<GenericSetting<?, ?>> list, ObservableList<GenericSetting<?, ?>> dst){
        list.forEach(s -> dst.add(s.copy()));
        return dst;
    }

    public String toSafeName(String name){
        return name.replace(' ', '_').toLowerCase();
    }

    public GenericSetting<C, V> copy(){
        return new GenericSetting<>(clazz, settingName.getValue(), defaultValue, stringConverter, randomiser, validator, setter);
    }
}