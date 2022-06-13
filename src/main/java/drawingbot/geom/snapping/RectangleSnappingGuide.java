package drawingbot.geom.snapping;

import java.awt.geom.Rectangle2D;

public class RectangleSnappingGuide extends AbstractSnappingGuide {

    public Rectangle2D.Double rect;

    public RectangleSnappingGuide(double x, double y, double w, double h) {
        this(new Rectangle2D.Double(x, y, w, h));
    }

    public RectangleSnappingGuide(Rectangle2D.Double rect) {
        this.rect = rect;
    }

    @Override
    public double snapToX(double x, double radius, double scale) {
        double snapped = Double.MAX_VALUE;
        if (shouldSnapValue(x, rect.getMinX(), radius, scale)) {
            snapped = rect.getMinX();
        } else if (shouldSnapValue(x, rect.getMaxX(), radius, scale)) {
            snapped = rect.getMaxX();
        }
        return snapped;
    }

    @Override
    public double snapToY(double y, double radius, double scale) {
        double snapped = Double.MAX_VALUE;
        if (shouldSnapValue(y, rect.getMinY(), radius, scale)) {
            snapped = rect.getMinY();
        } else if (shouldSnapValue(y, rect.getMaxY(), radius, scale)) {
            snapped = rect.getMaxY();
        }
        return snapped;
    }
}
