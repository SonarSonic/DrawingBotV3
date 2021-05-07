package drawingbot.javafx;

import drawingbot.javafx.controls.StringConverterGenericSetting;
import drawingbot.javafx.settings.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**a simple setting which can be easily altered in java fx, which can be randomised, reset, converted to and from a string & parsed to json*/
public abstract class GenericSetting<C, V> implements ObservableValue<V> {

    public final Class<C> clazz; //the class this setting can be applied to
    public final SimpleStringProperty settingName; //the settings name
    public StringConverter<V> stringConverter; //to convert the value to and from a string for javafx
    public Function<ThreadLocalRandom, V> randomiser; //the randomiser returns a valid random value
    public Function<V, V> validator; //the validator checks a value and returns a valid setting
    public BiConsumer<C, V> setter; //the setter sets the value in the class

    public final V defaultValue; //the default value for the setting
    public final SimpleObjectProperty<V> value; //the current value
    public final SimpleBooleanProperty lock; //the current value

    ///optional
    public Function<C, V> getter; //the getter gets the value in the class

    public GenericSetting(Class<C> clazz, String settingName, V defaultValue, StringConverter<V> stringConverter, Function<ThreadLocalRandom, V> randomiser, boolean shouldLock, Function<V, V> validator, BiConsumer<C, V> setter) {
        this.clazz = clazz;
        this.settingName = new SimpleStringProperty(settingName);
        this.defaultValue = defaultValue;
        this.value = new SimpleObjectProperty<>(defaultValue);
        this.stringConverter = stringConverter;
        this.randomiser = randomiser;
        this.validator = validator;
        this.setter = setter;
        this.lock = new SimpleBooleanProperty(shouldLock);
    }

    public GenericSetting<C, V> setStringConverter(StringConverter<V> stringConverter){
        this.stringConverter = stringConverter;
        return this;
    }

    public GenericSetting<C, V> setRandomiser(Function<ThreadLocalRandom, V> randomiser){
        this.randomiser = randomiser;
        return this;
    }

    public GenericSetting<C, V> setValidator(Function<V, V> validator){
        this.validator = validator;
        return this;
    }

    public GenericSetting<C, V> setSetter(BiConsumer<C, V> setter){
        this.setter = setter;
        return this;
    }

    public GenericSetting<C, V> setGetter(Function<C, V> getter){
        this.getter = getter;
        return this;
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
        value.setValue(validator.apply((V)v));
    }

    public void setValueFromString(String obj){
        value.setValue(stringConverter.fromString(obj));
    }

    public void updateSetting(Object instance){
        if(clazz.isInstance(instance)){
            setValue(getter.apply((C)instance));
        }
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
        if(lock.get()){
            return;
        }
        value.setValue(validator.apply(randomiser.apply(random)));
    }

    @Override
    public void addListener(InvalidationListener listener) {
        value.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        value.removeListener(listener);
    }

    @Override
    public void addListener(ChangeListener<? super V> listener) {
        value.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super V> listener) {
        value.removeListener(listener);
    }

    @Override
    public V getValue() {
        return value.get();
    }

    public String toSafeName(String name){
        return name.replace(' ', '_').toLowerCase();
    }

    public void unbind(){
        value.unbind();
        lock.unbind();
    }

    public TextField textField;
    public Node defaultNode;
    public Node labelledNode;

    public TextField getEditableTextField(){
        if(textField == null){
            textField = new TextField();
            textField.setTextFormatter(new TextFormatter<>(new StringConverterGenericSetting<>(() -> this)));
            textField.setText(getValueAsString());
            textField.setPrefWidth(80);

            textField.setOnAction(e -> setValueFromString(textField.getText()));
            value.addListener((observable, oldValue, newValue) -> textField.setText(getValueAsString()));
        }
        return textField;
    }

    public Node getJavaFXNode(boolean label){
        if(label){
            if(labelledNode == null){
                labelledNode = createJavaFXNode(true);
            }
            return labelledNode;
        }else{
            if(defaultNode == null){
                defaultNode = createJavaFXNode(false);
            }
            return defaultNode;
        }
    }

    protected abstract Node createJavaFXNode(boolean label);

    public abstract GenericSetting<C, V> copy();


    @Override
    public String toString() {
        return settingName.get() + ": " + getValueAsString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static HashMap<String, String> toJsonMap(List<GenericSetting<?, ?>> list, HashMap<String, String> dst){
        list.forEach(s -> {
            if(!s.value.get().equals(s.defaultValue)){
                dst.put(s.settingName.getValue(), s.getValueAsString());
            }
        });
        return dst;
    }

    public static void randomiseSettings(List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            setting.randomiseSetting(ThreadLocalRandom.current());
        }
    }


    public static void resetSettings(List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            setting.resetSetting();
        }
    }

    public static void updateSettingsFromInstance(List<GenericSetting<?, ?>> src, Object instance){
        for(GenericSetting<?, ?> setting : src){
            setting.updateSetting(instance);
        }
    }

    public static void applySettingsToInstance(List<GenericSetting<?, ?>> src, Object instance){
        for(GenericSetting<?, ?> setting : src){
            setting.applySetting(instance);
        }
    }

    public static void applySettings(HashMap<String, String> src, List<GenericSetting<?, ?>> dst){
        dst: for(GenericSetting<?, ?> settingDst : dst){

            for(Map.Entry<String, String> settingSrc : src.entrySet()){
                if(settingSrc.getKey().equals(settingDst.settingName.getValue())){
                    settingDst.setValueFromString(settingSrc.getValue());
                    continue dst;
                }
            }
            settingDst.resetSetting(); //if the src list doesn't contain a value for the dst setting, reset it to default
        }
    }

    public static void applySettings(List<GenericSetting<?, ?>> src, List<GenericSetting<?, ?>> dst){
        dst: for(GenericSetting<?, ?> settingDst : dst){

            for(GenericSetting<?, ?> settingSrc : src){
                if(settingSrc.settingName.getValue().equals(settingDst.settingName.getValue())){
                    settingDst.setValue(settingSrc.value.get());
                    continue dst;
                }
            }
            settingDst.resetSetting(); //if the src list doesn't contain a value for the dst setting, reset it to default
        }
    }

    public static ObservableList<GenericSetting<?, ?>> copy(ObservableList<GenericSetting<?, ?>> list, ObservableList<GenericSetting<?, ?>> dst){
        list.forEach(s -> dst.add(s.copy()));
        return dst;
    }

    public static List<GenericSetting<?, ?>> copy(List<GenericSetting<?, ?>> list, List<GenericSetting<?, ?>> dst){
        list.forEach(s -> dst.add(s.copy()));
        return dst;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String settingName, Boolean defaultValue, boolean shouldLock, BiConsumer<C, Boolean> setter){
        return new BooleanSetting<>(clazz, settingName, defaultValue, shouldLock, setter);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String settingName, String defaultValue, boolean shouldLock, BiConsumer<C, String> setter){
        return new StringSetting<>(clazz, settingName, defaultValue, shouldLock, setter);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String settingName, int defaultValue, int minValue, int maxValue, boolean shouldLock, BiConsumer<C, Integer> setter){
        return new IntegerSetting<>(clazz, settingName, defaultValue, minValue, maxValue, shouldLock, setter);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String settingName, float defaultValue, float minValue, float maxValue, boolean shouldLock, BiConsumer<C, Float> setter){
        return new FloatSetting<>(clazz, settingName, defaultValue, minValue, maxValue, shouldLock, setter);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String settingName, double defaultValue, double minValue, double maxValue, boolean shouldLock, BiConsumer<C, Double> setter){
        return new DoubleSetting<>(clazz, settingName, defaultValue, minValue, maxValue, shouldLock, setter);
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String settingName, long defaultValue, long minValue, long maxValue, boolean shouldLock, BiConsumer<C, Long> setter){
        return new LongSetting<>(clazz, settingName, defaultValue, minValue, maxValue, shouldLock, setter);
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String settingName, Color defaultValue, boolean shouldLock, BiConsumer<C, Color> setter){
        return new ColourSetting<>(clazz, settingName, defaultValue, shouldLock, setter);
    }

    public static <C, V> GenericSetting<C, V> createOptionSetting(Class<C> clazz, String settingName, List<V> values, V defaultValue, boolean shouldLock, BiConsumer<C, V> setter){
        StringConverter<V> optionStringConverter = new StringConverter<V>() {
            @Override
            public String toString(V object) {
                return object.toString();
            }
            @Override
            public V fromString(String string) {
                for(V v : values){
                    if(v.toString().equals(string)){
                        return v;
                    }
                }
                return null;
            }
        };
        return new OptionSetting<>(clazz, settingName, optionStringConverter, values, defaultValue, shouldLock, setter);
    }
}