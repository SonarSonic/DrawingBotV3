package drawingbot.files.presets.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.files.presets.IJsonData;
import drawingbot.utils.EnumJsonType;

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
    public EnumJsonType getJsonType() {
        return EnumJsonType.PFM_PRESET;
    }
}
