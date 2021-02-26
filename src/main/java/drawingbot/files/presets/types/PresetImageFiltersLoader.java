package drawingbot.files.presets.types;

import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.image.ImageFilterRegistry;
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
        applyPreset(ImageFilterRegistry.getDefaultImageFilterPreset());
    }

    @Override
    public void registerPreset(GenericPreset<PresetImageFilters> preset) {
        ImageFilterRegistry.registerPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetImageFilters> preset) {
        ImageFilterRegistry.imagePresets.remove(preset);
    }

    @Override
    public GenericPreset<PresetImageFilters> updatePreset(GenericPreset<PresetImageFilters> preset) {
        preset.data.filters.clear();
        ImageFilterRegistry.currentFilters.forEach(preset.data::copyFilter);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetImageFilters> preset) {
        ImageFilterRegistry.currentFilters.clear();
        for (int i = 0; i < preset.data.filters.size(); i++) {
            PresetImageFilters.Filter filter = preset.data.filters.get(i);
            GenericFactory<BufferedImageOp> factory = ImageFilterRegistry.getFilterFromName(filter.type);
            ImageFilterRegistry.ObservableImageFilter observableImageFilter = new ImageFilterRegistry.ObservableImageFilter(factory);
            GenericSetting.applySettings(filter.settings, observableImageFilter.filterSettings);
            ImageFilterRegistry.currentFilters.add(observableImageFilter);
        }
    }

    @Override
    public GenericPreset<PresetImageFilters> getDefaultPreset() {
        return ImageFilterRegistry.getDefaultImageFilterPreset();
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (GenericPreset<PresetImageFilters> preset : ImageFilterRegistry.imagePresets) {
            if (preset.userCreated) {
                userCreated.add(preset);
            }
        }
        return userCreated;
    }
}
