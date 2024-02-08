package drawingbot.files.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.files.json.adapters.JsonAdapterGenericPreset;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractPresetLoader<DATA> implements IPresetLoader<DATA>, IPresetLoader.Listener<DATA>  {

    public static final String VERSION = "2.0";
    public final Class<DATA> dataType;
    public final PresetType presetType;
    public final File configFile;

    public final LinkedHashMap<Class<?>, IPresetManager<?, DATA>> presetManagers = new LinkedHashMap<>();
    public final ObservableList<GenericPreset<DATA>> presets = FXCollections.observableArrayList();
    public final FilteredList<GenericPreset<DATA>> userPresets = new FilteredList<>(presets, p -> p.userCreated);
    public final ObservableList<String> subTypes = FXCollections.observableArrayList();
    public final HashMap<String, ObservableList<GenericPreset<DATA>>> presetsByType = new LinkedHashMap<>();

    public AbstractPresetLoader(Class<DATA> dataType, PresetType presetType, String configFile) {
        this.presetType = presetType;
        this.dataType = dataType;
        this.configFile = new File(FileUtils.getUserDataDirectory(), configFile);
        addSpecialListener(this);
    }

    @Override
    public String getVersion(){
        return VERSION;
    }

    @Override
    public final PresetType getPresetType(){
        return presetType;
    }

    @Override
    public final Class<DATA> getDataType(){
        return dataType;
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty loading = new SimpleBooleanProperty(true);

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean isLoading) {
        this.loading.set(isLoading);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void logPresetAction(String action, GenericPreset<DATA> preset){
        DrawingBotV3.logger.finest("%s %s: %s".formatted(action, preset.presetType.getDisplayName(), preset.getPresetName()));
    }

    /**
     * INTERNAL USE, creates the sub type if it doesn't already exist
     */
    protected void initSubType(String subType){
        if(subType != null && !subType.isEmpty()){
            if(!subTypes.contains(subType)){
                subTypes.add(subType);
            }
            presetsByType.putIfAbsent(subType, FXCollections.observableArrayList());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addPreset(GenericPreset<DATA> preset) {
        if(preset == null){
            return;
        }
        //Skip duplicate presets during load TODO deal with duplicate names properly??
        if(isLoading() && presets.contains(preset)){
            logPresetAction("Skipping Duplicate", preset);
            return;
        }
        logPresetAction("Adding", preset);

        presets.add(preset);

        if(preset.getPresetSubType() != null && !preset.getPresetSubType().isEmpty()) {
            initSubType(preset.getPresetSubType());
            presetsByType.get(preset.getPresetSubType()).add(preset);
        }else if(!getPresetType().ignoreSubType){
            logPresetAction("Missing sub type", preset);
        }

        if(!isLoading()){
            markDirty();
            sendListenerEvent(listener -> listener.onPresetAdded(preset));
        }
    }

    @Override
    public void removePreset(GenericPreset<DATA> preset) {
        if(preset == null){
            return;
        }
        logPresetAction("Removing", preset);

        //Remove the preset from the global list
        presets.remove(preset);

        //Remove the preset from the sub types list
        if(preset.getPresetSubType() != null && !preset.getPresetSubType().isEmpty()) {
            presetsByType.get(preset.getPresetSubType()).remove(preset);
        }

        if(!isLoading()){
            markDirty();
            sendListenerEvent(listener -> listener.onPresetRemoved(preset));
        }

    }

    @Override
    public GenericPreset<DATA> editPreset(GenericPreset<DATA> oldPreset, GenericPreset<DATA> editPreset){
        if(editPreset == null){
            return oldPreset;
        }

        //If the edited preset was a system default we can't override it so just registered the edited version instead
        if(oldPreset.isSystemPreset()){
            addPreset(editPreset);
            return editPreset;
        }

        logPresetAction("Replacing", oldPreset);
        logPresetAction("Editing", editPreset);

        boolean isMainDefault = oldPreset == getDefaultPreset();
        boolean isSubTypeDefault = presetType.defaultsPerSubType && oldPreset == getDefaultPresetForSubType(oldPreset.getPresetSubType());

        GenericPreset<DATA> result = null;

        if(!oldPreset.getPresetSubType().equals(editPreset.getPresetSubType())){
            //If the presets sub type changes we need to remove / add it back to reflect this change
            removePreset(oldPreset);
            addPreset(editPreset);
            result = editPreset;
        }else{
            //Copy the data from the edit preset to the existing preset, keeping it's name and order
            oldPreset.setPresetName(editPreset.getPresetName());
            oldPreset.setPresetSubType(editPreset.getPresetSubType());
            oldPreset.data = editPreset.data;
            result = oldPreset;
        }

        if(isMainDefault){
            setDefaultPreset(result);
        }
        if(isSubTypeDefault){
            setDefaultPresetSubType(result);
        }

        //Send out listener events and mark the presets as dirty
        sendListenerEvent(listener -> listener.onPresetEdited(editPreset));
        markDirty();
        return result;
    }

    /**
     * @return all registered presets
     */
    @Override
    public final ObservableList<GenericPreset<DATA>> getPresets(){
        return presets;
    }


    @Override
    public final ObservableList<GenericPreset<DATA>> getUserCreatedPresets(){
        return userPresets;
    }

    @Override
    public final ObservableList<String> getPresetSubTypes(){
        return subTypes;
    }

    @Override
    public ObservableList<GenericPreset<DATA>> getPresetsForSubType(String subType){
        return presetsByType.getOrDefault(subType, FXCollections.observableArrayList());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public GenericPreset<DATA> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }

    @Override
    public GenericPreset<DATA> getDefaultPresetForSubType(String subType){
        return MasterRegistry.INSTANCE.getDefaultPresetForSubTypeWithFallback(this, subType, "Default", true);
    }

    @Override
    public void setDefaultPreset(GenericPreset<DATA> preset){
        if(preset == null){
            return;
        }
        DBPreferences.INSTANCE.setDefaultPreset(getPresetType().id, preset.getPresetSubType() + ":" + preset.getPresetName());
    }

    @Override
    public void setDefaultPresetSubType(GenericPreset<DATA> preset){
        if(preset == null){
            return;
        }
        DBPreferences.INSTANCE.setDefaultPreset(getPresetType().id + ":" + preset.getPresetSubType(), preset.getPresetName());
    }

    @Override
    public void resetDefaultPreset(){
        DBPreferences.INSTANCE.clearDefaultPreset(getPresetType().id);
    }

    @Override
    public void resetDefaultPresetSubType(String subType){
        DBPreferences.INSTANCE.clearDefaultPreset(getPresetType().id + ":" + subType);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public GenericPreset<DATA> createNewPreset(){
        return createNewPreset("User", "New Preset", true);
    }

    @Override
    public final GenericPreset<DATA> createNewPreset(String presetSubType, String presetName, boolean userCreated) {
        GenericPreset<DATA> preset = new GenericPreset<>(getVersion(), presetType, presetSubType, presetName, userCreated);
        preset.data = createDataInstance(preset);
        preset.presetLoader = this;
        return preset;
    }

    @Override
    public GenericPreset<DATA> createEditablePreset(GenericPreset<DATA> preset) {
        GenericPreset<DATA> duplicatePreset = new GenericPreset<>(preset);
        duplicatePreset.userCreated = true;
        if(preset.isSystemPreset()){ //don't override system presets
            duplicatePreset.setPresetName(preset.getPresetName() + " - Copy");
        }
        return duplicatePreset;
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty dirty = new SimpleBooleanProperty(false);

    public boolean isDirty() {
        return dirty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        if (!isDirty()) {
            dirty.set(true);

            //mark this preset loader as dirty so updates will be saved on the next tick
            JsonLoaderManager.INSTANCE.markDirty(this);

            //run later to prevent json update happen before services have been created
            sendListenerEvent(Listener::onMarkDirty);
        }
    }


    @Override
    public void updateJSON() {
        if (isDirty()) {
            dirty.set(false);
            DrawingBotV3.INSTANCE.backgroundService.submit(this::saveToJSON);
        }
    }

    @Override
    public void loadFromJSON(){
        PresetContainerJsonFile<DATA> presets = JsonLoaderManager.getOrCreateJSONFile(PresetContainerJsonFile.class, configFile, c -> new PresetContainerJsonFile<DATA>());
        presets.jsonMap.forEach(preset -> {
            if(preset != null && preset.data != null){
                addPreset(preset);
            }
        });
        onJSONLoaded();
        DrawingBotV3.logger.info("Loaded JSON: %s, loaded %s presets".formatted(configFile.getName(), getPresets().size()));
    }

    @Override
    public void saveToJSON(){
        try {
            List<GenericPreset<DATA>> presets = getSaveablePresets();
            Gson gson = JsonLoaderManager.createDefaultGson();
            PresetContainerJsonFile<DATA> presetContainer = new PresetContainerJsonFile<>(presets);
            JsonWriter writer = gson.newJsonWriter(new FileWriter(configFile));
            gson.toJson(presetContainer, PresetContainerJsonFile.class, writer);
            writer.flush();
            writer.close();
            DrawingBotV3.logger.info("Updated JSON: %s, saved %s presets".formatted(configFile.getName(), presets.size()));
        }catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error updating preset json");
        }
    }


    /**
     * called once all jsons have been loaded during the applications init
     */
    protected void onJSONLoaded() {}



    /**
     * called by {@link JsonAdapterGenericPreset}
     * @return a json element to represent the presets data type
     */
    @Override
    public JsonElement toJsonElement(Gson gson, GenericPreset<?> preset){
        return gson.toJsonTree(preset.data, getDataType());
    }

    /**
     * called by {@link JsonAdapterGenericPreset}
     * @return the presets data, from the given json element
     */
    @Override
    public DATA fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element){
        return gson.fromJson(element, getDataType());
    }

    ////////////////////////////////////////////////////////

    @Override
    public void loadDefaults(DBTaskContext context) {
        GenericPreset<DATA> defaultPreset = getDefaultPreset();
        if(defaultPreset != null){
            defaultPreset.applyPreset(context);
        }
    }

    /////////////////////////////////

    private ObservableList<IPresetLoader.Listener<DATA>> listeners = null;

    @Override
    public ObservableList<IPresetLoader.Listener<DATA>> listeners(){
        if(listeners == null){
            listeners = FXCollections.observableArrayList();
        }
        return listeners;
    }

}