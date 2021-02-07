package drawingbot.api;

/**
 * {@link IDrawingPen} defines a pen used when rendering the drawing
 */
public interface IDrawingPen {

    /**
     * @return the pen's name, as defined in the pen's plugin or set by the user
     */
    String getName();

    /**
     * @return the ARGB value of the pen, used when rendering
     */
    int getARGB();
}
