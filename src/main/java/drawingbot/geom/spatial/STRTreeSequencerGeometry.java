package drawingbot.geom.spatial;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;


public class STRTreeSequencerGeometry extends STRTreeSequencer<IGeometry>{

    public STRTreeSequencerGeometry(List<IGeometry> cities, double allowableDistance) {
        super(cities, allowableDistance);
    }

    @Override
    public IGeometry reverseCity(IGeometry city) {
        return GeometryUtils.reverseGPath((GPath) city);
    }

    @Override
    public boolean canReverse(IGeometry city) {
        return city instanceof GPath;
    }

    public List<IGeometry> merge(){
        List<IGeometry> geometries = new ArrayList<>();
        sequence((last, next) -> {

            if(last == null || !(last.city instanceof GPath) || !(next.city instanceof GPath) || !(geometries.get(geometries.size()-1) instanceof GPath) || distanceSTRNodes(last, next) > allowableDistance){
                geometries.add(next.city);
            }else{
                GPath path = (GPath) geometries.get(geometries.size()-1);
                path.append(next.city.getAWTShape(), true);
            }
        });

        return geometries;
    }

    @Override
    protected Coordinate getStartCoordinateFromCity(IGeometry geometry) {
        return geometry.getOriginCoordinate();
    }

    @Override
    protected Coordinate getEndCoordinateFromCity(IGeometry geometry) {
        return geometry.getEndCoordinate();
    }
}
