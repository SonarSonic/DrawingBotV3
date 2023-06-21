package drawingbot.files.json;

import drawingbot.files.FileUtils;
import javafx.stage.FileChooser;

public class PresetType {

    public final String id;
    public final String displayName;
    public final FileChooser.ExtensionFilter[] filters;
    public boolean defaultsPerSubType = false;

    public PresetType(String id, String displayName){
        this(id, displayName, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_JSON});
    }

    public PresetType(String id, String displayName, FileChooser.ExtensionFilter[] filters){
        this.id = id;
        this.displayName = displayName;
        this.filters = filters;
    }

    public PresetType setDefaultsPerSubType(boolean defaultsPerSubType){
        this.defaultsPerSubType = defaultsPerSubType;
        return this;
    }


}
