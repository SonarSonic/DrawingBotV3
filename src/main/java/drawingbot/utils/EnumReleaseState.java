package drawingbot.utils;

public enum EnumReleaseState {
    RELEASE,
    BETA,
    ALPHA,
    EXPERIMENTAL; // Developer Only

    public boolean isRelease(){
        return this == RELEASE;
    }

    public boolean isBeta(){
        return this == BETA;
    }

    public boolean isAlpha(){
        return this == ALPHA;
    }

    public boolean isExperimental(){
        return this == EXPERIMENTAL;
    }

    public String getDisplayName() {
        return Utils.capitalize(name());
    }
}
