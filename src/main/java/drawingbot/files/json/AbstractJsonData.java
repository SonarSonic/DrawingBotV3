package drawingbot.files.json;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

@JsonData
public abstract class AbstractJsonData implements IJsonData {

    @SerializedName(value = "settings", alternate = "settingList")
    public HashMap<String, JsonElement> settings;

    public AbstractJsonData(){
        settings = new HashMap<>();
    }

    public AbstractJsonData(HashMap<String, JsonElement> settings){
        this.settings = settings;
    }

}
