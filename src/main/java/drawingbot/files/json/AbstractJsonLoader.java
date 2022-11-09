package drawingbot.files.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.javafx.GenericPreset;
import javafx.application.Platform;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractJsonLoader<O extends IJsonData> {

    public final PresetType type;
    public final File configFile;

    public AbstractJsonLoader(PresetType type, String configFile) {
        this.type = type;
        this.configFile = new File(FileUtils.getUserDataDirectory(), configFile);
    }

    public boolean canLoadPreset(GenericPreset<?> preset){
        return preset.presetType == type;
    }

    public String getVersion(){
        return "1.1";
    }

    protected abstract O getPresetInstance(GenericPreset<O> preset);

    /**
     * registers the preset, called when a preset is created or loaded from a json
     */
    protected abstract void registerPreset(GenericPreset<O> preset);

    /**
     * unregisters the preset, called when a preset is deleted
     */
    protected abstract void unregisterPreset(GenericPreset<O> preset);


    /**
     * called when a presets subtype / name is changed, used for updating the name in the data object if needed
     */
    protected void onPresetEdited(GenericPreset<O> preset) {}

    /**
     * called once all jsons have been loaded during the applications init
     */
    protected void onJSONLoaded() {}

    /**
     * @return all the presets created by users that are currently registered, to be saved to the json
     */
    public abstract List<GenericPreset<?>> getUserCreatedPresets();


    /**
     * @return all registered presets
     */
    public Collection<GenericPreset<O>> getAllPresets(){
        return new ArrayList<>();
    }

    public List<String> getPresetSubTypes(){
        return new ArrayList<>();
    }

    public List<GenericPreset<O>> getPresetsForSubType(String subType){
        return new ArrayList<>();
    }

    public GenericPreset<O> getDefaultPresetForSubType(String subType){
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private AbstractPresetManager<O> defaultManager = null;

    public AbstractPresetManager<O> getDefaultManager(){
        return defaultManager;
    }

    public void setDefaultManager(AbstractPresetManager<O> defaultManager) {
        this.defaultManager = defaultManager;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public final GenericPreset<O> createNewPreset(String presetSubType, String presetName, boolean userCreated) {
        GenericPreset<O> preset = new GenericPreset<>(getVersion(), type, presetSubType, presetName, userCreated);
        preset.data = getPresetInstance(preset);
        return preset;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public final void tryRegisterPreset(GenericPreset<O> preset) {
        if (preset != null && preset.data != null) {
            registerPreset(preset);
        }
    }

    public final void trySavePreset(GenericPreset<O> preset) {
        if (preset != null){
            registerPreset(preset);
            if (preset.userCreated) {
                queueJsonUpdate();
            }
        }
    }

    public final void tryEditPreset(GenericPreset<O> preset) {
        if (preset != null && preset.userCreated) {
            onPresetEdited(preset);
            queueJsonUpdate();
        }
    }

    public final boolean tryDeletePreset(GenericPreset<O> preset) {
        if (preset != null && preset.userCreated) {
            unregisterPreset(preset);
            queueJsonUpdate();
            return true;
        }
        return false;
    }

    public abstract JsonElement toJsonElement(Gson gson, GenericPreset<?> preset);

    public abstract O fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element);

    public final void queueJsonUpdate() {
        //run later to prevent json update happen before services have been created
        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.backgroundService.submit(this::saveToJSON);
        });
    }

    public void loadFromJSON(){
        PresetContainerJsonFile<O> presets = JsonLoaderManager.getOrCreateJSONFile(PresetContainerJsonFile.class, configFile, c -> new PresetContainerJsonFile<O>());
        presets.jsonMap.forEach(this::tryRegisterPreset);
        onJSONLoaded();
    }

    public void saveToJSON(){
        try {
            Gson gson = JsonLoaderManager.createDefaultGson();
            PresetContainerJsonFile<O> userPFMPresets = new PresetContainerJsonFile(getUserCreatedPresets());
            JsonWriter writer = gson.newJsonWriter(new FileWriter(configFile));
            gson.toJson(userPFMPresets, PresetContainerJsonFile.class, writer);
            writer.flush();
            writer.close();
        }catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error updating preset json");
        }
    }

    public void loadDefaults(){

    }

}