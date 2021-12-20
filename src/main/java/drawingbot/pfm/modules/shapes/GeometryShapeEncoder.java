package drawingbot.pfm.modules.shapes;

import drawingbot.api.IPixelData;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.GPath;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;

import java.util.List;

public class GeometryShapeEncoder extends ShapeEncoder {

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        pfmModular.task.updateMessage("Building Geometry");
        if(positionEncoder.getGeometries().getNumGeometries() == 0){
            return;
        }
        LineDissolver dissolver = new LineDissolver();

        for (int i = 0; i < positionEncoder.getGeometries().getNumGeometries(); i ++) {
            Geometry geometry = positionEncoder.getGeometries().getGeometryN(i);
            Geometry intersectionGeometry = geometry.intersection(pfmModular.task.clippingShape);
            if(!intersectionGeometry.isEmpty()){
                geometry = intersectionGeometry;
            }else if(!geometry.isValid() || geometry.isEmpty()){
                geometry = null;
            }
            if(geometry != null){
                dissolver.add(geometry);
            }
        }

        List<LineString> list = LinearComponentExtracter.getLines(dissolver.getResult(), true);
        int index = 0;
        for(LineString string : list){
            GPath path = GeometryUtils.geometryToGPath(string, null);
            pfmModular.task.addGeometry(path);
            pfmModular.updateShapeEncoderProgess(index, list.size());
            index++;
        }
    }
}
