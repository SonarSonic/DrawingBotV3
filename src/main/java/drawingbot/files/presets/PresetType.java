package drawingbot.files.presets;

import drawingbot.files.FileUtils;
import javafx.stage.FileChooser;

public class PresetType {

    public final String id;
    public final FileChooser.ExtensionFilter[] filters;

    public PresetType(String id){
        this(id, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_JSON});
    }

    public PresetType(String id, FileChooser.ExtensionFilter[] filters){
        this.id = id;
        this.filters = filters;
    }

}
