package drawingbot.javafx.preferences.items;

import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

/**
 * A {@link ElementNode} which wraps around a DrawingBotV3 {@link GenericSetting}
 * It will take the settings names and editors which edit the values live (e.g. sliders) will notify the setting when the value is changing with {@link GenericSetting#setValueChanging(boolean)}
 */
@Deprecated
public class SettingPresetNode<DATA> extends SettingNode<DATA> {

    public GenericSetting<?, DATA> setting;

    public SettingPresetNode(GenericSetting<?, DATA> setting, Property<DATA> dataProperty, Property<GenericPreset<PresetData>> presetProperty, TreeNode... children) {
        this("", setting, dataProperty, presetProperty, children);
    }

    public SettingPresetNode(String overrideName, GenericSetting<?, DATA> setting, Property<DATA> dataProperty, Property<GenericPreset<PresetData>> presetProperty, TreeNode... children) {
        super(overrideName, setting, children);
        this.setting = setting;
        this.dataProperty().bind(dataProperty);
        this.presetProperty().bind(presetProperty);

        InvalidationListener targetInvalidationListener = (v) -> {
            if(getData() != null){
                setting.setValue(setting.getValueFromInstance(getData()));
            }
        };
        this.data.addListener((observable, oldValue, newValue) -> {
            if(newValue instanceof Observable oldObservable){
                oldObservable.removeListener(targetInvalidationListener);
            }
            if(newValue instanceof Observable newObservable){
                newObservable.addListener(targetInvalidationListener);
            }
        });

        this.setting.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!setting.isValueChanging() && getData() != null){
                setting.applySetting(getData());
            }
        });

        this.propertyDisabledProperty().bind(setting.disabledProperty());
        if(overrideName.isEmpty()){
            this.nameProperty().bind(setting.displayNameProperty());
        }
    }

    ///////////////////////////////

    public ObjectProperty<DATA> data = new SimpleObjectProperty<>();

    public Object getData() {
        return data.get();
    }

    public ObjectProperty<DATA> dataProperty() {
        return data;
    }

    public SettingPresetNode<DATA> setData(DATA data) {
        this.data.set(data);
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

    public SettingPresetNode<DATA> setPreset(GenericPreset<PresetData> preset) {
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
