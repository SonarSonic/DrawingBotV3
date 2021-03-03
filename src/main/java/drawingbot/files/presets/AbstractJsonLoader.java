package drawingbot.files.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractJsonLoader<O extends IJsonData> {

    public final EnumJsonType type;
    public final File configFile;

    public AbstractJsonLoader(EnumJsonType type, String configFile) {
        this.type = type;
        this.configFile = new File(FileUtils.getUserDataDirectory(), configFile);
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
     * updates the presets settings with the ones currently configured
     * @return the preset or null if the settings couldn't be saved
     */
    protected abstract GenericPreset<O> updatePreset(GenericPreset<O> preset);

    /**
     * applies the presets settings
     */
    protected abstract void applyPreset(GenericPreset<O> preset);

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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public final GenericPreset<O> createNewPreset(String presetSubType, String presetName, boolean userCreated) {
        GenericPreset<O> preset = new GenericPreset<>(type, presetSubType, presetName, userCreated);
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
        registerPreset(preset);
        if (preset.userCreated) {
            queueJsonUpdate();
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

    public final GenericPreset<O> tryUpdatePreset(GenericPreset<O> preset) {
        if (preset != null && preset.userCreated) {
            preset = updatePreset(preset);
            if (preset != null) {
                queueJsonUpdate();
                return preset;
            }
        }
        return null;
    }

    public abstract JsonElement toJsonElement(Gson gson,  GenericPreset<?> preset);

    public abstract O fromJsonElement(Gson gson,  GenericPreset<?> preset, JsonElement element);

    public final void queueJsonUpdate() {
        DrawingBotV3.INSTANCE.backgroundService.submit(this::saveToJSON);
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
}
