package drawingbot.utils;

public enum EnumDirection {
    NEGATIVE,
    POSITIVE;

    public int asInteger(){
        return this == NEGATIVE ? -1 : 1;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
