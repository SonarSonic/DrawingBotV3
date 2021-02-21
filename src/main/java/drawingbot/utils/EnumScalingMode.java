package drawingbot.utils;

public enum EnumScalingMode {
    CROP_TO_FIT,
    SCALE_TO_FIT,
    STRETCH_TO_FIT;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
