package drawingbot.pfm;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import processing.core.PApplet;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PFMSetting<P extends IPFM, V> {

    public Class<P> pfmClass;
    public V defaultValue;
    public Function<V, V> validator;
    public Function<ThreadLocalRandom, V> randomiser;
    public BiConsumer<P, V> setter;

    public SimpleStringProperty settingName;
    public SimpleObjectProperty<V> value;
    public StringConverter<V> stringConverter;


    public PFMSetting(Class<P> pfmClass, String settingName, V defaultValue, StringConverter<V> stringConverter, Function<ThreadLocalRandom, V> randomiser, Function<V, V> validator, BiConsumer<P, V> setter) {
        this.pfmClass = pfmClass;
        this.settingName = new SimpleStringProperty(settingName);
        this.defaultValue = defaultValue;
        this.value = new SimpleObjectProperty<>(defaultValue);
        this.stringConverter = stringConverter;
        this.randomiser = randomiser;
        this.validator = validator;
        this.setter = setter;
    }

    public static <P extends IPFM> PFMSetting<P, Long> createRangedLongSetting(Class<P> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<P, Long> setter){
        return new PFMSetting<>(pfmClass, settingName, defaultValue, new LongStringConverter(), rand -> rand.nextLong(minValue, maxValue), value -> (value < minValue) ? minValue : ((value > maxValue) ? maxValue : defaultValue), setter);
    }

    public static <P extends IPFM> PFMSetting<P, Float> createRangedFloatSetting(Class<P> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<P, Float> setter){
        return new PFMSetting<>(pfmClass, settingName, defaultValue, new FloatStringConverter(), rand -> (float)rand.nextDouble(minValue, maxValue), value -> PApplet.constrain(value, minValue, maxValue), setter);
    }

    public static <P extends IPFM> PFMSetting<P, Integer> createRangedIntSetting(Class<P> pfmClass, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<P, Integer> setter){
        return new PFMSetting<>(pfmClass, settingName, defaultValue, new IntegerStringConverter(), rand -> rand.nextInt(minValue, maxValue), value -> PApplet.constrain(value, minValue, maxValue), setter);
    }

    public void registerSetting(){
        PFMSettingsRegistry.settingsMap.putIfAbsent(pfmClass, FXCollections.observableArrayList());
        PFMSettingsRegistry.settingsMap.get(pfmClass).add(this);
        value.addListener((observable, oldValue, newValue) -> PFMSettingsRegistry.onSettingChanged(this, observable, oldValue, newValue));
    }

    public void applySetting(IPFM pfm){
        if(pfmClass.isInstance(pfm)){
            setter.accept((P)pfm, value.get());
        }
    }

    public void resetSetting(){
        value.setValue(defaultValue);
    }

    public void randomiseSetting(ThreadLocalRandom random){
        value.setValue(randomiser.apply(random));
    }

}
