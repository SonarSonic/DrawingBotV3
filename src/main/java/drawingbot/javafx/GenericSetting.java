package drawingbot.javafx;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import drawingbot.DrawingBotV3;
import drawingbot.javafx.controls.StringConverterGenericSetting;
import drawingbot.javafx.settings.*;
import drawingbot.registry.Register;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**a simple setting which can be easily altered in java fx, which can be randomised, reset, converted to and from a string & parsed to json*/
public abstract class GenericSetting<C, V> implements ObservableValue<V> {

    public final Class<C> clazz; //the class this setting can be applied to
    public final SimpleStringProperty key = new SimpleStringProperty(); //the settings name
    public StringConverter<V> stringConverter; //to convert the value to and from a string for javafx
    public Function<V, V> validator; //the validator checks a value and returns a valid setting
    public BiConsumer<C, V> setter; //the setter sets the value in the instance
    public Function<ThreadLocalRandom, V> randomiser; //optional: the randomiser returns a valid random value
    public Function<C, V> getter; //optional: the getter gets the value in the class

    public V defaultValue; //the default value for the setting
    public final SimpleObjectProperty<V> value = new SimpleObjectProperty<>(); //the current value
    public final SimpleBooleanProperty randomiseExclude = new SimpleBooleanProperty(false); //should prevent randomising
    public String category = ""; //optional category identifier

    public transient TextField textField;
    public transient Node defaultNode;
    public transient Node labelledNode;

    protected GenericSetting(GenericSetting<C, V> toCopy, V newValue){
        this.clazz = toCopy.clazz;
        this.setKey(toCopy.key.get());
        this.setStringConverter(toCopy.stringConverter);
        this.setRandomiser(toCopy.randomiser);
        this.setValidator(toCopy.validator);
        this.setSetter(toCopy.setter);
        this.setGetter(toCopy.getter);
        this.setDefaultValue(toCopy.defaultValue);
        this.setRandomiseExclude(toCopy.randomiseExclude.get());
        this.setCategory(toCopy.category);

        this.setValue(newValue);
    }

    public GenericSetting(Class<C> clazz, String category, String key, V defaultValue, StringConverter<V> stringConverter, Function<V, V> validator, BiConsumer<C, V> setter) {
        this.clazz = clazz;
        this.category = category;
        this.key.set(key);
        this.defaultValue = defaultValue;
        this.value.set(defaultValue);
        this.stringConverter = stringConverter;
        this.validator = validator;
        this.setter = setter;
        this.randomiseExclude.set(false);
    }

    public String getKey(){
        return key.get();
    }

    public GenericSetting<C, V> setKey(String key) {
        this.key.set(key);
        return this;
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

    public GenericSetting<C, V> setDefaultValue(V defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public GenericSetting<C, V> setCategory(String category){
        this.category = category;
        return this;
    }

    public GenericSetting<C, V> setRandomiseExclude(boolean exclude){
        this.randomiseExclude.set(exclude);
        return this;
    }

    public boolean isInstance(Object instance){
        return this.clazz.isInstance(instance);
    }

    public boolean isAssignableFrom(Class<?> clazz){
        return this.clazz.isAssignableFrom(clazz);
    }

    public JsonElement getValueAsJsonElement(){
        return getValueAsJsonElement(value.get());
    }

    public JsonElement getValueAsJsonElement(Object value){
        return new JsonPrimitive(getValueAsString((V)value));
    }

    public V getValueFromJsonElement(JsonElement element){
        if(element instanceof JsonPrimitive){
            JsonPrimitive primitive = (JsonPrimitive) element;
            return stringConverter.fromString(primitive.getAsString());
        }
        return defaultValue;
    }

    public final String getValueAsString(){
        return getValueAsString(value.get());
    }

    public String getValueAsString(V value){
        return stringConverter.toString(value);
    }

    public void setValue(Object v){
        value.setValue(validator != null ? validator.apply((V)v) : (V)v);
    }

    public void setValueFromString(String obj){
        value.setValue(stringConverter.fromString(obj));
    }

    @Nullable
    public V getValueFromInstance(Object instance){
        if(clazz.isInstance(instance)){
            if(getter == null){
                DrawingBotV3.logger.warning("Generic Setting: Missing getter: " + getKey());
                return null;
            }
            return getter.apply((C)instance);

        }
        return null;
    }

    public void updateSetting(Object instance){
        V value = getValueFromInstance(instance);
        if(value != null){
            setValue(value);
        }
    }

    public final void applySetting(Object instance){
        applySetting(instance, value.get());
    }

    public void applySetting(Object instance, Object value){
        if(clazz.isInstance(instance)){
            if(setter == null){
                DrawingBotV3.logger.warning("Generic Setting: Missing setter: " + getKey());
                return;
            }
            setter.accept((C)instance, (V)value);
        }
    }

    public void resetSetting(){
        value.setValue(defaultValue);
    }

    public void randomiseSetting(ThreadLocalRandom random){
        if(randomiseExclude.get() || randomiser == null){
            return;
        }
        setValue(randomiser.apply(random));
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

    public final boolean isDefaultValue(){
        return isDefaultValue(value.get());
    }

    public boolean isDefaultValue(Object value){
        return Objects.equals(value, defaultValue);
    }

    public String toSafeName(String name){
        return name.replace(' ', '_').toLowerCase();
    }

    public void unbind(){
        value.unbind();
        randomiseExclude.unbind();
    }

    public boolean hasEditableTextField(){
        return true;
    }

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
        return key.get() + ": " + getValueAsString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static HashMap<String, JsonElement> toJsonMap(List<GenericSetting<?, ?>> list, HashMap<String, JsonElement> dst, boolean changesOnly){
        list.forEach(s -> {
            if(!changesOnly || !s.isDefaultValue()){
                dst.put(s.getKey(), s.getValueAsJsonElement());
            }
        });
        return dst;
    }

    public static JsonObject toJsonObject(List<GenericSetting<?, ?>> settingList, @Nullable Object instance, boolean changesOnly){
        JsonObject jsonObject = new JsonObject();

        for(GenericSetting<?, ?> setting : settingList){
            Object value = instance == null ? null : setting.getValueFromInstance(instance);

            if(value == null) {
                value = setting.value.get();
            }

            if(!changesOnly || !setting.isDefaultValue(value)){
                jsonObject.add(setting.getKey(), setting.getValueAsJsonElement(value));
            }
        }
        return jsonObject;
    }

    public static <I> I fromJsonObject(JsonObject jsonObject, List<GenericSetting<?, ?>> settingList, @Nullable I instance, boolean updateSettings){
        for(GenericSetting<?, ?> setting : settingList){
            Object value = setting.getValue();
            JsonElement element = jsonObject.get(setting.getKey());
            if(element != null){
                Object fromJson = setting.getValueFromJsonElement(element);
                value = fromJson != null ? fromJson : value;
            }
            setting.applySetting(instance, value);
            if(updateSettings){
                setting.setValue(value);
            }
        }
        return instance;
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

    public static void applySettings(HashMap<String, JsonElement> src, List<GenericSetting<?, ?>> dst){
        dst: for(GenericSetting<?, ?> settingDst : dst){

            for(Map.Entry<String, JsonElement> settingSrc : src.entrySet()){
                if(settingSrc.getKey().equals(settingDst.getKey())){

                    settingDst.setValue(settingDst.getValueFromJsonElement(settingSrc.getValue()));
                    continue dst;
                }
            }
            settingDst.resetSetting(); //if the src list doesn't contain a value for the dst setting, reset it to default
        }
    }

    public static void applySettings(List<GenericSetting<?, ?>> src, List<GenericSetting<?, ?>> dst){
        dst: for(GenericSetting<?, ?> settingDst : dst){

            for(GenericSetting<?, ?> settingSrc : src){
                if(settingSrc.getKey().equals(settingDst.getKey())){
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

    public static void saveValues(List<GenericSetting<?, ?>> list,  Map<String, String> valueMap){
        list.forEach(setting -> valueMap.put(setting.getKey(), setting.getValueAsString()));
    }

    public static void loadValues(List<GenericSetting<?, ?>> list,  Map<String, String> valueMap){
        list.forEach(setting -> {
            String value = valueMap.get(setting.getKey());
            if(value != null){
                setting.setValueFromString(value);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter){
        return createBooleanSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String category, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter){
        return new BooleanSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String settingName, String defaultValue, BiConsumer<C, String> setter){
        return createStringSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String category, String settingName, String defaultValue, BiConsumer<C, String> setter){
        return new StringSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String settingName, int defaultValue, BiConsumer<C, Integer> setter){
        return createIntSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, BiConsumer<C, Integer> setter){
        return new IntegerSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String settingName, float defaultValue, BiConsumer<C, Float> setter) {
        return createFloatSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, BiConsumer<C, Float> setter){
        return new FloatSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String settingName, double defaultValue, BiConsumer<C, Double> setter){
        return createDoubleSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, BiConsumer<C, Double> setter){
        return new DoubleSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C> LongSetting<C> createLongSetting(Class<C> clazz, String settingName, long defaultValue, BiConsumer<C, Long> setter){
        return createLongSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> LongSetting<C> createLongSetting(Class<C> clazz, String category, String settingName, long defaultValue, BiConsumer<C, Long> setter){
        return new LongSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        return createRangedIntSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        return new IntegerSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter) {
        return createRangedFloatSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter){
        return new FloatSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String settingName, double defaultValue, double minValue, double maxValue, BiConsumer<C, Double> setter){
        return createRangedDoubleSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, double minValue, double maxValue, BiConsumer<C, Double> setter){
        return new DoubleSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        return createRangedLongSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String category, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        return new LongSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String settingName, Color defaultValue, BiConsumer<C, Color> setter){
        return createColourSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue,  setter);
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String category, String settingName, Color defaultValue, BiConsumer<C, Color> setter){
        return new ColourSetting<>(clazz, category, settingName, defaultValue, setter);
    }

    public static <C, V> ListSetting<C, V> createListSetting(Class<C> clazz, Class<V> objectType, String settingName, ArrayList<V> defaultValue, BiConsumer<C, ArrayList<V>> setter){
        return createListSetting(clazz, objectType, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C, V> ListSetting<C, V> createListSetting(Class<C> clazz, Class<V> objectType, String category, String settingName, ArrayList<V> defaultValue, BiConsumer<C, ArrayList<V>> setter){
        return new ListSetting<>(clazz, objectType, category, settingName, defaultValue, setter);
    }

    public static <C, V> ObjectSetting<C, V> createObjectSetting(Class<C> clazz, Class<V> objectType, String settingName, V defaultValue, BiConsumer<C, V> setter){
        return createObjectSetting(clazz, objectType, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C, V> ObjectSetting<C, V> createObjectSetting(Class<C> clazz, Class<V> objectType, String category, String settingName, V defaultValue, BiConsumer<C, V> setter){
        return new ObjectSetting<>(clazz, objectType, category, settingName, defaultValue, setter);
    }

    public static <C, V> GenericSetting<C, V> createOptionSetting(Class<C> clazz, String settingName, List<V> values, V defaultValue, BiConsumer<C, V> setter) {
        return createOptionSetting(clazz, Register.CATEGORY_UNIQUE, settingName, values, defaultValue, setter);
    }

    public static <C, V> GenericSetting<C, V> createOptionSetting(Class<C> clazz, String category, String settingName, List<V> values, V defaultValue, BiConsumer<C, V> setter){
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
        return new OptionSetting<>(clazz, category, settingName, defaultValue, optionStringConverter, values, setter);
    }
}