package drawingbot.utils;

public enum UnitsLength {

    MILLIMETRES("mm", "mm", 1D),
    CENTIMETRES("cm", "cm", 10D),
    METRES("m", "m", 1000D),
    INCHES("inches", "in", 25.4D),
    PIXELS("pixels", "px", 1D);

    public String displayName;
    public String suffix;
    public double convertToMM;

    UnitsLength(String displayName, String suffix, double convertToMM) {
        this.displayName = displayName;
        this.suffix = suffix;
        this.convertToMM = convertToMM;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getSuffix(){
        return suffix;
    }

    public double toMM(double value){
        return value * convertToMM;
    }

    public static double convert(double value, UnitsLength from, UnitsLength to){
        if(from == to){
            return value;
        }
        return (value * from.convertToMM) / to.convertToMM;
    }

    public static double convert(double value, double toConvertToMM, double fromConvertToMM){
        if(toConvertToMM == fromConvertToMM){
            return value;
        }
        return (value * fromConvertToMM) / toConvertToMM;
    }
}
