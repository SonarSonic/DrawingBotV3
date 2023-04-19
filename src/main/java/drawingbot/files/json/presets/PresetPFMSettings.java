package drawingbot.files.json.presets;

import com.google.gson.JsonElement;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.IJsonData;
import drawingbot.registry.Register;

import java.util.HashMap;

@JsonData
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
