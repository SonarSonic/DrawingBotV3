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
public class ConfigJsonLoader extends AbstractJsonLoader<AbstractJsonData> {

    public HashMap<String, IPresetManager<AbstractJsonData>> presetManagers = new HashMap<>();
    public HashMap<String, Supplier<? extends AbstractJsonData>> presetFactories = new HashMap<>();
    public HashMap<String, Class<? extends AbstractJsonData>> presetClass = new HashMap<>();
    public HashMap<String, GenericPreset<AbstractJsonData>> configs = new HashMap<>();
    public boolean loading;

    public ConfigJsonLoader(PresetType presetType) {
        super(presetType, "config_settings.json");
        setDefaultManager(new AbstractPresetManager<>(this) {
            @Override
            public GenericPreset<AbstractJsonData> updatePreset(DBTaskContext context, GenericPreset<AbstractJsonData> preset) {
                IPresetManager<AbstractJsonData> manager = presetManagers.get(preset.getPresetSubType());
                if(manager != null){
                    manager.updatePreset(null, preset);
                }
                return preset;
            }

            @Override
            public void applyPreset(DBTaskContext context, GenericPreset<AbstractJsonData> preset) {
                IPresetManager<AbstractJsonData> manager = presetManagers.get(preset.getPresetSubType());
                if(manager != null){
                    manager.applyPreset(null, preset);
                }
            }
        });
    }

    @Override
    public void init(){
        registerConfigFactory(PresetApplicationSettings.class, new DefaultPresetManager<AbstractJsonData, DBPreferences>(this) {

            @Override
            public void registerDataLoaders() {
                registerSettings(MasterRegistry.INSTANCE.applicationSettings);
                registerPresetDataLoader(new PresetDataLoader.DataInstance<>(AbstractJsonData.class, "ui_state", PresetProjectSettingsManager.UIGlobalState.class, PresetProjectSettingsManager.UIGlobalState::new, 0){

                    @Override
                    public void loadData(DBTaskContext context, PresetProjectSettingsManager.UIGlobalState data, GenericPreset<AbstractJsonData> preset) {
                        FXHelper.loadUIStates(data.nodes);
                    }

                    @Override
                    public void saveData(DBTaskContext context, PresetProjectSettingsManager.UIGlobalState data, GenericPreset<AbstractJsonData> preset) {
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
        }, "application_settings", PresetApplicationSettings::new, false);
    }

    private void registerConfigFactory(Class<? extends AbstractJsonData> clazz, IPresetManager<AbstractJsonData> presetManager, String name, Supplier<AbstractJsonData> create, boolean isHidden){
        presetManagers.put(name, presetManager);
        presetFactories.put(name, create);
        presetClass.put(name, clazz);
    }

    @Override
    protected void onJSONLoaded() {
        super.onJSONLoaded();
        loading = true;
        //if a preset currently exists, load it, if it doesn't create one
        for(Map.Entry<String, IPresetManager<AbstractJsonData>> managerEntry : presetManagers.entrySet()){
            GenericPreset<AbstractJsonData> preset = configs.get(managerEntry.getKey());
            if(preset == null){
                preset = createNewPreset(managerEntry.getKey(), "config", false);
                getDefaultManager().updatePreset(null, preset);
                registerPreset(preset);
            }else{
                getDefaultManager().applyPreset(null, preset);
            }
        }
        loading = false;
    }

    @Override
    protected AbstractJsonData getPresetInstance(GenericPreset<AbstractJsonData> preset) {
        return presetFactories.get(preset.getPresetSubType()).get();
    }

    @Override
    protected void registerPreset(GenericPreset<AbstractJsonData> preset) {
        configs.put(preset.getPresetSubType(), preset);
    }

    @Override
    protected void unregisterPreset(GenericPreset<AbstractJsonData> preset) {
        configs.remove(preset.getPresetSubType());
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        return List.copyOf(configs.values());
    }

    @Override
    public Collection<GenericPreset<AbstractJsonData>> getAllPresets() {
        return configs.values();
    }

    @Override
    public JsonElement toJsonElement(Gson gson, GenericPreset<?> preset) {
        return gson.toJsonTree(preset.data, presetClass.get(preset.getPresetSubType()));
    }

    @Override
    public AbstractJsonData fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
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
        for(GenericPreset<AbstractJsonData> preset : getAllPresets()){
            getDefaultManager().updatePreset(null, preset);
        }
        queueJsonUpdate();
    }
}