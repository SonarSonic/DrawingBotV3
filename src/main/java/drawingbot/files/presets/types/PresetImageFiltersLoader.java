package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;

import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;

public class PresetImageFiltersLoader extends AbstractPresetLoader<PresetImageFilters> {

    public PresetImageFiltersLoader() {
        super(PresetImageFilters.class, EnumJsonType.IMAGE_FILTER_PRESET, "user_filter_presets.json");
    }

    @Override
    public PresetImageFilters getPresetInstance(GenericPreset<PresetImageFilters> preset) {
        return new PresetImageFilters();
    }

    @Override
    public void onJSONLoaded() {
        super.onJSONLoaded();
        applyPreset(MasterRegistry.INSTANCE.getDefaultImageFilterPreset());
    }

    @Override
    public void registerPreset(GenericPreset<PresetImageFilters> preset) {
        MasterRegistry.INSTANCE.registerImageFilterPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetImageFilters> preset) {
        MasterRegistry.INSTANCE.imgFilterPresets.remove(preset);
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
        return MasterRegistry.INSTANCE.getDefaultImageFilterPreset();
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (GenericPreset<PresetImageFilters> preset : MasterRegistry.INSTANCE.imgFilterPresets) {
            if (preset.userCreated) {
                userCreated.add(preset);
            }
        }
        return userCreated;
    }
}
