package drawingbot.render.modes;

import drawingbot.render.renderer.JFXRenderer;

/**
 * Implemented on all {@link DisplayModeBase} which utilise the {@link JFXRenderer}
 */
public interface IJFXDisplayMode {

    /**
     * Perform any pre-render checks / updates
     * @param jfr the JFXRenderer instance
     */
    default void preRender(JFXRenderer jfr){}

    /**
     * Perform the render e.g. draw the image / drawing etc.
     * @param jfr the JFXRenderer instance
     */
    void doRender(JFXRenderer jfr);

    /**
     * Perform any post render operations
     * @param jfr the JFXRenderer instance
     */
    default void postRender(JFXRenderer jfr){}
}
