package drawingbot.files.json;

import com.google.gson.Gson;
import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DefaultPresetManager<O extends AbstractJsonData, I> extends AbstractPresetManager<O> {

    private final List<GenericSetting<?, ?>> settings = new ArrayList<>();
    public boolean changesOnly;

    public List<PresetDataLoader<O>> presetDataLoaders = new ArrayList<>();

    public DefaultPresetManager(AbstractJsonLoader<O> presetLoader) {
        super(presetLoader);
        registerDataLoaders();
    }

    public void registerDataLoaders(){}

    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    public final void registerSetting(GenericSetting<?, ?>setting){
        settings.add(setting);
    }

    public final void registerSettings(List<GenericSetting<?, ?>> setting){
        settings.addAll(setting);
    }

    public final void registerPresetDataLoader(PresetDataLoader<O> loader){
        presetDataLoaders.add(loader);
    }

    public abstract I getInstance(DBTaskContext context);

    @Override
    public GenericPreset<O> updatePreset(DBTaskContext context, GenericPreset<O> preset) {
        I instance = getInstance(context);
        if(instance != null){
            GenericSetting.updateSettingsFromInstance(settings, instance);
            preset.data.settings = GenericSetting.toJsonMap(settings, new HashMap<>(), false);

            Gson gson = JsonLoaderManager.createDefaultGson();
            for(PresetDataLoader<O> loader : presetDataLoaders){
                try {
                    loader.save(context, gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to save project data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<O> preset) {
        I instance = getInstance(context);
        if(instance != null) {
            GenericSetting.applySettings(preset.data.settings, settings);

            List<GenericSetting<?, ?>> toApply = settings;
            if (changesOnly) {
                toApply = GenericSetting.filterSettings(toApply, preset.data.settings.keySet());
            }
            GenericSetting.applySettingsToInstance(toApply, instance);

            Gson gson = JsonLoaderManager.createDefaultGson();
            for(PresetDataLoader<O> loader : presetDataLoaders){
                try {
                    loader.load(context, gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to load preset data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }
    }
}
