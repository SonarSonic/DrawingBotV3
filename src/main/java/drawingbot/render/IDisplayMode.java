package drawingbot.render;

import drawingbot.utils.flags.FlagStates;

public interface IDisplayMode {

    String getName();

    default void applySettings(){}

    default void resetSettings(){}

    IRenderer getRenderer();

    FlagStates getRenderFlags();

    default boolean isHidden(){
        return false;
    }

}
