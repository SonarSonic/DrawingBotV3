package drawingbot.api;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;

public interface IGeometryFilter {

    IGeometryFilter BYPASS_FILTER = (drawing, geometry, pen) -> true;
    IGeometryFilter DEFAULT_EXPORT_FILTER = (drawing, geometry, pen) -> pen.isEnabled() && (!DrawingBotV3.INSTANCE.exportRange.get() || geometry.getGeometryIndex() > drawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= drawing.getDisplayedShapeMax());
    IGeometryFilter DEFAULT_VIEW_FILTER = (drawing, geometry, pen) -> pen.isEnabled() && (geometry.getGeometryIndex() > drawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= drawing.getDisplayedShapeMax());
    IGeometryFilter SELECTED_PEN_FILTER = (drawing, geometry, pen) -> DEFAULT_VIEW_FILTER.filter(drawing, geometry, pen) && (DrawingBotV3.INSTANCE.controller.drawingSetsController.getSelectedPen() == null || DrawingBotV3.INSTANCE.controller.drawingSetsController.getSelectedPen() == pen);


    boolean filter(PlottedDrawing drawing, IGeometry geometry, ObservableDrawingPen pen);

}
