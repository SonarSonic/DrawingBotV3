package drawingbot.utils;

public enum Units {

    MILLIMETRES("mm", 1F),
    CENTIMETRES("cm", 10F),
    INCHES("inches", 25.4F);

    public String displayName;
    public float convertToMM;

    Units(String displayName, float convertToMM) {
        this.displayName = displayName;
        this.convertToMM = convertToMM;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public float toMM(float value){
        return value * convertToMM;
    }

    public static float convert(float value, Units from, Units to){
        if(from == to){
            return value;
        }
        return (value * from.convertToMM) / to.convertToMM;
    }
}
