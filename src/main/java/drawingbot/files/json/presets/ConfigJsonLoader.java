package drawingbot.files.json.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.IConfigData;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ConfigJsonLoader extends AbstractJsonLoader<IConfigData> {

    public HashMap<String, GenericFactory<IConfigData>> configFactories = new HashMap<>();
    public HashMap<Class<? extends IConfigData>, GenericPreset<IConfigData>> configs = new HashMap<>();

    public ConfigJsonLoader(PresetType presetType) {
        super(presetType, "config_settings.json");
        registerTypes();
        setDefaultManager(new AbstractPresetManager<>(this) {
            @Override
            public GenericPreset<IConfigData> updatePreset(DBTaskContext context, GenericPreset<IConfigData> preset) {
                return configs.get(configFactories.get(preset.presetSubType).getInstanceClass()).data.updatePreset(preset);
            }

            @Override
            public void applyPreset(DBTaskContext context, GenericPreset<IConfigData> preset) {
                configs.get(configFactories.get(preset.presetSubType).getInstanceClass()).data.applyPreset(preset);
            }
        });
    }

    private void registerTypes(){
        //registerConfigFactory(PresetApplicationSettings.class, "application_settings", PresetApplicationSettings::new, false);
    }

    private <C extends IConfigData> void registerConfigFactory(Class<C> clazz, String name, Supplier<C> create, boolean isHidden){
        configFactories.put(name, (GenericFactory<IConfigData>)new GenericFactory<C>(clazz, name, create, isHidden));
        registerPreset(createNewPreset(name, "config", false)); //add default config, overwritten if the json contains one, prevents crash if we can't read the files
    }

    public <D> D getConfigData(Class<D> type){
        GenericPreset<IConfigData> preset = configs.get(type);
        Objects.requireNonNull(preset);
        if(type.isInstance(preset.data)){
            return (D) preset.data;
        }
        throw new NullPointerException("Invalid Config Data");
    }

    @Override
    protected IConfigData getPresetInstance(GenericPreset<IConfigData> preset) {
        return configFactories.get(preset.presetSubType).instance();
    }

    @Override
    protected void registerPreset(GenericPreset<IConfigData> preset) {
        configs.put(configFactories.get(preset.presetSubType).getInstanceClass(), preset);
    }

    @Override
    protected void unregisterPreset(GenericPreset<IConfigData> preset) {
        configs.remove(configFactories.get(preset.presetSubType).getInstanceClass());
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        return List.copyOf(configs.values());
    }

    @Override
    public Collection<GenericPreset<IConfigData>> getAllPresets() {
        return configs.values();
    }

    @Override
    public JsonElement toJsonElement(Gson gson, GenericPreset<?> preset) {
        return gson.toJsonTree(preset.data, configFactories.get(preset.presetSubType).getInstanceClass());
    }

    @Override
    public IConfigData fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        return gson.fromJson(element, configFactories.get(preset.presetSubType).getInstanceClass());
    }

    @Override
    public void loadFromJSON() {
        super.loadFromJSON();
        queueJsonUpdate(); //saves the defaults to the config file
    }
}