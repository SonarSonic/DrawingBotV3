package drawingbot.files.presets;

import drawingbot.javafx.GenericPreset;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as an object to be saved by GSON, forms the actual file which stores multiple presets
 */
public class PresetContainerJsonFile<T extends IJsonData> {

    public List<GenericPreset<T>> jsonMap;

    public PresetContainerJsonFile() {
        jsonMap = new ArrayList<>();
    }

    public PresetContainerJsonFile(List<GenericPreset<T>> jsonMap) {
        this.jsonMap = jsonMap;
    }
}
