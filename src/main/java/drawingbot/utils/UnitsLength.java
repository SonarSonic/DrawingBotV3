package drawingbot.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public enum UnitsLength {

    MILLIMETRES("mm", "mm", 1F),
    CENTIMETRES("cm", "cm", 10F),
    INCHES("inches", "in", 25.4F),
    PIXELS("pixels", "px", 1F);

    public String displayName;
    public String suffix;
    public float convertToMM;

    public static final ObservableList<UnitsLength> OBSERVABLE_LIST = FXCollections.observableArrayList(UnitsLength.values());

    UnitsLength(String displayName, String suffix, float convertToMM) {
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

    public float toMM(float value){
        return value * convertToMM;
    }

    public static float convert(float value, UnitsLength from, UnitsLength to){
        if(from == to){
            return value;
        }
        return (value * from.convertToMM) / to.convertToMM;
    }

    public static double convert(double value, UnitsLength from, UnitsLength to){
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

    public static double convert(double value, double toConvertToMM, double fromConvertToMM){
        if(toConvertToMM == fromConvertToMM){
            return value;
        }
        return (value * fromConvertToMM) / toConvertToMM;
    }
}
