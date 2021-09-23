package drawingbot.utils;

import drawingbot.files.FileUtils;
import javafx.stage.FileChooser;

public enum EnumJsonType {

    CONFIG_SETTINGS("config_settings"),
    PFM_PRESET("pfm_settings"),
    IMAGE_FILTER_PRESET("image_filters"),
    DRAWING_SET("drawing_set"),
    DRAWING_PEN("drawing_pen"),
    DRAWING_AREA("drawing_area"),
    GCODE_SETTINGS("gcode_settings"),
    VPYPE_SETTINGS("vpype_settings"),
    PROJECT_PRESET("project", new FileChooser.ExtensionFilter[]{FileUtils.FILTER_PROJECT});

    public final String id;
    public final FileChooser.ExtensionFilter[] filters;

    EnumJsonType(String id){
        this(id, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_JSON});
    }

    EnumJsonType(String id, FileChooser.ExtensionFilter[] filters){
        this.id = id;
        this.filters = filters;
    }

    public static EnumJsonType fromId(String id){
        for(EnumJsonType type : values()){
            if(type.id.equals(id)){
                return type;
            }
        }
        return null;
    }

}