package drawingbot.files.presets.types;

import com.google.gson.JsonElement;
import drawingbot.files.presets.IJsonData;
import drawingbot.files.presets.PresetType;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.Register;

import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PresetImageFilters implements IJsonData {

    public List<Filter> filters;

    public PresetImageFilters() {
        filters = new ArrayList<>();
    }

    public void addFilter(GenericFactory<BufferedImageOp> factory, HashMap<String, JsonElement> settings) {
        filters.add(new Filter(true, factory.getName(), settings));
    }

    public void copyFilter(ObservableImageFilter filter) {
        filters.add(new Filter(filter.enable.get(), filter.filterFactory.getName(), GenericSetting.toJsonMap(filter.filterSettings, new HashMap<>(), false)));
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_FILTERS;
    }


    public static class Filter {

        public boolean isEnabled = true;
        public String type;
        public HashMap<String, JsonElement> settings;

        public Filter() {}

        public Filter(boolean isEnabled, String type, HashMap<String, JsonElement> settings) {
            this.isEnabled = isEnabled;
            this.type = type;
            this.settings = settings;
        }
    }
}
