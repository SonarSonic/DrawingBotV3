package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
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

    public abstract Property<PFMFactory<?>> pfmProperty(DBTaskContext context);

    public abstract Property<ObservableList<GenericSetting<?, ?>>> settingProperty(DBTaskContext context);

    @Override
    public GenericPreset<PresetPFMSettings> updatePreset(DBTaskContext context, GenericPreset<PresetPFMSettings> preset) {
        PFMFactory<?> pfm = pfmProperty(context).getValue();
        ObservableList<GenericSetting<?, ?>> settings = settingProperty(context).getValue();
        if(pfm != null && settings != null) {
            preset.presetSubType = pfm.getName();
            preset.data.settingList = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<PresetPFMSettings> preset) {
        pfmProperty(context).setValue(MasterRegistry.INSTANCE.getPFMFactory(preset.presetSubType));
        Property<ObservableList<GenericSetting<?, ?>>> settings = settingProperty(context);
        GenericSetting.applySettings(preset.data.settingList, settings.getValue());
    }

}
