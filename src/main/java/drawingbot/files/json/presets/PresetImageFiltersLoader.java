package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;

import java.awt.image.BufferedImageOp;

public class PresetImageFiltersLoader extends AbstractPresetLoader<PresetImageFilters> {

    public PresetImageFiltersLoader(PresetType presetType) {
        super(PresetImageFilters.class, presetType, "user_filter_presets.json");
    }

    @Override
    public PresetImageFilters getPresetInstance(GenericPreset<PresetImageFilters> preset) {
        return new PresetImageFilters();
    }

    @Override
    public void registerPreset(GenericPreset<PresetImageFilters> preset) {
        DrawingBotV3.logger.finest("Registering Image Filter Preset: " + preset.presetName);
        super.registerPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetImageFilters> preset) {
        DrawingBotV3.logger.finest("Unregistering Image Filter Preset: " + preset.presetName);
        super.unregisterPreset(preset);
    }

    @Override
    public GenericPreset<PresetImageFilters> updatePreset(GenericPreset<PresetImageFilters> preset) {
        preset.data.filters.clear();
        DrawingBotV3.INSTANCE.currentFilters.forEach(preset.data::copyFilter);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetImageFilters> preset) {
        DrawingBotV3.INSTANCE.currentFilters.clear();
        for (int i = 0; i < preset.data.filters.size(); i++) {
            PresetImageFilters.Filter filter = preset.data.filters.get(i);
            GenericFactory<BufferedImageOp> factory = MasterRegistry.INSTANCE.getImageFilterFactory(filter.type);
            ObservableImageFilter observableImageFilter = new ObservableImageFilter(filter.isEnabled, factory);
            GenericSetting.applySettings(filter.settings, observableImageFilter.filterSettings);
            DrawingBotV3.INSTANCE.currentFilters.add(observableImageFilter);
        }
    }

    @Override
    public GenericPreset<PresetImageFilters> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }
}
