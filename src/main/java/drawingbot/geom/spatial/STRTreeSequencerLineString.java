package drawingbot.geom.spatial;

import drawingbot.geom.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.linearref.LinearGeometryBuilder;

import java.util.List;


public class STRTreeSequencerLineString extends STRTreeSequencer<LineString>{

    public STRTreeSequencerLineString(List<LineString> cities, double allowableDistance) {
        super(cities, allowableDistance);
    }

    @Override
    public LineString reverseCity(LineString city) {
        return city.reverse();
    }

    @Override
    public boolean canReverse(LineString city) {
        return true;
    }

    public List<LineString> merge(){
        LinearGeometryBuilder builder = new LinearGeometryBuilder(GeometryUtils.factory);
        builder.setIgnoreInvalidLines(true);
        sequence((last, next) -> {

            if(last != null && distanceSTRNodes(last, next) > allowableDistance){
                builder.endLine();
            }

            for (Coordinate coordinate : next.city.getCoordinates()) {
                builder.add(coordinate, false);
            }

        });
        return LinearComponentExtracter.getLines(builder.getGeometry(), true);
    }

    @Override
    protected Coordinate getStartCoordinateFromCity(LineString geometry) {
        return geometry.getCoordinateN(0);
    }

    @Override
    protected Coordinate getEndCoordinateFromCity(LineString geometry) {
        return geometry.getCoordinateN(geometry.getNumPoints() - 1);
    }
}