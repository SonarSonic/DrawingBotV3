package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import java.util.HashMap;

public abstract class PresetPFMSettingsManager extends AbstractPresetManager<PresetPFMSettings> {

    public PresetPFMSettingsManager(PresetPFMSettingsLoader presetLoader) {
        super(presetLoader);
    }

    public abstract Property<PFMFactory<?>> pfmProperty();

    public abstract Property<ObservableList<GenericSetting<?, ?>>> settingProperty();

    @Override
    public GenericPreset<PresetPFMSettings> updatePreset(GenericPreset<PresetPFMSettings> preset) {
        PFMFactory<?> pfm = pfmProperty().getValue();
        ObservableList<GenericSetting<?, ?>> settings = settingProperty().getValue();
        if(pfm != null && settings != null) {
            preset.presetSubType = pfm.getName();
            preset.data.settingList = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
        }
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetPFMSettings> preset) {
        pfmProperty().setValue(MasterRegistry.INSTANCE.getPFMFactory(preset.presetSubType));
        Property<ObservableList<GenericSetting<?, ?>>> settings = settingProperty();
        GenericSetting.applySettings(preset.data.settingList, settings.getValue());
    }

}
