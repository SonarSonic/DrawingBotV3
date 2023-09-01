package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.registry.MasterRegistry;

public class PresetDrawingAreaLoader extends AbstractPresetLoader<PresetData> {

    public PresetDrawingAreaLoader(PresetType presetType) {
        super(PresetData.class, presetType, "user_page_presets.json");
        setDefaultManager(new PresetDrawingAreaManager(this) {
            @Override
            public ObservableCanvas getInstance(DBTaskContext context) {
                return context.project().drawingArea.get();
            }
        });
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Original Sizing");
    }

    @Override
    protected PresetData getPresetInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }
}
