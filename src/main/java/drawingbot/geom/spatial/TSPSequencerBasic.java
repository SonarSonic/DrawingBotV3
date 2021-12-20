package drawingbot.geom.spatial;

import org.locationtech.jts.geom.Coordinate;

import java.util.List;

@Deprecated
public abstract class TSPSequencerBasic<T> extends TSPSequencer<T> {

    public TSPSequencerBasic(List<T> lineStrings, double allowableDistance) {
        super(lineStrings, allowableDistance);
    }

    @Override
    protected T getNearestGeometry(Coordinate point) {
        T nearest = null;
        measuredDistance = -1;
        int index = 0;
        for (int i = 0; i < sorted.length; i++) {
            if (!sorted[i]) {
                T geometry = cities.get(i);
                Coordinate startCoord = getCoordinateFromCity(geometry);
                double toStart = point.distance(startCoord);
                if (measuredDistance == -1 || toStart < measuredDistance) {
                    measuredDistance = toStart;
                    nearest = geometry;
                    index = i;
                    if (measuredDistance <= allowableDistance) {
                        break;
                    }
                }
            }
        }
        sorted[index] = true;
        sortedCount++;
        return nearest;
    }
}
