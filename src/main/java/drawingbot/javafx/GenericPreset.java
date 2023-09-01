package drawingbot.javafx;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.adapters.JsonAdapterGenericPreset;
import drawingbot.utils.INamedSetting;
import javafx.beans.property.SimpleStringProperty;

@JsonAdapter(JsonAdapterGenericPreset.class)
public class GenericPreset<O> implements INamedSetting {

    public transient AbstractJsonLoader<O> presetLoader; //the preset loader which created this preset

    public PresetType presetType; //preset type, which defines the PresetManager and IPresetData types
    public String version; //the major version of this preset
    public boolean userCreated; //if the preset should be saved to the json, if false it's assumed the preset is pre-installed

    private final SimpleStringProperty presetSubType = new SimpleStringProperty(); //only needed by some presets, e.g. PFMPresets have different presets for each PFM
    private final SimpleStringProperty presetName = new SimpleStringProperty(); //the presets name as it should show up in the user interface

    public O data; //data this preset is bound to

    public GenericPreset(){}

    public GenericPreset(String version, PresetType presetType, String presetSubType, String presetName, boolean userCreated){
        this.version = version;
        this.presetType = presetType;
        this.presetSubType.set(presetSubType);
        this.presetName.set(presetName);
        this.userCreated = userCreated;
    }

    public GenericPreset(GenericPreset<O> copy){
        copyData(copy);
    }

    public void copyData(GenericPreset<O> copy){
        this.presetLoader = copy.presetLoader;
        this.version = copy.version;
        this.presetType = copy.presetType;
        this.presetSubType.set(copy.getPresetSubType());
        this.presetName.set(copy.getPresetName());
        this.userCreated = copy.userCreated;
        this.data = copy.presetLoader.duplicateData(JsonLoaderManager.createDefaultGson(), copy);
    }

    public String getPresetSubType() {
        return presetSubType.get();
    }

    public SimpleStringProperty presetSubTypeProperty() {
        return presetSubType;
    }

    public void setPresetSubType(String presetSubType) {
        this.presetSubType.set(presetSubType);
    }

    public String getPresetName() {
        return presetName.get();
    }

    public SimpleStringProperty presetNameProperty() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName.set(presetName);
    }

    @Override
    public String toString() {
        return getPresetName();
    }
}
