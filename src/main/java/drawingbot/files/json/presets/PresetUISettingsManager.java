package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericSetting;
import drawingbot.plotting.canvas.ObservableCanvas;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

import java.util.List;

public class PresetUISettingsManager extends DefaultPresetManager<PresetUISettings, ObservableProject> {

    public PresetUISettingsManager(PresetUISettingsLoader presetLoader) {
        super(presetLoader);
    }

    @Override
    public void registerSettings() {
        registerSetting(GenericSetting.createOptionSetting(ObservableProject.class, EnumBlendMode.class, "blendMode", FXCollections.observableArrayList(EnumBlendMode.values()), EnumBlendMode.NORMAL, i -> i.blendMode));
        registerSetting(GenericSetting.createColourSetting(ObservableProject.class, "canvasColor", Color.WHITE, i -> i.drawingArea.get().canvasColor));
        registerSetting(GenericSetting.createColourSetting(ObservableProject.class, "backgroundColor", ObservableCanvas.backgroundColourDefault, i -> i.drawingArea.get().backgroundColor));
        
    }

    @Override
    public ObservableProject getInstance(DBTaskContext context) {
        return context.project();
    }
}