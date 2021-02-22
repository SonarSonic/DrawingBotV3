package drawingbot.utils;

public enum EnumJsonType {

    CONFIG_SETTINGS("config_settings"),
    PFM_PRESET("pfm_settings"),
    IMAGE_FILTER_PRESET("image_filters"),
    DRAWING_SET("drawing_set"),
    DRAWING_PEN("drawing_pen");

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