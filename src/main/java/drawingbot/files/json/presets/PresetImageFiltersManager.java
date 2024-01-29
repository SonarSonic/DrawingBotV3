package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImageOp;

public abstract class PresetImageFiltersManager extends AbstractPresetManager<PresetImageFilters> {

    public PresetImageFiltersManager(PresetImageFiltersLoader presetLoader) {
        super(presetLoader);
    }

    public abstract Property<ObservableList<ObservableImageFilter>> imageFiltersProperty(DBTaskContext context);

    @Override
    public GenericPreset<PresetImageFilters> updatePreset(DBTaskContext context, GenericPreset<PresetImageFilters> preset, boolean loadingProject) {
        ObservableList<ObservableImageFilter> filterList = imageFiltersProperty(context).getValue();
        if(filterList != null){
            preset.data.filters.clear();
            filterList.forEach(preset.data::copyFilter);
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<PresetImageFilters> preset, boolean changesOnly, boolean loadingProject) {
        ObservableList<ObservableImageFilter> filterList = imageFiltersProperty(context).getValue();
        if(filterList != null) {
            filterList.clear();
            for (int i = 0; i < preset.data.filters.size(); i++) {
                PresetImageFilters.Filter filter = preset.data.filters.get(i);
                GenericFactory<BufferedImageOp> factory = MasterRegistry.INSTANCE.getImageFilterFactory(filter.type);
                ObservableImageFilter observableImageFilter = new ObservableImageFilter(filter.isEnabled, factory);
                GenericSetting.applySettings(filter.settings, observableImageFilter.filterSettings);
                filterList.add(observableImageFilter);
            }
        }
    }
}
