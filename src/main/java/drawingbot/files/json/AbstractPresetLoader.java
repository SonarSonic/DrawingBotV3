package drawingbot.files.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.files.json.adapters.JsonAdapterGenericPreset;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class AbstractPresetLoader<DATA> implements IPresetLoader<DATA>, IPresetLoader.Listener<DATA>  {

    public static final String VERSION = "2.0";
    public final Class<DATA> dataType;
    public final PresetType presetType;
    public final File configFile;

    public final LinkedHashMap<Class<?>, IPresetManager<?, DATA>> presetManagers = new LinkedHashMap<>();
    public final ObservableList<GenericPreset<DATA>> presets = FXCollections.observableArrayList();
    public final ObservableList<GenericPreset<DATA>> overriddenSystemPresets = FXCollections.observableArrayList();
    public final FilteredList<GenericPreset<DATA>> userPresets = new FilteredList<>(presets, p -> p.userCreated);
    public final ObservableList<String> subTypes = FXCollections.observableArrayList();
    public final HashMap<String, ObservableList<GenericPreset<DATA>>> presetsByType = new LinkedHashMap<>();

    public final ObjectProperty<GenericPreset<DATA>> defaultPreset = new SimpleObjectProperty<>();
    public final ObservableMap<String, GenericPreset<DATA>> defaultSubTypePreset = FXCollections.observableHashMap();

    public AbstractPresetLoader(Class<DATA> dataType, PresetType presetType, String configFile) {
        this.presetType = presetType;
        this.dataType = dataType;
        this.configFile = new File(FileUtils.getUserDataDirectory(), configFile);
        this.defaultPreset.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.setDefaultPreset(false);
            }
            if(newValue != null){
                newValue.setDefaultPreset(true);
            }
        });
        this.defaultSubTypePreset.addListener((MapChangeListener<String, GenericPreset<DATA>>) change -> {
            if (change.getValueRemoved() != null) {
                change.getValueRemoved().setDefaultSubTypePreset(false);
            }
            if (change.getValueAdded() != null) {
                change.getValueAdded().setDefaultSubTypePreset(true);
            }
        });

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

    protected void logPresetAction(Level level, String action, GenericPreset<DATA> preset){
        DrawingBotV3.logger.log(level, "%s %s: %s:%s".formatted(action, preset.presetType.getDisplayName(), preset.getPresetSubType(), preset.getPresetName()));
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

    protected boolean isUniqueName(GenericPreset<?> preset){
        return presets.stream().noneMatch(p -> p.getPresetName().equals(preset.getPresetName()) && p.getPresetType().equals(preset.getPresetType()) && p.getPresetSubType().equals(preset.getPresetSubType()));
    }

    protected boolean isUniqueName(GenericPreset<?> preset, String newName){
        return presets.stream().noneMatch(p -> p.getPresetName().equals(newName) && p.getPresetType().equals(preset.getPresetType()) && p.getPresetSubType().equals(preset.getPresetSubType()));
    }

    protected GenericPreset<DATA> getOverriddenSystemPreset(GenericPreset<?> preset){
        return presets.stream().filter(p -> (p.isSystemPreset() || (!isLoading() && p.overridesSystemPreset)) && p.getPresetName().equals(preset.getPresetName()) && p.getPresetType().equals(preset.getPresetType()) && p.getPresetSubType().equals(preset.getPresetSubType())).findFirst().orElse(null);
    }

    protected GenericPreset<DATA> getRestoreSystemPreset(GenericPreset<?> preset){
        return overriddenSystemPresets.stream().filter(p -> p.getPresetName().equals(preset.getPresetName()) && p.getPresetType().equals(preset.getPresetType()) && p.getPresetSubType().equals(preset.getPresetSubType())).findFirst().orElse(null);
    }

    protected void tryRestoreSystemPresets(){
        List<GenericPreset<DATA>> restore = overriddenSystemPresets.stream().filter(this::isUniqueName).collect(Collectors.toList());
        restore.forEach(preset -> {
            addPreset(preset);
            overriddenSystemPresets.remove(preset);
        });
    }

    protected void enforceUniqueName(GenericPreset<?> preset){
        String uniqueName = Utils.uniqueName(preset.getPresetName(), n -> isUniqueName(preset, n));
        preset.setPresetName(uniqueName);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addPreset(GenericPreset<DATA> preset) {
        if(preset == null){
            return;
        }
        GenericPreset<DATA> replacePreset = null;

        //Enforce unique preset names
        if(!isUniqueName(preset)){
            //If we can find an exact match, don't load the preset
            if(presets.contains(preset)){
                logPresetAction(Level.WARNING, "Skipping Identical Preset", preset);
                return;
            }
            if(preset.isSystemPreset()){
                logPresetAction(Level.WARNING,"Skipping Duplicate System Preset", preset);
            }else{
                replacePreset = getOverriddenSystemPreset(preset);
                if(replacePreset != null){
                    //If the preset is the same as a system preset we can override it
                    preset.overridesSystemPreset = true;
                    overriddenSystemPresets.add(replacePreset);
                    logPresetAction(Level.WARNING,"Overriding System Preset with User Preset", preset);
                    markDirty();
                }else{
                    //If the preset isn't an exact match for an existing one keep it and rename it
                    String oldName = preset.getPresetName();
                    enforceUniqueName(preset);
                    logPresetAction(Level.WARNING, "Renamed Duplicate User Preset: %s -> %s".formatted(oldName, preset.getPresetName()), preset);
                    markDirty();
                }
            }
        }

        if(!isLoading()){
            logPresetAction(Level.INFO,"Adding", preset);
        }

        if(replacePreset != null){
            presets.set(presets.indexOf(replacePreset), preset);
        }else{
            presets.add(preset);
        }

        if(preset.getPresetSubType() != null && !preset.getPresetSubType().isEmpty()) {
            initSubType(preset.getPresetSubType());

            List<GenericPreset<DATA>> presetsForType = presetsByType.get(preset.getPresetSubType());
            if(replacePreset != null){
                presetsForType.set(presetsForType.indexOf(replacePreset), preset);
            }else{
                presetsForType.add(preset);
            }

        }else if(getPresetType().getSubTypeBehaviour() != PresetType.SubTypeBehaviour.IGNORED){
            logPresetAction(Level.WARNING,"Missing sub type", preset);
        }

        if(!isLoading()){
            markDirty();
            Platform.runLater(() -> sendListenerEvent(listener -> listener.onPresetAdded(preset)));
        }
    }

    @Override
    public void removePreset(GenericPreset<DATA> preset) {
        if(preset == null){
            return;
        }
        logPresetAction(Level.INFO,"Removing", preset);

        GenericPreset<DATA> restorePreset = getRestoreSystemPreset(preset);

        //Remove the preset from the global list
        if(restorePreset != null){
            presets.set(presets.indexOf(preset), restorePreset);
        }else{
            presets.remove(preset);
        }

        //Remove the preset from the sub types list
        if(preset.getPresetSubType() != null && !preset.getPresetSubType().isEmpty()) {
            List<GenericPreset<DATA>> presetsForType = presetsByType.get(preset.getPresetSubType());
            if(restorePreset != null){
                presetsForType.set(presetsForType.indexOf(preset), restorePreset);
            }else{
                presetsForType.remove(preset);
            }
        }

        if(!isLoading()){
            markDirty();
            Platform.runLater(() -> sendListenerEvent(listener -> listener.onPresetRemoved(preset)));
        }

    }

    @Override
    public boolean reorderPreset(GenericPreset<?> preset, List<GenericPreset<?>> displayedList, int shift) {
        int index = displayedList.indexOf(preset);
        int newIndex = Utils.clamp(index + shift, 0, displayedList.size()-1);

        //if the preset is at the start or end of the displayedList don't move it in the global lists
        if(index == newIndex){
            return false;
        }

        //find the position of the preset which comes before in the list, the displayed list must therefore respect the order
        GenericPreset<?> shiftPreset = displayedList.get(newIndex);

        //check the preset is actually loaded by this IPresetLoader
        if(!canLoadPreset(preset) || !canLoadPreset(shiftPreset)){
            return false;
        }

        int oldGlobalIndex = presets.indexOf(preset);
        int newGlobalIndex = presets.indexOf(shiftPreset);

        presets.remove(preset);
        presets.add(newGlobalIndex < oldGlobalIndex ? newGlobalIndex : newGlobalIndex, cast(preset));

        //Recreate the sub type list, to match the new order of the presets
        if(preset.getPresetSubType() != null && !preset.getPresetSubType().isEmpty()) {
            List<GenericPreset<DATA>> subTypePresets = presetsByType.get(preset.getPresetSubType());
            subTypePresets.clear();
            presets.stream().filter(p -> p.getPresetSubType().equals(preset.getPresetSubType())).forEach(subTypePresets::add);

        }
        markDirty();
        return true;
    }

    @Override
    public GenericPreset<DATA> editPreset(GenericPreset<DATA> oldPreset, GenericPreset<DATA> editPreset){
        if(editPreset == null){
            return oldPreset;
        }

        //If the edited preset was a system default try to override it
        if(oldPreset.isSystemPreset()){
            editPreset.overridesSystemPreset = true;
            addPreset(editPreset);
            return editPreset;
        }

        logPresetAction(Level.INFO,"Replacing", oldPreset);
        logPresetAction(Level.INFO,"Editing", editPreset);

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
        markDirty();
        Platform.runLater(() -> sendListenerEvent(listener -> listener.onPresetEdited(editPreset)));
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
        DBPreferences.INSTANCE.setDefaultPreset(getPresetType(), preset);
        updateDefaultPresetProperties();
    }

    @Override
    public void setDefaultPresetSubType(GenericPreset<DATA> preset){
        if(preset == null){
            return;
        }
        DBPreferences.INSTANCE.setDefaultPresetSubType(getPresetType(), preset);
        updateDefaultPresetProperties();
    }

    @Override
    public void resetDefaultPreset(){
        DBPreferences.INSTANCE.clearDefaultPreset(getPresetType());
        updateDefaultPresetProperties();
    }

    @Override
    public void resetDefaultPresetSubType(String subType){
        DBPreferences.INSTANCE.clearDefaultPresetSubType(getPresetType(), subType);
        updateDefaultPresetProperties();
    }

    public void updateDefaultPresetProperties(){
        this.defaultPreset.set(getDefaultPreset());
        this.subTypes.forEach(type -> defaultSubTypePreset.put(type, getDefaultPresetForSubType(type)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public GenericPreset<DATA> createNewPreset(){
        String subType = getPresetType().getSubTypeBehaviour().isFixed() ? getDefaultPreset().getPresetSubType() : "User";
        return createNewPreset(subType, "New Preset", true);
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
        enforceUniqueName(preset);
        return duplicatePreset;
    }

    @Override
    public GenericPreset<DATA> createOverridePreset(GenericPreset<DATA> systemPreset) {
        GenericPreset<DATA> duplicatePreset = new GenericPreset<>(systemPreset);
        duplicatePreset.userCreated = true;
        duplicatePreset.overridesSystemPreset = true;
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

            updateDefaultPresetProperties();

            //mark this preset loader as dirty so updates will be saved on the next tick
            JsonLoaderManager.INSTANCE.markDirty(this);

            //run later to prevent json update happen before services have been created
            Platform.runLater(() -> sendListenerEvent(Listener::onMarkDirty));
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
    public void loadFromJSON() {
        PresetLoaderDataFile dataFile = JsonLoaderManager.getOrCreateJSONFile(PresetLoaderDataFile.class, configFile, c -> new PresetLoaderDataFile());
        dataFile.jsonMap.forEach(preset -> {
            if (preset != null && preset.data != null && canLoadPreset(preset)) {
                preset.userCreated = true;
                addPreset(cast(preset));
            }
        });

        onJSONLoaded();

        //Sort the presets according to the saved order
        if (!dataFile.presetOrder.isEmpty()) {
            Comparator<GenericPreset<DATA>> comparator = Comparator.comparingInt(p -> {
                int index = dataFile.presetOrder.indexOf(p.getPresetID());
                if (index == -1) {
                    //If the preset is new or hasn't been sorted, or is a new System Preset sort it by it's place in the loaded list
                    return presets.indexOf(p);
                }
                return index;
            });
            presets.sort(comparator);
            presetsByType.values().forEach(list -> list.sort(comparator));
        }

        //Hide system presets
        if(!dataFile.hiddenSystemPresets.isEmpty()){
            dataFile.hiddenSystemPresets.forEach(presetID -> {
                GenericPreset<?> preset = findPresetFromID(presetID);
                if(preset != null && preset.isSystemPreset()){
                    preset.setEnabled(false);
                }
            });
        }
    }

    @Override
    public void saveToJSON(){
        try {
            Gson gson = JsonLoaderManager.createDefaultGson();
            PresetLoaderDataFile dateFile = new PresetLoaderDataFile();
            dateFile.jsonMap = (List<GenericPreset<?>>)(Object)getSaveablePresets();
            dateFile.presetOrder = getPresets().stream().map(GenericPreset::getPresetID).collect(Collectors.toList());
            dateFile.hiddenSystemPresets = getPresets().stream().filter(f -> f.isSystemPreset() && !f.isEnabled()).map(GenericPreset::getPresetID).collect(Collectors.toList());

            JsonWriter writer = gson.newJsonWriter(new FileWriter(configFile));
            gson.toJson(dateFile, PresetLoaderDataFile.class, writer);
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
    protected void onJSONLoaded() {
        updateDefaultPresetProperties();
    }



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