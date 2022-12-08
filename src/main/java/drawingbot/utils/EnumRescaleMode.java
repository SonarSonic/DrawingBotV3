package drawingbot.utils;

public enum EnumRescaleMode {
    HIGH_QUALITY("High Quality"),
    LOW_QUALITY("Low Quality"),
    OFF("Off");

    public String displayName;

    EnumRescaleMode(String displayName) {
        this.displayName = displayName;
    }

    public boolean isHighQuality(){
        return this == HIGH_QUALITY;
    }

    public boolean isLowQuality(){
        return this == LOW_QUALITY;
    }

    public boolean isOff(){
        return this == OFF;
    }

    public boolean shouldRescale(){
        return this != OFF;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
