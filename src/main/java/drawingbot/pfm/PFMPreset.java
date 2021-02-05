package drawingbot.pfm;

import drawingbot.utils.GenericSetting;

import java.util.HashMap;
import java.util.List;

public class PFMPreset {

    public String pfmName;
    public String presetName;
    public HashMap<String, String> settings;
    public boolean userCreated; //should it be saved to JSON.

    public PFMPreset(){}

    public PFMPreset(String pfmName, String presetName, boolean userCreated){
        this.pfmName = pfmName;
        this.presetName = presetName;
        this.settings = new HashMap<>();
        this.userCreated = userCreated;
    }

    public void savePreset(List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            if(setting.value.get() != setting.defaultValue){
                settings.put(toSafeName(setting.settingName.getValue()), setting.getValueAsString());
            }
        }
    }

    public void loadPreset(List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            String obj = settings.get(toSafeName(setting.settingName.getValue()));
            if(obj != null){
                setting.setValueFromString(obj);
            }else{
                setting.resetSetting();
            }
        }
    }

    public <V> PFMPreset addSetting(GenericSetting<?, V> setting, V obj){
        settings.put(toSafeName(setting.settingName.getValue()), setting.stringConverter.toString(obj));
        return this;
    }

    public String toSafeName(String name){
        return name.replace(' ', '_').toLowerCase();
    }

    @Override
    public String toString() {
        return presetName;
    }
}
