package drawingbot.utils;

public enum EnumOrientation {

    PORTRAIT,
    LANDSCAPE;

    public static EnumOrientation getType(double width, double height){
        if(width == height){
            return null;
        }
        return width > height ? EnumOrientation.LANDSCAPE : EnumOrientation.PORTRAIT;
    }

    public EnumOrientation rotate(){
        return this == PORTRAIT ? LANDSCAPE : PORTRAIT;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
