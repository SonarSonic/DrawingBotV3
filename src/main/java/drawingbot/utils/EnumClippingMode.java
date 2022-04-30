package drawingbot.utils;

public enum EnumClippingMode {
    NONE,
    DRAWING,
    PAGE;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
