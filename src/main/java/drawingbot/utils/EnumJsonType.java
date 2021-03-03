package drawingbot.utils;

public enum EnumJsonType {

    CONFIG_SETTINGS("config_settings"),
    PFM_PRESET("pfm_settings"),
    IMAGE_FILTER_PRESET("image_filters"),
    DRAWING_SET("drawing_set"),
    DRAWING_PEN("drawing_pen"),
    DRAWING_AREA("drawing_area"),
    GCODE_SETTINGS("gcode_settings");

    public String id;
    EnumJsonType(String id){
        this.id = id;
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