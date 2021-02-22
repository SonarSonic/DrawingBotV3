package drawingbot.files.presets.types;

import drawingbot.files.presets.IJsonData;
import drawingbot.utils.EnumJsonType;

import java.util.HashMap;

public class PresetPFMSettings implements IJsonData {

    public HashMap<String, String> settingList;

    public PresetPFMSettings(){
        settingList = new HashMap<>();
    }

    public PresetPFMSettings(HashMap<String, String> settingList){
        this.settingList = settingList;
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.PFM_PRESET;
    }
}
