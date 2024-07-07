package drawingbot.files.json.presets;

import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetDataLoader;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettingsManager;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;

import java.util.List;

public class PresetPreferencesManager extends DefaultPresetManager<DBPreferences, PresetData> {

    public PresetPreferencesManager(IPresetLoader<PresetData> presetLoader) {
        super(presetLoader, DBPreferences.class);
    }

    @Override
    public DBPreferences getTargetFromContext(DBTaskContext context) {
        return DBPreferences.INSTANCE;
    }

    @Override
    public List<GenericSetting<?, ?>> getSettings() {
        return MasterRegistry.INSTANCE.applicationSettings;
    }

    @Override
    public void registerDataLoaders() {
        super.registerDataLoaders();
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
}
