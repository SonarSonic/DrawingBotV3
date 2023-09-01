package drawingbot.files.json.projects;

import com.google.gson.JsonElement;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.PresetData;

import java.util.HashMap;

@JsonData
public class PresetProjectSettings extends PresetData {

    public String name = "";
    public String imagePath = "";
    public String notes = "";
    public String timeStamp = "";
    public String thumbnailID = "";
    public double rating = 0D;

    public transient boolean isSubProject = false;

    public PresetProjectSettings(){
        super();
    }

    public PresetProjectSettings(HashMap<String, JsonElement> settingList){
        super(settingList);
    }

}
