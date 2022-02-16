package drawingbot.render;

public interface IDisplayMode {

    String getName();

    default void applySettings(){}

    default void resetSettings(){}

    IRenderer getRenderer();

}
