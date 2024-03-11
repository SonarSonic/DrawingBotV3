package drawingbot.render.modes;

import drawingbot.render.renderer.JFXRenderer;

/**
 * Implemented on all {@link DisplayModeBase} which utilise the {@link JFXRenderer}
 */
public interface IJFXDisplayMode {

    /**
     * Called every render tick, regardless of if this DisplayMode is drawn or not, should be used to update the renderers state.
     * It can be used to alter the outcome of a call to {@link #isRenderDirty(JFXRenderer)}
     */
    default void onRenderTick(JFXRenderer jfr) {}

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

    /**
     * @return true if the JavaFX Canvas needs to re-rendered
     */
    boolean isRenderDirty(JFXRenderer jfr);
}
