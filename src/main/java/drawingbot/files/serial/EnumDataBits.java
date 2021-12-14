package drawingbot.files.serial;

public enum EnumDataBits {

    BITS_8("8 bits", 8),
    BITS_7("7 bits", 7),
    BITS_6("6 bits", 6),
    BITS_5("5 bits", 5);

    public String displayName;
    public int flag;

    EnumDataBits(String displayName, int flag){
        this.displayName = displayName;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
