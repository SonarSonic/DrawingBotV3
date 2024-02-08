package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

public class PresetDrawingAreaLoader extends AbstractPresetLoader<PresetData> {

    public PresetDrawingAreaLoader(PresetType presetType) {
        super(PresetData.class, presetType, "user_page_presets.json");
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Original Sizing");
    }

    @Override
    public PresetData createDataInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }
}
