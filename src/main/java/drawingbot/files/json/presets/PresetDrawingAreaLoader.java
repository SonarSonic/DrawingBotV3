package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

public class PresetDrawingAreaLoader extends AbstractPresetLoader<PresetDrawingArea> {

    public PresetDrawingAreaLoader(PresetType presetType) {
        super(PresetDrawingArea.class, presetType, "user_page_presets.json");
        setDefaultManager(new PresetDrawingAreaManager(this) {
            @Override
            public ObservableCanvas getInstance() {
                return DrawingBotV3.INSTANCE.drawingArea;
            }
        });
    }

    @Override
    public GenericPreset<PresetDrawingArea> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Original Sizing");
    }

    @Override
    protected PresetDrawingArea getPresetInstance(GenericPreset<PresetDrawingArea> preset) {
        return new PresetDrawingArea();
    }
}
