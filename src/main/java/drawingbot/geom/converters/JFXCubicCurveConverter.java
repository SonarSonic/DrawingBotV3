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
        jfxCubicCurve.setStartX(curve.x1);
        jfxCubicCurve.setStartY(curve.y1);
        jfxCubicCurve.setControlX1(curve.ctrlx1);
        jfxCubicCurve.setControlY1(curve.ctrly1);
        jfxCubicCurve.setControlX2(curve.ctrlx2);
        jfxCubicCurve.setControlY2(curve.ctrly2);
        jfxCubicCurve.setEndX(curve.x1);
        jfxCubicCurve.setEndY(curve.y1);
    }
}
