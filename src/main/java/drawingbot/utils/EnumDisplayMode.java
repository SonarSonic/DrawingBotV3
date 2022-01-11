package drawingbot.utils;

public enum EnumDisplayMode {

    IMAGE(EnumDisplayModeType.IMAGE),
    DRAWING(EnumDisplayModeType.TASK),
    ORIGINAL(EnumDisplayModeType.TASK),
    REFERENCE(EnumDisplayModeType.TASK),
    LIGHTENED(EnumDisplayModeType.TASK),
    SELECTED_PEN(EnumDisplayModeType.TASK),
    HARDWARE_ACCELERATED_RENDERER(EnumDisplayModeType.TASK);

    public final EnumDisplayModeType type;

    EnumDisplayMode(EnumDisplayModeType type){
        this.type = type;
    }

    public boolean isOpenGL(){
        return this == HARDWARE_ACCELERATED_RENDERER;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
