package drawingbot.render.shapes.editing;

public enum TransformModeType {

    TRANSLATE,
    SCALE,
    ROTATE,
    SKEW;

    public boolean isTranslation() {
        return this == TRANSLATE;
    }

    public boolean isRotation() {
        return this == ROTATE;
    }

    public boolean isSkew() {
        return this == SKEW;
    }

    public boolean isScale() {
        return this == SCALE;
    }
}
