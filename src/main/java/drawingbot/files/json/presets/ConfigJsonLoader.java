package drawingbot.files.json.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.files.json.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;

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
        registerTypes();
        setDefaultManager(new AbstractPresetManager<>(this) {
            @Override
            public GenericPreset<AbstractJsonData> updatePreset(DBTaskContext context, GenericPreset<AbstractJsonData> preset) {
                IPresetManager<AbstractJsonData> manager = presetManagers.get(preset.presetSubType);
                if(manager != null){
                    manager.updatePreset(null, preset);
                }
                return preset;
            }

            @Override
            public void applyPreset(DBTaskContext context, GenericPreset<AbstractJsonData> preset) {
                IPresetManager<AbstractJsonData> manager = presetManagers.get(preset.presetSubType);
                if(manager != null){
                    manager.applyPreset(null, preset);
                }
            }
        });
    }

    private void registerTypes(){
        registerConfigFactory(PresetApplicationSettings.class, new DefaultPresetManager<AbstractJsonData, DBPreferences>(this) {

            @Override
            public void registerSettings() {
                registerSettings(DBPreferences.INSTANCE.settings);
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
        queueJsonUpdate();
    }

    @Override
    protected AbstractJsonData getPresetInstance(GenericPreset<AbstractJsonData> preset) {
        return presetFactories.get(preset.presetSubType).get();
    }

    @Override
    protected void registerPreset(GenericPreset<AbstractJsonData> preset) {
        configs.put(preset.presetSubType, preset);
    }

    @Override
    protected void unregisterPreset(GenericPreset<AbstractJsonData> preset) {
        configs.remove(preset.presetSubType);
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
        return gson.toJsonTree(preset.data, presetClass.get(preset.presetSubType));
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
        return gson.fromJson(element, presetClass.get(preset.presetSubType));
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