package drawingbot.javafx;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.adapters.JsonAdapterGenericPreset;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.INamedSetting;
import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

@JsonAdapter(JsonAdapterGenericPreset.class)
public class GenericPreset<DATA> implements INamedSetting {

    public transient IPresetLoader<DATA> presetLoader; //the preset loader which created this preset

    public PresetType presetType; //preset type, which defines the PresetManager and IPresetData types
    public String version; //the major version of this preset
    public boolean userCreated; //if the preset should be saved to the json, if false it's assumed the preset is pre-installed

    private final SimpleStringProperty presetSubType = new SimpleStringProperty(); //only needed by some presets, e.g. PFMPresets have different presets for each PFM
    private final SimpleStringProperty presetName = new SimpleStringProperty(); //the presets name as it should show up in the user interface

    public DATA data; //data this preset is bound to

    public GenericPreset(){}

    public GenericPreset(String version, PresetType presetType, String presetSubType, String presetName, boolean userCreated){
        this.version = version;
        this.presetType = presetType;
        this.presetSubType.set(presetSubType);
        this.presetName.set(presetName);
        this.userCreated = userCreated;
    }

    public GenericPreset(GenericPreset<DATA> copy){
        copyData(copy);
    }

    public void copyData(GenericPreset<DATA> copy){
        this.presetLoader = copy.presetLoader;
        this.version = copy.version;
        this.presetType = copy.presetType;
        this.presetSubType.set(copy.getPresetSubType());
        this.presetName.set(copy.getPresetName());
        this.userCreated = copy.userCreated;
        this.data = copy.presetLoader.duplicateData(JsonLoaderManager.createDefaultGson(), copy);
    }

    public void applyPreset(DBTaskContext context){
        MasterRegistry.INSTANCE.applyPresetToProject(context, this, false, false);
    }

    public void updatePreset(DBTaskContext context){
        MasterRegistry.INSTANCE.updatePresetFromProject(context, this, false, false);
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

    public boolean isSystemPreset(){
        return presetLoader.isSystemPreset(this);
    }

    public boolean isUserPreset(){
        return presetLoader.isUserPreset(this);
    }

    @Override
    public String toString() {
        return getPresetName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GenericPreset<?> other){
            return Objects.equals(presetType, other.presetType)
                    && Objects.equals(version, other.version)
                    && Objects.equals(userCreated, other.userCreated)
                    && Objects.equals(getPresetName(), other.getPresetName())
                    && Objects.equals(getPresetSubType(), other.getPresetSubType())
                    && Objects.equals(data, other.data);
        }
        return super.equals(obj);
    }
}
