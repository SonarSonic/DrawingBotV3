package drawingbot.files.presets;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractSettingsLoader<O extends AbstractJsonData> extends AbstractPresetLoader<O> {

    public List<GenericSetting<?, ?>> settings = new ArrayList<>();

    public AbstractSettingsLoader(Class<O> dataType, PresetType type, String configFile) {
        super(dataType, type, configFile);
        registerSettings();
    }

    public abstract void registerSettings();

    public void registerSetting(GenericSetting<?, ?> setting){
        settings.add(setting);
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


}
