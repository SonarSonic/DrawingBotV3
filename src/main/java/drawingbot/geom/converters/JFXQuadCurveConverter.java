package drawingbot.geom.converters;

import drawingbot.geom.shapes.GQuadCurve;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;

public class JFXQuadCurveConverter implements JFXGeometryConverter {

    @Override
    public boolean canConvert(IGeometry geometry) {
        return geometry instanceof GQuadCurve;
    }

    @Override
    public Shape convert(IGeometry geometry) {
        GQuadCurve curve = (GQuadCurve) geometry;
        QuadCurve jfxQuadCurve = new QuadCurve();
        update(jfxQuadCurve, curve);
        return jfxQuadCurve;
    }

    @Override
    public boolean canUpdate(Shape shape) {
        return shape instanceof QuadCurve;
    }

    @Override
    public void update(Shape shape, IGeometry geometry) {
        GQuadCurve curve = (GQuadCurve) geometry;
        QuadCurve jfxQuadCurve = (QuadCurve) shape;
        jfxQuadCurve.setStartX(curve.x1);
        jfxQuadCurve.setStartY(curve.y1);
        jfxQuadCurve.setControlX(curve.ctrlx);
        jfxQuadCurve.setControlY(curve.ctrly);
        jfxQuadCurve.setEndX(curve.x1);
        jfxQuadCurve.setEndY(curve.y1);
    }
}
