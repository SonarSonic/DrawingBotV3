package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;

import java.awt.image.BufferedImageOp;

public class PresetImageFiltersManager extends AbstractPresetManager<ImageFilterSettings, PresetImageFilters> {

    public PresetImageFiltersManager(PresetImageFiltersLoader presetLoader) {
        super(presetLoader, ImageFilterSettings.class);
    }

    @Override
    public ImageFilterSettings getTargetFromContext(DBTaskContext context) {
        return context.project().getImageSettings();
    }

    @Override
    public void updatePreset(DBTaskContext context, ImageFilterSettings target, GenericPreset<PresetImageFilters> preset) {
        if(target != null){
            preset.data.filters.clear();
            target.currentFilters.get().forEach(preset.data::copyFilter);
        }
    }

    @Override
    public void applyPreset(DBTaskContext context, ImageFilterSettings target, GenericPreset<PresetImageFilters> preset, boolean changesOnly) {
        if(target != null) {
            target.currentFilters.get().clear();
            for (int i = 0; i < preset.data.filters.size(); i++) {
                PresetImageFilters.Filter filter = preset.data.filters.get(i);
                GenericFactory<BufferedImageOp> factory = MasterRegistry.INSTANCE.getImageFilterFactory(filter.type);
                if(factory != null){
                    ObservableImageFilter observableImageFilter = new ObservableImageFilter(filter.isEnabled, factory);
                    GenericSetting.applySettings(filter.settings, observableImageFilter.filterSettings);
                    target.currentFilters.get().add(observableImageFilter);
                }else{
                    DrawingBotV3.logger.warning("Missing image filter type: " + filter.type);
                }
            }
        }
    }

    @Override
    public DefaultPresetEditor<ImageFilterSettings, PresetImageFilters> createPresetEditor() {
        return new PresetImageFiltersEditor(this);
    }
}
