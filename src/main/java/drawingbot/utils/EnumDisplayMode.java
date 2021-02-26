package drawingbot.utils;

public enum EnumDisplayMode {

    DRAWING, //TODO ADD IMAGE DISPLAY MODE
    ORIGINAL,
    REFERENCE,
    LIGHTENED,
    SELECTED_PEN;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
