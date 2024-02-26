package drawingbot.javafx.preferences.items;

import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A {@link ElementNode} which wraps around a DrawingBotV3 {@link GenericSetting}
 * It will take the settings names and editors which edit the values live (e.g. sliders) will notify the setting when the value is changing with {@link GenericSetting#setValueChanging(boolean)}
 */
@Deprecated
public class SettingPresetTargetNode<TARGET, DATA> extends SettingNode<DATA> {

    public GenericSetting<TARGET, DATA> setting;

    public SettingPresetTargetNode(GenericSetting<TARGET, DATA> setting, Property<TARGET> targetProperty, Property<GenericPreset<PresetData>> presetProperty, TreeNode... children) {
        this("", setting, targetProperty, presetProperty, children);
    }

    public SettingPresetTargetNode(String overrideName, GenericSetting<TARGET, DATA> setting, Property<TARGET> targetProperty, Property<GenericPreset<PresetData>> presetProperty, TreeNode... children) {
        super(overrideName.isEmpty() ? setting.getDisplayName() : overrideName, setting, children);
        this.setting = setting;
        this.targetProperty().bind(targetProperty);
        this.presetProperty().bind(presetProperty);

        InvalidationListener targetInvalidationListener = (v) -> {
            if(getTarget() != null){
                setting.setValue(setting.getValueFromInstance(getTarget()));
            }
        };
        this.target.addListener((observable, oldValue, newValue) -> {
            if(newValue instanceof Observable oldObservable){
                oldObservable.removeListener(targetInvalidationListener);
            }
            if(newValue instanceof Observable newObservable){
                newObservable.addListener(targetInvalidationListener);
            }
        });

        this.setting.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!setting.isValueChanging() && getTarget() != null){
                setting.applySetting(getTarget());
            }
        });

        this.propertyDisabledProperty().bind(setting.disabledProperty());
        if(overrideName.isEmpty()){
            this.nameProperty().bind(setting.displayNameProperty());
        }
    }

    ///////////////////////////////

    public ObjectProperty<TARGET> target = new SimpleObjectProperty<>();

    public Object getTarget() {
        return target.get();
    }

    public ObjectProperty<TARGET> targetProperty() {
        return target;
    }

    public SettingPresetTargetNode<TARGET, DATA> setTarget(TARGET target) {
        this.target.set(target);
        return this;
    }

    ///////////////////////////////

    public ObjectProperty<GenericPreset<PresetData>> preset = new SimpleObjectProperty<>();

    public GenericPreset<PresetData> getPreset() {
        return preset.get();
    }

    public ObjectProperty<GenericPreset<PresetData>> presetProperty() {
        return preset;
    }

    public SettingPresetTargetNode<TARGET, DATA> setPreset(GenericPreset<PresetData> preset) {
        this.preset.set(preset);
        return this;
    }

    ///////////////////////////////

    @Override
    public void resetProperty() {
        if(getPreset() != null){
            GenericSetting.applySettings(getPreset().data.settings, setting);
        }else{
            setting.resetSetting();
        }
    }

}
