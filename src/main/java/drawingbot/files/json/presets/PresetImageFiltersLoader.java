package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

public class PresetImageFiltersLoader extends AbstractPresetLoader<PresetImageFilters> {

    public PresetImageFiltersLoader(PresetType presetType) {
        super(PresetImageFilters.class, presetType, "user_filter_presets.json");
    }

    @Override
    public PresetImageFilters createDataInstance(GenericPreset<PresetImageFilters> preset) {
        return new PresetImageFilters();
    }

    @Override
    public GenericPreset<PresetImageFilters> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }
}
