package drawingbot.api;

/**
 * {@link IDrawingPen} defines a pen used when rendering the drawing
 */
public interface IDrawingPen {

    /**
     * @param pfmARGB the line's colour as configured by the PFM, won't be null
     * @return returns the custom ARGB value to use when rendering a pen
     */
    default int getCustomARGB(int pfmARGB){
        return getCustomARGB();
    }

    /**
     * @return the pens unique name, unique to each pen
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
     * @return the pen's name, as defined in the pen's plugin or set by the user
     */
    String getName();

    /**
     * @return the ARGB value of the pen, used when rendering
     */
    int getCustomARGB();

    /**
     * @return the stroke size for rendering the pen
     */
    float getStrokeSize();

    /**
     * @return the distribution weight for affecting the percentage of lines per pen
     */
    int getDistributionWeight();

}
