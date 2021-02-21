package drawingbot.plotting;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class PlottedPath {

    public GeneralPath path;
    public BasicStroke stroke;
    public Color color;
    public int pointCount = 0;

    public PlottedPath(GeneralPath path, BasicStroke stroke, Color color) {
        this.path = path;
        this.stroke = stroke;
        this.color = color;
    }
}
