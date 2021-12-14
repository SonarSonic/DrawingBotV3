package drawingbot.files.presets;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.EnumJsonType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSettingsLoader<O extends AbstractJsonData> extends AbstractPresetLoader<O> {

    public ObservableList<GenericPreset<O>> presets = FXCollections.observableArrayList();

    public List<GenericSetting<?, ?>> settings = new ArrayList<>();

    public AbstractSettingsLoader(Class<O> dataType, EnumJsonType type, String configFile) {
        super(dataType, type, configFile);
        registerSettings();
    }

    public abstract void registerSettings();

    public void registerSetting(GenericSetting<?, ?> setting){
        settings.add(setting);
    }

    @Override
    public void registerPreset(GenericPreset<O> preset) {
        presets.add(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<O> preset) {
        presets.remove(preset);
    }

    @Override
    public GenericPreset<O> updatePreset(GenericPreset<O> preset) {
        GenericSetting.updateSettingsFromInstance(settings, DrawingBotV3.INSTANCE);
        preset.data.settingList = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<O> preset) {
        GenericSetting.applySettings(preset.data.settingList, settings);
        GenericSetting.applySettingsToInstance(settings, DrawingBotV3.INSTANCE);
    }

    @Override
    public GenericPreset<O> getDefaultPreset() {
        return null;
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (GenericPreset<O> preset : presets) {
            if (preset.userCreated) {
                userCreated.add(preset);
            }
        }
        return userCreated;
    }

    public List<String> getPresetSubTypes(){
        List<String> subTypes = new ArrayList<>();
        for (GenericPreset<O> preset : presets) {
            if (!preset.presetSubType.isEmpty() && !subTypes.contains(preset.presetSubType)) {
                subTypes.add(preset.presetSubType);
            }
        }
        return subTypes;
    }

    public List<GenericPreset<O>> getPresetsForSubType(String subType){
        List<GenericPreset<O>> subPresets = new ArrayList<>();
        for (GenericPreset<O> preset : presets) {
            if (preset.presetSubType.equals(subType)) {
                subPresets.add(preset);
            }
        }
        return subPresets;
    }

    public GenericPreset<O> getDefaultPresetsForSubType(String subType){
        for (GenericPreset<O> preset : presets) {
            if (preset.presetSubType.equals(subType)) {
                return preset;
            }
        }
        return null;
    }

}
