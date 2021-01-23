package drawingbot.utils;

public enum EnumDisplayMode {
    DRAWING,
    ORIGINAL,
    REFERENCE,
    LIGHTENED,
    SELECTED_PEN;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
