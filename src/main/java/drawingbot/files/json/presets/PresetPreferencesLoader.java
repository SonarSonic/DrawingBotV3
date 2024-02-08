package drawingbot.files.json.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.files.json.*;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.util.ArrayList;

public class PresetPreferencesLoader extends AbstractConfigLoader<DBPreferences, PresetData> {

    public PresetPreferencesLoader(PresetType presetType) {
        super(presetType, PresetData.class, "config_settings.json");
    }

    @Override
    public DBPreferences getConfig() {
        return DBPreferences.INSTANCE;
    }

    @Override
    public IPresetManager<DBPreferences, PresetData> getDefaultConfigManager() {
        return Register.PRESET_MANAGER_PREFERENCES;
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "config");
    }

    @Override
    public PresetData createDataInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }


    @Override
    public PresetData fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        //LEGACY COMPATIBILITY: in old versions the config data was not wrapped in a "settings" map
        if(preset.version.equals("1")){
            if(element instanceof JsonObject object){
                if(!object.has("settings")){
                    JsonObject newObject = new JsonObject();
                    JsonObject settings = new JsonObject();
                    ArrayList<String> keySet = new ArrayList<>(object.keySet());
                    keySet.forEach(s -> settings.add(s, object.remove(s)));
                    newObject.add("settings", settings);
                    element = newObject;
                }
            }
        }
        return super.fromJsonElement(gson, preset, element);
    }
}
