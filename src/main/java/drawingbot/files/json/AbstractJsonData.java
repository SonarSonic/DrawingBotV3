package drawingbot.files.json;

import com.google.gson.JsonElement;
import drawingbot.files.json.IJsonData;

import java.util.HashMap;

public abstract class AbstractJsonData implements IJsonData {

    public HashMap<String, JsonElement> settingList;

    public AbstractJsonData(){
        settingList = new HashMap<>();
    }

    public AbstractJsonData(HashMap<String, JsonElement> settingList){
        this.settingList = settingList;
    }

}
