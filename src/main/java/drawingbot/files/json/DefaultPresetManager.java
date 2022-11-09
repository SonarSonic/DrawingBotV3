package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DefaultPresetManager<O extends AbstractJsonData, I> extends AbstractPresetManager<O> {

    private final List<GenericSetting<?, ?>> settings = new ArrayList<>();
    public boolean changesOnly;

    public DefaultPresetManager(AbstractJsonLoader<O> presetLoader) {
        super(presetLoader);
        registerSettings();
    }

    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    public final void registerSetting(GenericSetting<?, ?>setting){
        settings.add(setting);
    }

    public final void registerSettings(List<GenericSetting<?, ?>> setting){
        settings.addAll(setting);
    }

    public abstract void registerSettings();

    public abstract I getInstance(DBTaskContext context);

    @Override
    public GenericPreset<O> updatePreset(DBTaskContext context, GenericPreset<O> preset) {
        I instance = getInstance(context);
        if(instance != null){
            GenericSetting.updateSettingsFromInstance(settings, instance);
            preset.data.settings = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
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
        }
    }
}
