package drawingbot.javafx;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.json.IJsonData;
import drawingbot.files.json.adapters.JsonAdapterGenericPreset;
import drawingbot.files.json.PresetType;
import drawingbot.utils.INamedSetting;

@JsonAdapter(JsonAdapterGenericPreset.class)
public class GenericPreset<O extends IJsonData> implements INamedSetting {

    public PresetType presetType; //preset type, which defines the PresetManager and IPresetData types
    public String version; //the major version of this preset
    public String presetSubType; //only needed by some presets, e.g. PFMPresets have different presets for each PFM
    public String presetName; //the presets name as it should show up in the user interface
    public boolean userCreated; //if the preset should be saved to the json, if false it's assumed the preset is pre-installed

    public O data; //data this preset is bound to

    public GenericPreset(){}

    public GenericPreset(String version, PresetType presetType, String presetSubType, String presetName, boolean userCreated){
        this.version = version;
        this.presetType = presetType;
        this.presetSubType = presetSubType;
        this.presetName = presetName;
        this.userCreated = userCreated;
    }

    @Override
    public String toString() {
        return presetName;
    }
}
