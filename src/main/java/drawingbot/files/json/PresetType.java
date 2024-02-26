package drawingbot.files.json;

import drawingbot.files.FileUtils;
import javafx.stage.FileChooser;

public class PresetType {

    public final String registryName;
    public final String displayName;
    public final FileChooser.ExtensionFilter[] filters;
    public boolean defaultsPerSubType = false;

    public SubTypeBehaviour subTypeBehaviour = SubTypeBehaviour.FIXED;

    public boolean hidden = false;

    public PresetType(String registryName, String displayName){
        this(registryName, displayName, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_JSON});
    }

    public PresetType(String registryName, String displayName, FileChooser.ExtensionFilter[] filters){
        this.registryName = registryName;
        this.displayName = displayName;
        this.filters = filters;
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHidden() {
        return hidden;
    }

    public FileChooser.ExtensionFilter[] getFilters() {
        return filters;
    }

    public SubTypeBehaviour getSubTypeBehaviour() {
        return subTypeBehaviour;
    }

    public boolean isDefaultsPerSubType() {
        return defaultsPerSubType;
    }

    public PresetType setDefaultsPerSubType(boolean defaultsPerSubType){
        this.defaultsPerSubType = defaultsPerSubType;
        return this;
    }

    public PresetType setSubTypeBehaviour(SubTypeBehaviour subTypeBehaviour){
        this.subTypeBehaviour = subTypeBehaviour;
        return this;
    }

    public PresetType setHidden(boolean hidden){
        this.hidden = hidden;
        return this;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    ////////////////////////////////////////////////////

    /**
     * Defines the treatment of the {@link drawingbot.javafx.GenericPreset} sub types
     * e.g. if they are required and if they can be edited
     */
    public enum SubTypeBehaviour{
        /**
         * Sub types can only consist of those registered by the software. e.g. PFM Presets
         */
        FIXED,
        /**
         * Sub types can be created by the user or registered by the system
         */
        EDITABLE,
        /**
         * The preset type doesn't use sub types
         */
        IGNORED;

        public boolean isFixed() {
            return this == FIXED;
        }

        public boolean isEditable() {
            return this == EDITABLE;
        }

        public boolean isIgnored() {
            return this == IGNORED;
        }
    }
}