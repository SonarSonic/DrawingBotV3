package drawingbot.files.presets.types;

import com.google.gson.JsonElement;
import drawingbot.files.presets.IJsonData;
import drawingbot.files.presets.PresetType;
import drawingbot.registry.Register;

import java.util.HashMap;

public class PresetPFMSettings implements IJsonData {

    public HashMap<String, JsonElement> settingList;

    public PresetPFMSettings(){
        settingList = new HashMap<>();
    }

    public PresetPFMSettings(HashMap<String, JsonElement> settingList){
        this.settingList = settingList;
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_PFM;
    }
}
