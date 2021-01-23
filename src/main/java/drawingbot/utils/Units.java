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
}
