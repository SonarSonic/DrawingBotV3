package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImageOp;

public class PresetImageFiltersLoader extends AbstractPresetLoader<PresetImageFilters> {

    public PresetImageFiltersLoader(PresetType presetType) {
        super(PresetImageFilters.class, presetType, "user_filter_presets.json");
        setDefaultManager(new PresetImageFiltersManager(this) {
            @Override
            public Property<ObservableList<ObservableImageFilter>> imageFiltersProperty() {
                return DrawingBotV3.INSTANCE.imgFilterSettings.currentFilters;
            }
        });
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
    public GenericPreset<PresetImageFilters> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }
}
