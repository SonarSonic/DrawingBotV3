package drawingbot.utils;

public enum UnitsTime {

    SECONDS("secs", 1F),
    MINUTES("mins", 60F),
    HOURS("hours", 3600F);

    public final String displayName;
    public final float convertToSeconds;

    UnitsTime(String displayName, float convertToSeconds) {
        this.displayName = displayName;
        this.convertToSeconds = convertToSeconds;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public float toSeconds(float value){
        return value * convertToSeconds;
    }

    public static float convert(float value, UnitsTime from, UnitsTime to){
        if(from == to){
            return value;
        }
        return (value * from.convertToSeconds) / to.convertToSeconds;
    }
}
