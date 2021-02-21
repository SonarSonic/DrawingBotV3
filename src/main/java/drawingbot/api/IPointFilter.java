package drawingbot.api;

import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottedPoint;

public interface IPointFilter {

    boolean filter(PlottedPoint point, ObservableDrawingPen pen);

}
