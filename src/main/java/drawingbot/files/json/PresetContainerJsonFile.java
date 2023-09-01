package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as an object to be saved by GSON, forms the actual file which stores multiple presets
 */
@JsonData
public class PresetContainerJsonFile<T> {

    public List<GenericPreset<T>> jsonMap;

    public PresetContainerJsonFile() {
        jsonMap = new ArrayList<>();
    }

    public PresetContainerJsonFile(List<GenericPreset<T>> jsonMap) {
        this.jsonMap = jsonMap;
    }
}
