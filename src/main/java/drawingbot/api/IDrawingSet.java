package drawingbot.api;

import java.util.List;

/**
 * A collection of {@link IDrawingPen}s used when rendering the drawing
 */
public interface IDrawingSet<P extends IDrawingPen> {

    /**
     * @return the pens full name, unique to each pen
     */
    default String getCodeName(){
        return getType() + ":" + getName();
    }

    /**
     * @return the pens display name
     */
    default String getDisplayName(){
        return getType() + " " + getName();
    }

    /**
     * @return typically the pens manufacturer or custom sub type
     */
    String getType();

    /**
     * @return the drawing sets, as defined in the pen's plugin or set by the user
     */
    String getName();

    /**
     * @return all the {@link IDrawingPen}s in the {@link IDrawingSet}
     */
    List<P> getPens();

}
