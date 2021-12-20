package drawingbot.geom.spatial;

import drawingbot.geom.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.linearref.LinearGeometryBuilder;

import java.util.List;

@Deprecated
public class TSPSequencerLineString extends TSPSequencer<LineString> {

    public TSPSequencerLineString(List<LineString> lineStrings, double allowableDistance) {
        super(lineStrings, allowableDistance);
    }

    public void mergeLine(LinearGeometryBuilder builder, LineString string) {
        for (Coordinate coordinate : string.getCoordinates()) {
            builder.add(coordinate, false);
        }
    }

    public List<LineString> lineMerge() {
        LinearGeometryBuilder builder = new LinearGeometryBuilder(GeometryUtils.factory);
        builder.setIgnoreInvalidLines(true);
        LineString first = cities.get(0);
        sorted[0] = true;
        sortedCount++;
        mergeLine(builder, first);

        while (sortedCount < cities.size()) {
            LineString nearest = getNearestGeometry(builder.getLastCoordinate());
            if (measuredDistance > allowableDistance) {
                builder.endLine();
            }
            mergeLine(builder, nearest);

            if (progressCallback != null) {
                progressCallback.updateProgress((float) sortedCount / cities.size());
                progressCallback.updateMessage(sortedCount + " / " + cities.size());
            }
        }

        return LinearComponentExtracter.getLines(builder.getGeometry(), true);
    }

    @Override
    protected Coordinate getCoordinateFromCity(LineString geometry) {
        return geometry.getCoordinateN(geometry.getNumPoints() - 1);
    }

    @Override
    protected LineString getNearestGeometry(Coordinate point) {
        LineString nearest = null;
        measuredDistance = -1;
        boolean reversed = false;
        int index = 0;
        for (int i = 0; i < sorted.length; i++) {
            if (!sorted[i]) {
                LineString lineString = cities.get(i);
                Coordinate startCoord = lineString.getCoordinateN(0);
                double toStart = point.distance(startCoord);
                if (measuredDistance == -1 || toStart < measuredDistance) {
                    measuredDistance = toStart;
                    nearest = lineString;
                    reversed = false;
                    index = i;
                    if (measuredDistance <= allowableDistance) {
                        break;
                    }
                }
                Coordinate endCoord = lineString.getCoordinateN(lineString.getNumPoints() - 1);
                double toEnd = point.distance(endCoord);
                if (measuredDistance == -1 || toEnd < measuredDistance) {
                    measuredDistance = toEnd;
                    nearest = lineString;
                    reversed = true;
                    index = i;
                    if (measuredDistance <= allowableDistance) {
                        break;
                    }
                }

            }
        }
        sorted[index] = true;
        sortedCount++;
        return reversed ? nearest.reverse() : nearest;
    }

}
