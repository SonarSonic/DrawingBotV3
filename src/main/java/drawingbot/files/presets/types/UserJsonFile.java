package drawingbot.files.presets.types;

import drawingbot.files.presets.IJsonData;
import drawingbot.javafx.GenericPreset;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as an object to be saved by GSON, forms the actual file which stores multiple presets
 */
public class UserJsonFile<T extends IJsonData> {

    public List<GenericPreset<T>> jsonMap;

    public UserJsonFile() {
        jsonMap = new ArrayList<>();
    }

    public UserJsonFile(List<GenericPreset<T>> jsonMap) {
        this.jsonMap = jsonMap;
    }
}
