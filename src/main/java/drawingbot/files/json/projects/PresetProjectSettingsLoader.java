package drawingbot.files.json.projects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;

public class PresetProjectSettingsLoader extends AbstractPresetLoader<PresetProjectSettings> {

    public PresetProjectSettingsLoader(PresetType presetType) {
        super(PresetProjectSettings.class, presetType, "projects.json");
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public PresetProjectSettings createDataInstance(GenericPreset<PresetProjectSettings> preset) {
        if(preset.version.equals("1")){
            return new PresetProjectSettingsLegacy();
        }
        return new PresetProjectSettings();
    }

    @Override
    public void addPreset(GenericPreset<PresetProjectSettings> preset) {}

    @Override
    public void removePreset(GenericPreset<PresetProjectSettings> preset) {}

    @Override
    public GenericPreset<PresetProjectSettings> getDefaultPreset() {
        return null;
    }

    @Override
    public PresetProjectSettings fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        if(preset.version.equals("1")){
            return gson.fromJson(element, PresetProjectSettingsLegacy.class);
        }
        return gson.fromJson(element, PresetProjectSettings.class);
    }
}
