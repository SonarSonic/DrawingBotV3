package drawingbot.files.presets.types;

import drawingbot.files.presets.IJsonData;
import drawingbot.image.ImageFilterRegistry;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericSetting;

import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PresetImageFilters implements IJsonData {

    public List<Filter> filters;

    public PresetImageFilters() {
        filters = new ArrayList<>();
    }

    public void addFilter(GenericFactory<BufferedImageOp> factory, HashMap<String, String> settings) {
        filters.add(new Filter(factory.getName(), settings));
    }

    public void copyFilter(ImageFilterRegistry.ObservableImageFilter filter) {
        filters.add(new Filter(filter.filterFactory.getName(), GenericSetting.toJsonMap(filter.filterSettings, new HashMap<>())));
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.IMAGE_FILTER_PRESET;
    }

    public static class Filter {
        public String type;
        public HashMap<String, String> settings;

        public Filter() {}

        public Filter(String type, HashMap<String, String> settings) {
            this.type = type;
            this.settings = settings;
        }
    }
}
