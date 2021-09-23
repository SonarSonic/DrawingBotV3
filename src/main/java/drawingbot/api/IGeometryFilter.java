package drawingbot.api;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.basic.IGeometry;

public interface IGeometryFilter {

    boolean filter(IGeometry geometry, ObservableDrawingPen pen);

}
