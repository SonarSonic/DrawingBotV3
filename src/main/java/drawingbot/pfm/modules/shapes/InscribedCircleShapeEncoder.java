package drawingbot.pfm.modules.shapes;

import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GShape;
import drawingbot.pfm.PFMModular;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class InscribedCircleShapeEncoder extends ShapeEncoder {

    public double circleSize = 0.8D;

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        pfmModular.task.updateMessage("Calculating Inscribed Circles");
        for (int i = 0; i < positionEncoder.getGeometries().getNumGeometries(); i ++) {
            Geometry geometry = positionEncoder.getGeometries().getGeometryN(i);
            if (!(geometry instanceof Polygon || geometry instanceof MultiPolygon) || geometry.isEmpty()) {
                continue;
            }
            MaximumInscribedCircle circle = new MaximumInscribedCircle(geometry, 1);
            Point radius = circle.getRadiusPoint();
            Point centre = circle.getCenter();
            int diameter = (int)((radius.distance(centre)*2)*0.8D);

            pfmModular.task.addGeometry(new GEllipse((float)centre.getX() - diameter/2F, (float)centre.getY() - diameter/2F, diameter, diameter));
            pfmModular.updateShapeEncoderProgess(i, positionEncoder.getGeometries().getNumGeometries()-1);
        }
    }
}
