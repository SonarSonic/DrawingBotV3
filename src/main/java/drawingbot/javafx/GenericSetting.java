package drawingbot.javafx;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import drawingbot.DrawingBotV3;
import drawingbot.javafx.controls.StringConverterGenericSetting;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.IEditableProperty;
import drawingbot.javafx.editors.IEditor;
import drawingbot.javafx.editors.IEditorFactory;
import drawingbot.javafx.settings.*;
import drawingbot.javafx.settings.bindings.BindingFactory;
import drawingbot.javafx.settings.bindings.SimpleBindingFactory;
import drawingbot.registry.Register;
import drawingbot.utils.SpecialListenable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**a simple setting which can be easily altered in java fx, which can be randomised, reset, converted to and from a string & parsed to json.
 * it can be applied to an instance when needed or permanently attached to an instance */
public abstract class GenericSetting<C, V> extends SpecialListenable<GenericSetting.Listener> implements Observable, IEditableProperty<V> {

    public final Class<C> clazz; //the class this setting can be applied to or belongs to
    public final Class<V> type; //the object type of the value this setting represents

    protected GenericSetting(GenericSetting<C, V> toCopy, V newValue) {
        this.clazz = toCopy.clazz;
        this.type = toCopy.type;
        this.setCategory(toCopy.category);
        this.setKey(toCopy.key);
        this.setAltKeys(toCopy.altKeys);
        this.setDisplayName(toCopy.displayName.get());
        this.setStringConverter(toCopy.stringConverter);
        this.setRandomiser(toCopy.randomiser);
        this.setValidator(toCopy.validator);
        this.setSetter(toCopy.setter);
        this.setGetter(toCopy.getter);
        this.setDefaultValue(toCopy.defaultValue);
        this.setDisabled(toCopy.isDisabled());
        this.setBindingFactory(toCopy.bindingFactory);
        this.setEditorFactory(toCopy.editorFactory);
        this.setRandomiseExclude(toCopy.getRandomiseExclude());
        this.setDocURLSuffix(toCopy.docURLSuffix);
        this.setDocURL(toCopy.docURL);
        this.setValue(newValue);
    }

    public GenericSetting(@Nullable Class<C> clazz, Class<V> type, String category, String key, V defaultValue) {
        this.clazz = clazz;
        this.type = type;
        this.setCategory(category);
        this.setKey(key);
        this.setDisplayName(key);
        this.setDefaultValue(defaultValue);
        this.setValue(defaultValue);
    }
    ////////////////////////////////

    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    ////////////////////////////////

    public String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    ////////////////////////////////

    /**
     * Used to register old keys for this setting for when settings are renamed
     */
    private List<String> altKeys;

    public List<String> getAltKeys() {
        return altKeys;
    }

    public GenericSetting<C, V> setAltKeys(List<String> altKeys) {
        this.altKeys = altKeys;
        return this;
    }

    public String updateKey(String key) {
        if (key.equals(getKey())) {
            return key;
        }
        if (getAltKeys() != null) {
            if (getAltKeys().contains(key)) {
                return getKey();
            }
        }
        return key;
    }

    public boolean testKey(String key) {
        if (key.equals(getKey())) {
            return true;
        }
        if (getAltKeys() != null) {
            return getAltKeys().contains(key);
        }
        return false;
    }

    public GenericSetting<C, V> addAltKey(String altKey) {
        if (altKeys == null) {
            altKeys = new ArrayList<>();
        }
        altKeys.add(altKey);
        return this;
    }

    ////////////////////////////////

    public final StringProperty displayName = new SimpleStringProperty();

    public String getDisplayName() {
        return displayName.get();
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public GenericSetting<C, V> setDisplayName(String displayName) {
        this.displayName.set(displayName);
        return this;
    }

    ////////////////////////////////

    public V defaultValue; //the default value for the setting

    public V getDefaultValue() {
        return defaultValue;
    }

    public final boolean isDefaultValue() {
        return isDefaultValue(value.getValue());
    }

    public boolean isDefaultValue(Object value) {
        return Objects.equals(value, defaultValue);
    }

    public GenericSetting<C, V> setDefaultValue(V defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    ////////////////////////////////

    public final SimpleObjectProperty<V> value = new SimpleObjectProperty<>(this, "value") {

        @Override
        public V get() {
            return validate(super.get());
        }

        @Override
        public void set(V newValue) {
            super.set(validate(newValue));
        }
    };

    public V get() {
        return getValue();
    }

    public V getValue() {
        if (validator != null) {
            return validate(value.getValue());
        }
        return value.getValue();
    }

    @Override
    public final String getValueAsString() {
        return getValueAsString(value.getValue());
    }

    public String getValueAsString(V value) {
        return getStringConverter().toString(value);
    }

    public void set(Object v) {
        setValue(v);
    }

    public void setValue(Object v) {
        V cast = type.cast(v);
        value.setValue(validate(cast));
    }

    public void setValueFromString(String obj) {
        value.setValue(getStringConverter().fromString(obj));
    }

    ////////////////////////////////

    //// EDITORS \\\\

    public IEditorFactory<V> editorFactory = null;

    public GenericSetting<C, V> setEditorFactory(IEditorFactory<V> editorFactory) {
        this.editorFactory = editorFactory;
        return this;
    }

    @Override
    public IEditorFactory<V> getEditorFactory() {
        return editorFactory == null ? defaultEditorFactory() : editorFactory;
    }

    public IEditor<V> createEditor(EditorContext context) {
        return getEditorFactory().createEditor(context, this);
    }

    public abstract IEditorFactory<V> defaultEditorFactory();


    @Override
    public SimpleObjectProperty<V> valueProperty() {
        return value;
    }

    @Override
    public void resetValue() {
        resetSetting();
    }

    @Override
    public void randomiseValue() {
        randomise(ThreadLocalRandom.current());
    }

    @Override
    public Class<V> getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return "";
    }

    ////////////////////////////////

    public ObservableList<V> recommendedValues;

    public GenericSetting<C, V> addRecommendedValues(V... values) {
        if (recommendedValues == null) {
            recommendedValues = FXCollections.observableArrayList();
        }
        recommendedValues.addAll(values);
        return this;
    }

    public boolean hasRecommendedValues() {
        return recommendedValues != null && !recommendedValues.isEmpty();
    }

    public ObservableList<V> getRecommendedValues() {
        return recommendedValues;
    }

    ////////////////////////////////

    private static final boolean defaultValueChanging = false;
    private BooleanProperty valueChanging = null; //should prevent randomising

    public boolean isValueChanging() {
        return valueChanging == null ? defaultValueChanging : valueChanging.get();
    }

    public BooleanProperty valueChangingProperty() {
        if (valueChanging == null) {
            valueChanging = new SimpleBooleanProperty(defaultValueChanging);
        }
        return valueChanging;
    }

    public GenericSetting<C, V> setValueChanging(boolean valueChanging) {
        valueChangingProperty().set(valueChanging);
        return this;
    }

    ////////////////////////////////

    /**
     * We implement Observable so it becomes simple to use a Generic Setting in a list of Observables.
     * For example when using it in conjunction with {@link drawingbot.api.IProperties}
     */

    @Override
    public void addListener(InvalidationListener listener) {
        valueProperty().addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        valueProperty().removeListener(listener);
    }


    ////////////////////////////////

    private Function<V, V> validator; //optional: the validator checks a value and returns a valid setting

    public Function<V, V> getValidator() {
        return validator;
    }

    public GenericSetting<C, V> setValidator(Function<V, V> validator) {
        this.validator = validator;
        return this;
    }

    public V validate(V value) {
        if (validator != null) {
            return validator.apply(value);
        }
        return defaultValidate(value);
    }

    protected V defaultValidate(V value) {
        return value;
    }

    ////////////////////////////////

    private Function<ThreadLocalRandom, V> randomiser; //optional: the randomiser returns a valid random value

    public GenericSetting<C, V> setRandomiser(Function<ThreadLocalRandom, V> randomiser) {
        this.randomiser = randomiser;
        return this;
    }

    public void randomise(ThreadLocalRandom random) {
        if (getRandomiseExclude()) {
            return;
        }
        if (randomiser != null) {
            setValue(randomiser.apply(random));
        } else {
            V nextRandom = defaultRandomise(random);
            if (nextRandom != null) {
                setValue(nextRandom);
            }
        }
    }

    protected V defaultRandomise(ThreadLocalRandom random) {
        return null;
    }

    ////////////////////////////////

    private StringConverter<V> stringConverter; //to convert the value to and from a string for javafx

    public StringConverter<V> getStringConverter() {
        if (stringConverter != null) {
            return stringConverter;
        }
        StringConverter<V> fallback = defaultStringConverter();
        if (fallback == null) {
            DrawingBotV3.logger.warning("Generic Setting: Missing string converter: " + getKey());
        }
        return fallback;
    }

    public GenericSetting<C, V> setStringConverter(StringConverter<V> stringConverter) {
        this.stringConverter = stringConverter;
        return this;
    }

    protected StringConverter<V> defaultStringConverter() {
        return new StringConverterGenericSetting<>(() -> this);
    }

    ////////////////////////////////

    public TextFormatter<V> createTextFormatter() {
        return new TextFormatter<V>(getStringConverter(), getDefaultValue());
    }

    ////////////////////////////////

    @Nullable
    private BiConsumer<C, V> setter; //the setter sets the value in the instance

    @Nullable
    public BiConsumer<C, V> getSetter() {
        return setter;
    }

    public GenericSetting<C, V> setSetter(BiConsumer<C, V> setter) {
        this.setter = setter;
        return this;
    }

    public void createDefaultSetter() {
        setSetter((C, V) -> setValue(V));
    }

    ////////////////////////////////

    @Nullable
    private Function<C, V> getter; //optional: the getter gets the value in the class

    @Nullable
    public Function<C, V> getGetter() {
        return getter;
    }

    public GenericSetting<C, V> setGetter(Function<C, V> getter) {
        this.getter = getter;
        return this;
    }

    public void createDefaultGetter() {
        setGetter((C) -> getValue());
    }

    ////////////////////////////////

    public void createDefaultGetterAndSetter() {
        createDefaultSetter();
        createDefaultGetter();
    }

    ////////////////////////////////

    public boolean toneMappingExclude = false; //should this setting be ignored when comparing tone map caches

    public boolean getToneMappingExclude() {
        return toneMappingExclude;
    }

    public GenericSetting<C, V> setToneMappingExclude(boolean exclude) {
        this.toneMappingExclude = exclude;
        return this;
    }

    ////////////////////////////////

    private static final boolean defaultDisabled = false;
    private BooleanProperty disabled = null; //should prevent randomising

    public boolean isDisabled() {
        return disabled == null ? defaultDisabled : disabled.get();
    }

    public BooleanProperty disabledProperty() {
        if (disabled == null) {
            disabled = new SimpleBooleanProperty(defaultDisabled);
        }
        return disabled;
    }

    public GenericSetting<C, V> setDisabled(boolean disabled) {
        if (this.disabled == null && disabled == defaultDisabled) {
            return this;
        }
        this.disabledProperty().set(disabled);
        return this;
    }

    ////////////////////////////////

    private BindingFactory bindingFactory;

    public BindingFactory getBindingFactory() {
        return bindingFactory;
    }

    public GenericSetting<C, V> setBindingFactory(BindingFactory bindingFactory) {
        this.bindingFactory = bindingFactory;
        return this;
    }

    public GenericSetting<C, V> createDisableBinding(String targetKey, boolean value) {
        setBindingFactory(new SimpleBindingFactory(targetKey, value));
        return this;
    }

    public GenericSetting<C, V> createDisableObjectBinding(String targetKey, Object value) {
        setBindingFactory(new SimpleBindingFactory(targetKey, value));
        return this;
    }

    public GenericSetting<C, V> createDisableObjectBindingNot(String targetKey, Object value) {
        setBindingFactory(new SimpleBindingFactory(targetKey, value).invert());
        return this;
    }

    /*
    public GenericSetting<C, V> createBindingFactory(String targetKey, Object value, BiConsumer<GenericSetting<C, V>, GenericSetting<?, ?>> bindingFactory){
        setBindingFactory((setting, settings) -> {
            GenericSetting<?, ?> disablingSetting = GenericSetting.findSetting(settings, targetKey);
            if(disablingSetting != null){
                bindingFactory.accept(this, disablingSetting);
            }
        });
        return this;
    }
     */

    public void removeBindings() {
        disabledProperty().unbind();
        valueProperty().unbind();
    }

    ////////////////////////////////

    private static final boolean defaultRandomiseExclude = false;
    private BooleanProperty randomiseExclude = null; //should prevent randomising

    public boolean getRandomiseExclude() {
        return randomiseExclude == null ? defaultRandomiseExclude : randomiseExclude.get();
    }

    public BooleanProperty randomiseExcludeProperty() {
        if (randomiseExclude == null) {
            randomiseExclude = new SimpleBooleanProperty(defaultRandomiseExclude);
        }
        return randomiseExclude;
    }

    public GenericSetting<C, V> setRandomiseExclude(boolean exclude) {
        if (this.randomiseExclude == null && exclude == defaultRandomiseExclude) {
            return this;
        }
        this.randomiseExcludeProperty().set(exclude);
        return this;
    }

    ////////////////////////////////

    public boolean isInstance(Object instance) {
        return this.clazz.isInstance(instance);
    }

    public boolean isAssignableFrom(Class<?> clazz) {
        return this.clazz.isAssignableFrom(clazz);
    }

    public JsonElement getValueAsJsonElement() {
        return getValueAsJsonElement(value.getValue());
    }

    public JsonElement getValueAsJsonElement(Object value) {
        return new JsonPrimitive(getValueAsString(type.cast(value)));
    }

    public V getValueFromJsonElement(JsonElement element) {
        if (element instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive) element;
            return getStringConverter().fromString(primitive.getAsString());
        }
        return defaultValue;
    }

    @Nullable
    public V getValueFromInstance(Object instance) {
        if (clazz.isInstance(instance)) {
            if (getter == null) {
                DrawingBotV3.logger.warning("Generic Setting: Missing getter: " + getKey());
                return null;
            }
            return getter.apply(clazz.cast(instance));

        }
        return null;
    }

    public void updateSetting(Object instance) {
        V value = getValueFromInstance(instance);
        if (value != null) {
            setValue(value);
        }
    }

    public final void applySetting(Object instance) {
        applySetting(instance, value.getValue());
    }

    public void applySetting(Object instance, Object value) {
        if (clazz.isInstance(instance)) {
            if (setter == null) {
                DrawingBotV3.logger.warning("Generic Setting: Missing setter: " + getKey());
                return;
            }
            setter.accept(clazz.cast(instance), validate(type.cast(value)));
        }
    }

    public void resetSetting() {
        value.setValue(defaultValue);
    }

    public String toSafeName(String name) {
        return name.replace(' ', '_').toLowerCase();
    }

    public void unbind() {
        value.unbind();
        randomiseExcludeProperty().unbind();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract GenericSetting<C, V> copy();

    public void sendUserEditedEvent() {
        sendListenerEvent(l -> l.onSettingUserEdited(this));
    }

    @Override
    public String toString() {
        return key + ": " + getValueAsString();
    }

    ///////////////////////////

    public interface Listener {

        /**
         * Called only when the generic setting is edited by a user.
         */
        default void onSettingUserEdited(GenericSetting<?, ?> setting) {
        }

    }

    ///////////////////////////

    /**
     * Defines a different URL to use for this settings documentation
     */
    private String docURL = null;

    public String getDocURL() {
        return docURL;
    }

    public GenericSetting<C, V> setDocURL(String docURL) {
        this.docURL = docURL;
        return this;
    }

    ///////////////////////////

    /**
     * Defines a different suffix to use for this settings documentation
     */
    private String docURLSuffix = null;

    public String getDocURLSuffix() {
        return docURLSuffix;
    }


    public GenericSetting<C, V> setDocURLTerm(String docURLTerm) {
        this.docURLSuffix = "#term-" + docURLTerm;
        return this;
    }

    public GenericSetting<C, V> setDocURLSuffix(String docURLSuffix) {
        this.docURLSuffix = docURLSuffix;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static HashMap<String, JsonElement> toJsonMap(List<GenericSetting<?, ?>> list, HashMap<String, JsonElement> dst, boolean changesOnly) {
        list.forEach(s -> {
            if (!changesOnly || !s.isDefaultValue()) {
                dst.put(s.getKey(), s.getValueAsJsonElement());
            }
        });
        return dst;
    }

    public static JsonObject toJsonObject(List<GenericSetting<?, ?>> settingList, @Nullable Object instance, boolean changesOnly) {
        JsonObject jsonObject = new JsonObject();

        for (GenericSetting<?, ?> setting : settingList) {
            Object value = instance == null ? null : setting.getValueFromInstance(instance);

            if (value == null) {
                value = setting.value.getValue();
            }

            if (!changesOnly || !setting.isDefaultValue(value)) {
                jsonObject.add(setting.getKey(), setting.getValueAsJsonElement(value));
            }
        }
        return jsonObject;
    }

    public static <I> I fromJsonObject(JsonObject jsonObject, List<GenericSetting<?, ?>> settingList, @Nullable I instance, boolean updateSettings) {
        for (GenericSetting<?, ?> setting : settingList) {
            Object value = setting.getValue();
            JsonElement element = jsonObject.get(setting.getKey());
            if (element == null && setting.getAltKeys() != null) {
                //attempt to retrieve the setting using old keys
                for (String altKey : setting.getAltKeys()) {
                    element = jsonObject.get(altKey);
                    if (element != null) {
                        break;
                    }
                }
            }
            if (element != null) {
                Object fromJson = setting.getValueFromJsonElement(element);
                value = fromJson != null ? fromJson : value;
            }
            setting.applySetting(instance, value);
            if (updateSettings) {
                setting.setValue(value);
            }
        }
        return instance;
    }

    public static void randomiseSettings(List<GenericSetting<?, ?>> settingList) {
        for (GenericSetting<?, ?> setting : settingList) {
            setting.randomise(ThreadLocalRandom.current());
        }
    }


    public static void resetSettings(List<GenericSetting<?, ?>> settingList) {
        for (GenericSetting<?, ?> setting : settingList) {
            setting.resetSetting();
        }
    }

    public static GenericSetting<?, ?> findSetting(Collection<GenericSetting<?, ?>> settingList, String key) {
        for (GenericSetting<?, ?> setting : settingList) {
            if (setting.testKey(key)) {
                return setting;
            }
        }
        return null;
    }


    public static List<GenericSetting<?, ?>> filterSettings(List<GenericSetting<?, ?>> settingList, Collection<String> keys) {
        List<GenericSetting<?, ?>> filteredList = new ArrayList<>();
        for (GenericSetting<?, ?> setting : settingList) {
            if (keys.contains(setting.getKey()) || setting.altKeys != null && !Collections.disjoint(keys, setting.altKeys)) {
                filteredList.add(setting);
            }
        }
        return filteredList;
    }

    public static void updateSettingsFromInstance(List<GenericSetting<?, ?>> src, Object instance) {
        for (GenericSetting<?, ?> setting : src) {
            setting.updateSetting(instance);
        }
    }

    public static void applySettingsToInstance(List<GenericSetting<?, ?>> src, Object instance) {
        for (GenericSetting<?, ?> setting : src) {
            setting.applySetting(instance);
        }
    }

    public static void applySettings(HashMap<String, JsonElement> src, List<GenericSetting<?, ?>> dst) {
        for (GenericSetting<?, ?> settingDst : dst) {
            applySettings(src, settingDst);
        }
    }

    public static void applySettings(HashMap<String, JsonElement> src, GenericSetting<?, ?> setting) {
        for (Map.Entry<String, JsonElement> settingSrc : src.entrySet()) {
            if (setting.testKey(settingSrc.getKey())) {
                setting.setValue(setting.getValueFromJsonElement(settingSrc.getValue()));
                return;
            }
        }
        setting.resetSetting(); //if the src list doesn't contain a value for the dst setting, reset it to default
    }

    public static void applySettings(List<GenericSetting<?, ?>> src, List<GenericSetting<?, ?>> dst) {
        for (GenericSetting<?, ?> settingDst : dst) {
            applySettings(src, settingDst);
        }
    }

    public static void applySettings(List<GenericSetting<?, ?>> src, GenericSetting<?, ?> setting) {
        for (GenericSetting<?, ?> settingSrc : src) {
            if (setting.testKey(settingSrc.getKey())) {
                setting.setValue(settingSrc.value.getValue());
                return;
            }
        }
        setting.resetSetting(); //if the src list doesn't contain a value for the dst setting, reset it to default
    }

    public static ObservableList<GenericSetting<?, ?>> copy(ObservableList<GenericSetting<?, ?>> list, ObservableList<GenericSetting<?, ?>> dst) {
        list.forEach(s -> dst.add(s.copy()));
        return dst;
    }

    public static List<GenericSetting<?, ?>> copy(List<GenericSetting<?, ?>> list, List<GenericSetting<?, ?>> dst) {
        list.forEach(s -> dst.add(s.copy()));
        return dst;
    }

    public static Map<String, String> saveValues(List<GenericSetting<?, ?>> list, Map<String, String> valueMap) {
        list.forEach(setting -> valueMap.put(setting.getKey(), setting.getValueAsString()));
        return valueMap;
    }

    public static void loadValues(List<GenericSetting<?, ?>> list, Map<String, String> valueMap) {
        list.forEach(setting -> {
            String value = valueMap.get(setting.getKey());
            if (value == null && setting.altKeys != null) {
                for (String altKey : setting.altKeys) {
                    value = valueMap.get(altKey);
                    if (value != null) {
                        break;
                    }
                }
            }
            if (value != null) {
                setting.setValueFromString(value);
            }
        });
    }


    public static Map<String, String> getValueMap(List<GenericSetting<?, ?>> pfmSettings, List<GenericSetting<?, ?>> cacheSettings) {
        Map<String, String> valueMap = new HashMap<>();
        for (GenericSetting<?, ?> cacheSetting : cacheSettings) {
            for (GenericSetting<?, ?> pfmSetting : pfmSettings) {
                if (pfmSetting.testKey(cacheSetting.getKey()) && Objects.equals(pfmSetting.clazz, cacheSetting.clazz)) {
                    valueMap.put(pfmSetting.getKey(), pfmSetting.getValueAsString());
                }
            }
        }
        return valueMap;
    }

    /**
     * Generate and set any disable/value bindings for the given settings list
     * e.g. if a property is disabled when another is also disabled, this will ensure the {@link #disabledProperty()} of the setting is bound appropriately to other settings in the list
     */
    public static void addBindings(List<GenericSetting<?, ?>> settings){
        for(GenericSetting<?, ?> setting : settings){
            if(setting.getBindingFactory() != null){
                setting.getBindingFactory().setupBindings(setting, settings);
            }
        }
    }

    /**
     * Remove any bindings generated for the {@link #addBindings(List)} method
     * Results in the removal of all disable/value bindings from the given settings list
     */
    public static void removeBindings(List<GenericSetting<?, ?>> settings){
        for(GenericSetting<?, ?> setting : settings){
            setting.removeBindings();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String settingName, Boolean defaultValue, Function<C, BooleanProperty> supplier){
        return createBooleanSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String category, String settingName, Boolean defaultValue, Function<C, BooleanProperty> supplier){
        return (BooleanSetting<C>) createBooleanSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter){
        return createBooleanSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String category, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter){
        return (BooleanSetting<C>) createBooleanSetting(clazz, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C> BooleanSetting<C> createBooleanSetting(Class<C> clazz, String category, String settingName, Boolean defaultValue){
        return new BooleanSetting<>(clazz, category, settingName, defaultValue);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String settingName, String defaultValue, Function<C, StringProperty> supplier){
        return createStringSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String category, String settingName, String defaultValue, Function<C, StringProperty> supplier){
        return (StringSetting<C>) createStringSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String settingName, String defaultValue, BiConsumer<C, String> setter){
        return createStringSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String category, String settingName, String defaultValue, BiConsumer<C, String> setter){
        return (StringSetting<C>) createStringSetting(clazz, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C> StringSetting<C> createStringSetting(Class<C> clazz, String category, String settingName, String defaultValue){
        return new StringSetting<>(clazz, category, settingName, defaultValue);
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String settingName, int defaultValue, Function<C, IntegerProperty> supplier){
        return createIntSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, Function<C, IntegerProperty> supplier){
        return (IntegerSetting<C>) createIntSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String settingName, int defaultValue, BiConsumer<C, Integer> setter){
        return createIntSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, BiConsumer<C, Integer> setter){
        return (IntegerSetting<C>) createIntSetting(clazz, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C> IntegerSetting<C> createIntSetting(Class<C> clazz, String category, String settingName, int defaultValue){
        return new IntegerSetting<>(clazz, category, settingName, defaultValue);
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String settingName, float defaultValue, Function<C, FloatProperty> supplier){
        return createFloatSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, Function<C, FloatProperty> supplier){
        return (FloatSetting<C>) createFloatSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String settingName, float defaultValue, BiConsumer<C, Float> setter) {
        return createFloatSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, BiConsumer<C, Float> setter){
        return (FloatSetting<C>) createFloatSetting(clazz, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C> FloatSetting<C> createFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue){
        return new FloatSetting<>(clazz, category, settingName, defaultValue);
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String settingName, double defaultValue, Function<C, DoubleProperty> supplier){
        return createDoubleSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, Function<C, DoubleProperty> supplier){
        return (DoubleSetting<C>) createDoubleSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String settingName, double defaultValue, BiConsumer<C, Double> setter){
        return createDoubleSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, BiConsumer<C, Double> setter){
        return (DoubleSetting<C>) createDoubleSetting(clazz, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C> DoubleSetting<C> createDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue){
        return new DoubleSetting<>(clazz, category, settingName, defaultValue);
    }

    public static <C> LongSetting<C> createLongSetting(Class<C> clazz, String settingName, long defaultValue, Function<C, LongProperty> supplier){
        return createLongSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C> LongSetting<C> createLongSetting(Class<C> clazz, String category, String settingName, long defaultValue, Function<C, LongProperty> supplier){
        return (LongSetting<C>) createLongSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> LongSetting<C> createLongSetting(Class<C> clazz, String settingName, long defaultValue, BiConsumer<C, Long> setter){
        return createLongSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C> LongSetting<C> createLongSetting(Class<C> clazz, String category, String settingName, long defaultValue, BiConsumer<C, Long> setter){
        return (LongSetting<C>) new LongSetting<>(clazz, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String settingName, int defaultValue, int minValue, int maxValue, Function<C, IntegerProperty> supplier){
        return createRangedIntSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, supplier);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, int minValue, int maxValue, Function<C, IntegerProperty> supplier){
        return (IntegerSetting<C>) createRangedIntSetting(clazz, category, settingName, defaultValue, minValue, maxValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        return createRangedIntSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter){
        return (IntegerSetting<C>) new IntegerSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue).setSetter(setter);
    }

    public static <C> IntegerSetting<C> createRangedIntSetting(Class<C> clazz, String category, String settingName, int defaultValue, int minValue, int maxValue){
        return new IntegerSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String settingName, float defaultValue, float minValue, float maxValue, Function<C, FloatProperty> supplier){
        return createRangedFloatSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, supplier);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, float minValue, float maxValue, Function<C, FloatProperty> supplier){
        return (FloatSetting<C>) createRangedFloatSetting(clazz, category, settingName, defaultValue, minValue, maxValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter) {
        return createRangedFloatSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter){
        return (FloatSetting<C>) new FloatSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue).setSetter(setter);
    }

    public static <C> FloatSetting<C> createRangedFloatSetting(Class<C> clazz, String category, String settingName, float defaultValue, float minValue, float maxValue){
        return new FloatSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String settingName, double defaultValue, double minValue, double maxValue, Function<C, DoubleProperty> supplier){
        return createRangedDoubleSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, supplier);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, double minValue, double maxValue, Function<C, DoubleProperty> supplier){
        return (DoubleSetting<C>) createRangedDoubleSetting(clazz, category, settingName, defaultValue, minValue, maxValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String settingName, double defaultValue, double minValue, double maxValue, BiConsumer<C, Double> setter){
        return createRangedDoubleSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, double minValue, double maxValue, BiConsumer<C, Double> setter){
        return (DoubleSetting<C>) createRangedDoubleSetting(clazz, category, settingName, defaultValue, minValue, maxValue).setSetter(setter);
    }

    public static <C> DoubleSetting<C> createRangedDoubleSetting(Class<C> clazz, String category, String settingName, double defaultValue, double minValue, double maxValue){
        return new DoubleSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue);
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String settingName, long defaultValue, long minValue, long maxValue, Function<C, LongProperty> supplier){
        return createRangedLongSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, supplier);
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String category, String settingName, long defaultValue, long minValue, long maxValue, Function<C, LongProperty> supplier){
        return (LongSetting<C>) createRangedLongSetting(clazz, category, settingName, defaultValue, minValue, maxValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        return createRangedLongSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue, minValue, maxValue, setter);
    }

    public static <C> LongSetting<C> createRangedLongSetting(Class<C> clazz, String category, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter){
        return (LongSetting<C>) new LongSetting<>(clazz, category, settingName, defaultValue, minValue, maxValue).setSetter(setter);
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String settingName, Color defaultValue, Function<C, ObjectProperty<Color>> supplier){
        return (ColourSetting<C>) createColourSetting(clazz, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String category, String settingName, Color defaultValue, Function<C, ObjectProperty<Color>> supplier){
        return (ColourSetting<C>) createColourSetting(clazz, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String settingName, Color defaultValue, BiConsumer<C, Color> setter){
        return createColourSetting(clazz, Register.CATEGORY_UNIQUE, settingName, defaultValue,  setter);
    }

    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String category, String settingName, Color defaultValue, BiConsumer<C, Color> setter){
        return (ColourSetting<C>) createColourSetting(clazz, category, settingName, defaultValue).setSetter(setter);
    }
    public static <C> ColourSetting<C> createColourSetting(Class<C> clazz, String category, String settingName, Color defaultValue){
        return new ColourSetting<>(clazz, category, settingName, defaultValue);
    }

    public static <C, V> ListSetting<C, V> createListSetting(Class<C> clazz, Class<V> objectType, String settingName, ArrayList<V> defaultValue, Function<C, ObservableList<V>> supplier){
        return createListSetting(clazz, objectType, Register.CATEGORY_UNIQUE, settingName, defaultValue, supplier);
    }

    public static <C, V> ListSetting<C, V> createListSetting(Class<C> clazz, Class<V> objectType, String category, String settingName, ArrayList<V> defaultValue, Function<C, ObservableList<V>> supplier){
        return (ListSetting<C, V>) createListSetting(clazz, objectType, category, settingName, defaultValue, (I, V) -> supplier.apply(I).setAll(V)).setGetter(I -> new ArrayList<>(supplier.apply(I)));
    }

    public static <C, V> ListSetting<C, V> createListSetting(Class<C> clazz, Class<V> objectType, String settingName, ArrayList<V> defaultValue, BiConsumer<C, ArrayList<V>> setter){
        return createListSetting(clazz, objectType, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C, V> ListSetting<C, V> createListSetting(Class<C> clazz, Class<V> objectType, String category, String settingName, ArrayList<V> defaultValue, BiConsumer<C, ArrayList<V>> setter){
        return (ListSetting<C, V>) new ListSetting<>(clazz, objectType, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C, V> ObjectSetting<C, V> createObjectSetting(Class<C> clazz, Class<V> objectType, String settingName, V defaultValue, Function<C, ObjectProperty<V>> supplier){
        return (ObjectSetting<C, V>) createObjectSetting(clazz, objectType, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C, V> ObjectSetting<C, V> createObjectSetting(Class<C> clazz, Class<V> objectType, String category, String settingName, V defaultValue, Function<C, ObjectProperty<V>> supplier){
        return (ObjectSetting<C, V>) createObjectSetting(clazz, objectType, category, settingName, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C, V> ObjectSetting<C, V> createObjectSetting(Class<C> clazz, Class<V> objectType, String settingName, V defaultValue, BiConsumer<C, V> setter){
        return createObjectSetting(clazz, objectType, Register.CATEGORY_UNIQUE, settingName, defaultValue, setter);
    }

    public static <C, V> ObjectSetting<C, V> createObjectSetting(Class<C> clazz, Class<V> objectType, String category, String settingName, V defaultValue, BiConsumer<C, V> setter){
        return (ObjectSetting<C, V>) createObjectSettingBase(clazz, objectType, category, settingName, defaultValue).setSetter(setter);
    }

    public static <C, V> ObjectSetting<C, V> createObjectSettingBase(Class<C> clazz, Class<V> objectType, String category, String settingName, V defaultValue){
        return new ObjectSetting<>(clazz, objectType, category, settingName, defaultValue);
    }

    public static <C, V> OptionSetting<C, V> createOptionSetting(Class<C> clazz, Class<V> type, String category, String settingName, ObservableList<V> values, V defaultValue, Function<C, ObjectProperty<V>> supplier){
        return (OptionSetting<C, V>) createOptionSetting(clazz, type, category, settingName, values, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C, V> OptionSetting<C, V> createOptionSetting(Class<C> clazz, Class<V> type, String settingName, ObservableList<V> values, V defaultValue, Function<C, ObjectProperty<V>> supplier){
        return (OptionSetting<C, V>) createOptionSetting(clazz, type, settingName, values, defaultValue, (I, V) -> supplier.apply(I).set(V)).setGetter(I -> supplier.apply(I).get());
    }

    public static <C, V> OptionSetting<C, V> createOptionSetting(Class<C> clazz, Class<V> type, String settingName, ObservableList<V> values, V defaultValue, BiConsumer<C, V> setter) {
        return createOptionSetting(clazz, type, Register.CATEGORY_UNIQUE, settingName, values, defaultValue, setter);
    }

    public static <C, V> OptionSetting<C, V> createOptionSetting(Class<C> clazz, Class<V> type, String category, String settingName, ObservableList<V> values, V defaultValue, BiConsumer<C, V> setter){
        return (OptionSetting<C, V>) createOptionSetting(clazz, type, category, settingName, values, defaultValue).setSetter(setter);
    }

    public static <C, V> OptionSetting<C, V> createOptionSetting(Class<C> clazz, Class<V> type, String category, String settingName, ObservableList<V> values, V defaultValue){
        return new OptionSetting<>(clazz, type, category, settingName, defaultValue, values);
    }
}