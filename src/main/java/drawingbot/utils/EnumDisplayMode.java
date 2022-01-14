package drawingbot.utils;

public enum EnumDisplayMode {

    IMAGE(EnumDisplayModeType.IMAGE),
    DRAWING(EnumDisplayModeType.TASK),
    DRAWING_HARDWARE_ACCELERATED(EnumDisplayModeType.TASK),
    ORIGINAL(EnumDisplayModeType.TASK),
    REFERENCE(EnumDisplayModeType.TASK),
    LIGHTENED(EnumDisplayModeType.TASK),
    SELECTED_PEN(EnumDisplayModeType.TASK);

    public final EnumDisplayModeType type;

    EnumDisplayMode(EnumDisplayModeType type){
        this.type = type;
    }

    public boolean isOpenGL(){
        return this == DRAWING_HARDWARE_ACCELERATED;
    }

    @Override
    public String toString() {
        if(this == DRAWING_HARDWARE_ACCELERATED){
            return "Drawing (Hardware Accelerated)";
        }

        return Utils.capitalize(name());
    }
}
