package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPointFilter;

public class PlottedPoint {

    public static IPointFilter DEFAULT_FILTER = (point, pen) -> pen.isEnabled();
    public static IPointFilter SELECTED_PEN_FILTER = (point, pen) -> pen.isEnabled() && (DrawingBotV3.INSTANCE.controller.getSelectedPen() == null || DrawingBotV3.INSTANCE.controller.getSelectedPen().penNumber.get() == pen.penNumber.get());

    public int pathIndex;
    public int pen_number;
    public float x1, y1;
    public Integer rgba;

    public PlottedPoint(int pathIndex, int pen_number, float x1, float y1) {
        this.pathIndex = pathIndex;
        this.pen_number = pen_number;
        this.x1 = x1;
        this.y1 = y1;
    }
}