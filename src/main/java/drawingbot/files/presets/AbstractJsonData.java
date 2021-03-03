package drawingbot.files.presets;

import java.util.HashMap;

public abstract class AbstractJsonData implements IJsonData {

    public HashMap<String, String> settingList;

    public AbstractJsonData(){
        settingList = new HashMap<>();
    }

    public AbstractJsonData(HashMap<String, String> settingList){
        this.settingList = settingList;
    }

}
