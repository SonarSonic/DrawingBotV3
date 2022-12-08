package drawingbot.files.json.presets;

import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.DBPreferences;
import javafx.collections.FXCollections;

public class PresetUISettingsManager extends DefaultPresetManager<PresetUISettings, ObservableProject> {

    public PresetUISettingsManager(PresetUISettingsLoader presetLoader) {
        super(presetLoader);
    }

    @Override
    public void registerDataLoaders() {
        registerSetting(GenericSetting.createOptionSetting(ObservableProject.class, EnumBlendMode.class, "blendMode", FXCollections.observableArrayList(EnumBlendMode.values()), EnumBlendMode.NORMAL, i -> i.blendMode));
        registerSetting(GenericSetting.createColourSetting(ObservableProject.class, "canvasColor", DBPreferences.INSTANCE.defaultCanvasColour.get(), i -> i.drawingArea.get().canvasColor));
        registerSetting(GenericSetting.createColourSetting(ObservableProject.class, "backgroundColor", DBPreferences.INSTANCE.defaultBackgroundColour.get(), i -> i.drawingArea.get().backgroundColor));

    }

    @Override
    public ObservableProject getInstance(DBTaskContext context) {
        return context.project();
    }
}