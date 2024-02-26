package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as an object to be saved by GSON, forms the actual file which stores multiple presets
 */
@JsonData
public class PresetContainerJsonFile {

    public List<GenericPreset<?>> jsonMap;

    public PresetContainerJsonFile() {
        jsonMap = new ArrayList<>();
    }

    public PresetContainerJsonFile(List<GenericPreset<?>> jsonMap) {
        this.jsonMap = jsonMap;
    }
}
