package drawingbot.utils;

public enum EnumDisplayMode {

    IMAGE(EnumDisplayModeType.IMAGE),
    DRAWING(EnumDisplayModeType.TASK),
    ORIGINAL(EnumDisplayModeType.TASK),
    REFERENCE(EnumDisplayModeType.TASK),
    LIGHTENED(EnumDisplayModeType.TASK),
    SELECTED_PEN(EnumDisplayModeType.TASK);

    public EnumDisplayModeType type;

    EnumDisplayMode(EnumDisplayModeType type){
        this.type = type;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
