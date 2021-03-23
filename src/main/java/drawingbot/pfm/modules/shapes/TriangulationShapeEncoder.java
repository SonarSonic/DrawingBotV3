package drawingbot.pfm.modules.shapes;

import drawingbot.api.IPixelData;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.GShape;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.ArrayList;
import java.util.List;

public class TriangulationShapeEncoder extends ShapeEncoder {

    public boolean connectCorners = false;

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        pfmModular.task.updateMessage("Calculating Triangulation");
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();

        if(connectCorners){
            List<Coordinate> coordinates = new ArrayList<>(positionEncoder.getCoordinates());
            coordinates.add(new CoordinateXY(0, 0));
            coordinates.add(new CoordinateXY(0, data.getHeight()-1));
            coordinates.add(new CoordinateXY(data.getWidth()-1, 0));
            coordinates.add(new CoordinateXY(data.getWidth()-1, data.getHeight()-1));
            builder.setSites(coordinates);
        }else{
            builder.setSites(positionEncoder.getCoordinates());
        }

        GeometryCollection collection = (GeometryCollection) builder.getSubdivision().getEdges(GeometryUtils.factory);

        ShapeWriter shapeWriter = new ShapeWriter();
        for (int g = 0; g < collection.getNumGeometries(); g++) {
            Geometry geometry = collection.getGeometryN(g);
            List list = LinearComponentExtracter.getLines(geometry, true);
            for(Object obj : list){
                pfmModular.task.addGeometry(new GShape(shapeWriter.toShape((LineString)obj)));
            }
            pfmModular.updateShapeEncoderProgess(g, collection.getNumGeometries()-1);
        }
    }
}
