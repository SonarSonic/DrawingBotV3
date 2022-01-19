package drawingbot.api;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.presets.JsonAdapterDrawingPen;

/**
 * {@link IDrawingPen} defines a pen used when rendering the drawing
 */
@JsonAdapter(JsonAdapterDrawingPen.class)
public interface IDrawingPen {

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
     * @return if the user has currently selected this pen for use
     */
    boolean isEnabled();

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
    int getARGB();

    /**
     * @return the stroke size for rendering the pen
     */
    float getStrokeSize();

    /**
     * @return the distribution weight for affecting the percentage of lines per pen
     */
    int getDistributionWeight();

}
