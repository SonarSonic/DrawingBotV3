package drawingbot.utils;

public enum EnumReleaseState {
    RELEASE,
    BETA,
    ALPHA;

    public boolean isRelease(){
        return this == RELEASE;
    }

    public boolean isBeta(){
        return this == BETA;
    }

    public boolean isAlpha(){
        return this == ALPHA;
    }

    public String getDisplayName() {
        return Utils.capitalize(name());
    }
}
