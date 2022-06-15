package drawingbot.files.json.projects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Legacy Project Loader
 */
public class PresetProjectSettingsLoader extends AbstractPresetLoader<PresetProjectSettings> {

    public PresetProjectSettingsLoader(PresetType presetType) {
        super(PresetProjectSettings.class, presetType, "projects.json");
        setDefaultManager(new PresetProjectSettingsManager(this));
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public PresetProjectSettings getPresetInstance(GenericPreset<PresetProjectSettings> preset) {
        if(preset.version.equals("1")){
            return new PresetProjectSettingsLegacy();
        }
        return new PresetProjectSettings();
    }

    @Override
    public void registerPreset(GenericPreset<PresetProjectSettings> preset) {}

    @Override
    public void unregisterPreset(GenericPreset<PresetProjectSettings> preset) {}

    @Override
    public GenericPreset<PresetProjectSettings> getDefaultPreset() {
        return null;
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        return new ArrayList<>();
    }

    @Override
    public Collection<GenericPreset<PresetProjectSettings>> getAllPresets() {
        return new ArrayList<>();
    }

    @Override
    public PresetProjectSettings fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        if(preset.version.equals("1")){
            return gson.fromJson(element, PresetProjectSettingsLegacy.class);
        }
        return gson.fromJson(element, PresetProjectSettings.class);
    }
}
