package drawingbot.pfm.modules.shapes;

import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.*;

public class InscribedCircleShapeEncoder extends ShapeEncoder {

    public double circleSize = 0.8D;

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        pfmModular.task.updateMessage("Calculating Inscribed Circles");
        for (int i = 0; i < positionEncoder.getGeometries().getNumGeometries(); i ++) {
            Geometry geometry = positionEncoder.getGeometries().getGeometryN(i);
            Geometry intersectionGeometry = geometry.intersection(pfmModular.task.clippingShape);
            if(!intersectionGeometry.isEmpty()) {
                geometry = intersectionGeometry;
            }
            if (!(geometry instanceof Polygon || geometry instanceof MultiPolygon) || geometry.isEmpty()) {
                continue;
            }

            MaximumInscribedCircle circle = new MaximumInscribedCircle(geometry, 1);
            Point radius = circle.getRadiusPoint();
            Point centre = circle.getCenter();
            int diameter = Math.max(1, (int)((radius.distance(centre)*2)*circleSize));

            pfmModular.task.addGeometry(new GEllipse((float)centre.getX() - diameter/2F, (float)centre.getY() - diameter/2F, diameter, diameter), 1, -1);
            pfmModular.updateShapeEncoderProgess(i, positionEncoder.getGeometries().getNumGeometries()-1);

            if(pfmModular.task.isFinished()){
                break;
            }
        }
    }
}
