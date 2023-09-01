package drawingbot.files.json.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.files.json.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettingsManager;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;

import java.util.*;
import java.util.function.Supplier;

/**
 * Design principles of "Configs" - configs are able to change settings before the DBV3.INSTANCE has even been created.
 * Therefore any references to the INSTANCE will crash, so context should be passed as null, the config shouldn't require "context"
 */
public class ConfigJsonLoader extends AbstractJsonLoader<PresetData> {

    public HashMap<String, IPresetManager<PresetData>> presetManagers = new HashMap<>();
    public HashMap<String, Supplier<? extends PresetData>> presetFactories = new HashMap<>();
    public HashMap<String, Class<? extends PresetData>> presetClass = new HashMap<>();
    public HashMap<String, GenericPreset<PresetData>> configs = new HashMap<>();
    public boolean loading;

    public ConfigJsonLoader(PresetType presetType) {
        super(presetType, "config_settings.json");
        setDefaultManager(new AbstractPresetManager<>(this) {
            @Override
            public GenericPreset<PresetData> updatePreset(DBTaskContext context, GenericPreset<PresetData> preset, boolean loadingProject) {
                IPresetManager<PresetData> manager = presetManagers.get(preset.getPresetSubType());
                if(manager != null){
                    manager.updatePreset(null, preset, false);
                }
                return preset;
            }

            @Override
            public void applyPreset(DBTaskContext context, GenericPreset<PresetData> preset, boolean loadingProject) {
                IPresetManager<PresetData> manager = presetManagers.get(preset.getPresetSubType());
                if(manager != null){
                    manager.applyPreset(null, preset, false);
                }
            }
        });
    }

    @Override
    public void init(){
        registerConfigFactory(PresetData.class, new DefaultPresetManager<PresetData, DBPreferences>(this) {

            @Override
            public void registerDataLoaders() {
                registerSettings(MasterRegistry.INSTANCE.applicationSettings);
                registerPresetDataLoader(new PresetDataLoader.DataInstance<>(PresetData.class, "ui_state", PresetProjectSettingsManager.UIGlobalState.class, PresetProjectSettingsManager.UIGlobalState::new, 0){

                    @Override
                    public void loadData(DBTaskContext context, PresetProjectSettingsManager.UIGlobalState data, GenericPreset<PresetData> preset) {
                        FXHelper.loadUIStates(data.nodes);
                    }

                    @Override
                    public void saveData(DBTaskContext context, PresetProjectSettingsManager.UIGlobalState data, GenericPreset<PresetData> preset) {
                        FXHelper.saveUIStates(data.nodes);
                    }

                    @Override
                    public boolean isEnabled() {
                        return DBPreferences.INSTANCE.restoreLayout.get();
                    }
                });
            }

            @Override
            public DBPreferences getInstance(DBTaskContext context) {
                return DBPreferences.INSTANCE;
            }
        }, "application_settings", PresetData::new, false);
    }

    private void registerConfigFactory(Class<? extends PresetData> clazz, IPresetManager<PresetData> presetManager, String name, Supplier<PresetData> create, boolean isHidden){
        presetManagers.put(name, presetManager);
        presetFactories.put(name, create);
        presetClass.put(name, clazz);
    }

    @Override
    protected void onJSONLoaded() {
        super.onJSONLoaded();
        loading = true;
        //if a preset currently exists, load it, if it doesn't create one
        for(Map.Entry<String, IPresetManager<PresetData>> managerEntry : presetManagers.entrySet()){
            GenericPreset<PresetData> preset = configs.get(managerEntry.getKey());
            if(preset == null){
                preset = createNewPreset(managerEntry.getKey(), "config", false);
                getDefaultManager().updatePreset(null, preset, false);
                registerPreset(preset);
            }else{
                getDefaultManager().applyPreset(null, preset, false);
            }
        }
        loading = false;
    }

    @Override
    protected PresetData getPresetInstance(GenericPreset<PresetData> preset) {
        return presetFactories.get(preset.getPresetSubType()).get();
    }

    @Override
    protected void registerPreset(GenericPreset<PresetData> preset) {
        configs.put(preset.getPresetSubType(), preset);
    }

    @Override
    protected void unregisterPreset(GenericPreset<PresetData> preset) {
        configs.remove(preset.getPresetSubType());
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        return List.copyOf(configs.values());
    }

    @Override
    public Collection<GenericPreset<PresetData>> getAllPresets() {
        return configs.values();
    }

    @Override
    public JsonElement toJsonElement(Gson gson, GenericPreset<?> preset) {
        return gson.toJsonTree(preset.data, presetClass.get(preset.getPresetSubType()));
    }

    @Override
    public PresetData fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        //LEGACY COMPATIBILITY: in old versions the config data was not wrapped in a "settings" map
        if(preset.version.equals("1")){
            if(element instanceof JsonObject){
                JsonObject object = (JsonObject) element;
                if(!object.has("settings")){
                    JsonObject newObject = new JsonObject();
                    JsonObject settings = new JsonObject();
                    ArrayList<String> keySet = new ArrayList<>(object.keySet());
                    keySet.forEach(s -> settings.add(s, object.remove(s)));
                    newObject.add("settings", settings);
                    element = newObject;
                }
            }
        }
        return gson.fromJson(element, presetClass.get(preset.getPresetSubType()));
    }

    public void markDirty(){
        if(loading){
            return;
        }
        //at the moment there is only one preset per config, TODO, when this changes this logic should too
        for(GenericPreset<PresetData> preset : getAllPresets()){
            getDefaultManager().updatePreset(null, preset, false);
        }
        queueJsonUpdate();
    }

    /**
     * This won't wait for a background thread which could be cancelled, save the json on the main thread.
     */
    public void onShutdown(){
        for(GenericPreset<PresetData> preset : getAllPresets()){
            getDefaultManager().updatePreset(null, preset, false);
        }
        saveToJSON();
    }
}