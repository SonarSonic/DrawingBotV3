package drawingbot.geom.snapping;

import javafx.beans.property.SimpleBooleanProperty;

public interface ISnappingGuide {

    /**
     * @param x the input x point
     * @param radius the effective radius for snapping points
     * @return the snapped X value or NAN  is out of the guides snapping range
     */
    double snapToX(double x, double radius, double scale);

    /**
     * @param y the input y point
     * @param radius the effective radius for snapping points
     * @return the snapped Y value or NAN  is out of the guides snapping range
     */
    double snapToY(double y, double radius, double scale);

    boolean isEnabled();

    SimpleBooleanProperty enabledProperty();

    void setEnabled(boolean enabled);

}
