package drawingbot.utils;

public enum UnitsLength {

    MILLIMETRES("mm", 1F),
    CENTIMETRES("cm", 10F),
    INCHES("inches", 25.4F),
    PIXELS("pixels", 1F);

    public String displayName;
    public float convertToMM;

    UnitsLength(String displayName, float convertToMM) {
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

    public static float convert(float value, UnitsLength from, UnitsLength to){
        if(from == to){
            return value;
        }
        return (value * from.convertToMM) / to.convertToMM;
    }

    public static float convert(float value, float toConvertToMM, float fromConvertToMM){
        if(toConvertToMM == fromConvertToMM){
            return value;
        }
        return (value * fromConvertToMM) / toConvertToMM;
    }
}
