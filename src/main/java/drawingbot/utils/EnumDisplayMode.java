package drawingbot.utils;

public enum EnumDisplayMode {

    IMAGE(false),
    DRAWING(true),
    ORIGINAL(true),
    REFERENCE(true),
    LIGHTENED(true),
    SELECTED_PEN(true);

    public boolean scaleToTask;

    EnumDisplayMode(boolean scaleToTask){
        this.scaleToTask = scaleToTask;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
