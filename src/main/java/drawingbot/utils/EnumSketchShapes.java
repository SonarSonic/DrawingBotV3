package drawingbot.utils;

public enum EnumSketchShapes {

    RECTANGLES,
    ELLIPSES;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
