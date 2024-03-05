package drawingbot.api;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;

public interface IGeometryFilter {

    IGeometryFilter BYPASS_FILTER = (drawing, geometry, pen) -> true;
    IGeometryFilter DEFAULT_FILTER  = (drawing, geometry, pen) -> pen.isEnabled();
    IGeometryFilter DEFAULT_VIEW_FILTER = (drawing, geometry, pen) -> pen.isEnabled() && (geometry.getGeometryIndex() >= drawing.getDisplayedShapeMin() && geometry.getGeometryIndex() < drawing.getDisplayedShapeMax());

    boolean filter(PlottedDrawing drawing, IGeometry geometry, ObservableDrawingPen pen);

}
