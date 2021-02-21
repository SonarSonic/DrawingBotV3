package drawingbot.utils;

import com.google.gson.JsonObject;
import drawingbot.files.GsonExclude;

public class GenericPreset {

    public EnumPresetType presetType;
    public String presetSubType; //only needed by some presets, e.g. PFMPresets have different presets for each PFM
    public String presetName; //the presets name as it should show up in the user interface
    public boolean userCreated; //if the preset should be saved to the json, if false it's assumed the preset is pre-installed

    public JsonObject jsonObject; //the json object the settings are stored in

    @GsonExclude
    public Object binding; //optional object this preset is bound to

    public GenericPreset(){}

    public GenericPreset(EnumPresetType presetType, String presetSubType, String presetName, boolean userCreated, JsonObject jsonObject){
        this.presetType = presetType;
        this.presetSubType = presetSubType;
        this.presetName = presetName;
        this.userCreated = userCreated;
        this.jsonObject = jsonObject;
    }

    @Override
    public String toString() {
        return presetName;
    }
}
