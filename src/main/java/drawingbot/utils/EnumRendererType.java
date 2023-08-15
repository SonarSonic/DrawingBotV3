package drawingbot.utils;

public enum EnumRendererType {
    ANY,
    JAVA_FX,
    OPEN_GL;

    public boolean reRenderJFX(){
        return this == ANY || this == JAVA_FX;
    }

    public boolean reRenderOpenGL(){
        return this == ANY || this == OPEN_GL;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
