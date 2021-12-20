package drawingbot.api;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottedDrawing;

public interface IGeometryFilter {

    boolean filter(PlottedDrawing drawing, IGeometry geometry, ObservableDrawingPen pen);

}
