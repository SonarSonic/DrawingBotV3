package drawingbot.files.json;

import drawingbot.files.FileUtils;
import javafx.stage.FileChooser;

public class PresetType {

    public final String id;
    public final String displayName;
    public final FileChooser.ExtensionFilter[] filters;
    public boolean defaultsPerSubType = false;
    public boolean ignoreSubType = false;

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

    public PresetType setIgnoreSubType(boolean ignoreSubType){
        this.ignoreSubType = ignoreSubType;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FileChooser.ExtensionFilter[] getFilters() {
        return filters;
    }

    public boolean isDefaultsPerSubType() {
        return defaultsPerSubType;
    }
}