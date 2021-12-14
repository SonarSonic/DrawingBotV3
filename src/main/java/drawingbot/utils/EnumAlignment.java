package drawingbot.utils;

public enum EnumAlignment {
    CENTER,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public static EnumAlignment[] xAxis = new EnumAlignment[]{CENTER, LEFT, RIGHT};
    public static EnumAlignment[] yAxis = new EnumAlignment[]{CENTER, TOP, BOTTOM};

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
