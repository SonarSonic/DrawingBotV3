package drawingbot.geom.converters;

import drawingbot.geom.shapes.GCubicCurve;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Shape;

public class JFXCubicCurveConverter implements JFXGeometryConverter {

    @Override
    public boolean canConvert(IGeometry geometry) {
        return geometry instanceof GCubicCurve;
    }

    @Override
    public Shape convert(IGeometry geometry) {
        GCubicCurve curve = (GCubicCurve) geometry;
        CubicCurve jfxCubicCurve = new CubicCurve();
        update(jfxCubicCurve, curve);
        return jfxCubicCurve;
    }

    @Override
    public boolean canUpdate(Shape shape) {
        return shape instanceof CubicCurve;
    }

    @Override
    public void update(Shape shape, IGeometry geometry) {
        GCubicCurve curve = (GCubicCurve) geometry;
        CubicCurve jfxCubicCurve = (CubicCurve) shape;
        jfxCubicCurve.setStartX(curve.getX1());
        jfxCubicCurve.setStartY(curve.getY1());
        jfxCubicCurve.setControlX1(curve.getCtrlX1());
        jfxCubicCurve.setControlY1(curve.getCtrlY1());
        jfxCubicCurve.setControlX2(curve.getCtrlX2());
        jfxCubicCurve.setControlY2(curve.getCtrlY2());
        jfxCubicCurve.setEndX(curve.getX1());
        jfxCubicCurve.setEndY(curve.getY1());
    }
}
