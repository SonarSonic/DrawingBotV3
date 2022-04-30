package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;

import java.util.*;

public class PresetProjectSettingsLoader extends AbstractPresetLoader<PresetProjectSettings> {

    public PresetProjectSettingsLoader(PresetType presetType) {
        super(PresetProjectSettings.class, presetType, "projects.json");
        setDefaultManager(new PresetProjectSettingsManager(this));
    }

    @Override
    public PresetProjectSettings getPresetInstance(GenericPreset<PresetProjectSettings> preset) {
        return new PresetProjectSettings();
    }

    @Override
    public void registerPreset(GenericPreset<PresetProjectSettings> preset) {
        //MasterRegistry.INSTANCE.registerPFMPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetProjectSettings> preset) {
        //MasterRegistry.INSTANCE.pfmPresets.get(preset.presetSubType).remove(preset);
    }

    @Override
    public GenericPreset<PresetProjectSettings> getDefaultPreset() {
        return null;
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        /*

        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetProjectSettings>>> entry : MasterRegistry.INSTANCE.pfmPresets.entrySet()) {
            for (GenericPreset<PresetProjectSettings> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
         */
        return new ArrayList<>();
    }

    @Override
    public Collection<GenericPreset<PresetProjectSettings>> getAllPresets() {
        return new ArrayList<>();
    }
}
