package drawingbot.pfm;

import java.util.HashMap;
import java.util.Map;

public class PFMSettingsPreset {

    //key = PFMSettings UUID & value = the value to set the PFM too
    public String presetName = "Preset";
    public HashMap<String, Object> settings = new HashMap<>();


    public void loadPreset(){
        for(Map.Entry<String, Object> entry : settings.entrySet()){
            //TODO
        }
    }

}
