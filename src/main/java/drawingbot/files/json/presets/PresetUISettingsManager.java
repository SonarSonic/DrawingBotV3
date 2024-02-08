package drawingbot.files.json.presets;

import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.DBPreferences;
import javafx.collections.FXCollections;

public class PresetUISettingsManager extends DefaultPresetManager<ObservableProject, PresetData> {

    public PresetUISettingsManager(PresetUISettingsLoader presetLoader) {
        super(presetLoader, ObservableProject.class);
    }

    @Override
    public void registerDataLoaders() {
        registerSetting(GenericSetting.createOptionSetting(ObservableProject.class, EnumBlendMode.class, "blendMode", FXCollections.observableArrayList(EnumBlendMode.values()), EnumBlendMode.NORMAL, project -> project.blendMode));
        registerSetting(GenericSetting.createColourSetting(ObservableProject.class, "canvasColor", DBPreferences.INSTANCE.defaultCanvasColour.get(), project -> project.drawingArea.get().canvasColor));
        registerSetting(GenericSetting.createColourSetting(ObservableProject.class, "backgroundColor", DBPreferences.INSTANCE.defaultBackgroundColour.get(), project -> project.drawingArea.get().backgroundColor));
    }

    @Override
    public ObservableProject getTargetFromContext(DBTaskContext context) {
        return context.project();
    }
}