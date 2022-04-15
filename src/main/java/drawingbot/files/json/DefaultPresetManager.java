package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DefaultPresetManager<O extends AbstractJsonData, I> extends AbstractPresetManager<O> {

    private final List<GenericSetting<?, ?>> settings = new ArrayList<>();
    public boolean changesOnly;

    public DefaultPresetManager(AbstractPresetLoader<O> presetLoader) {
        super(presetLoader);
        registerSettings();
    }

    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    public final void registerSetting(GenericSetting<?, ?>setting){
        settings.add(setting);
    }

    public abstract void registerSettings();

    public abstract I getInstance();

    @Override
    public GenericPreset<O> updatePreset(GenericPreset<O> preset) {
        I instance = getInstance();
        if(instance != null){
            GenericSetting.updateSettingsFromInstance(settings, instance);
            preset.data.settingList = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
        }
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<O> preset) {
        I instance = getInstance();
        if(instance != null) {
            GenericSetting.applySettings(preset.data.settingList, settings);

            List<GenericSetting<?, ?>> toApply = settings;
            if (changesOnly) {
                toApply = GenericSetting.filterSettings(toApply, preset.data.settingList.keySet());
            }
            GenericSetting.applySettingsToInstance(toApply, instance);
        }
    }
}
