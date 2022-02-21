package drawingbot.geom.spatial;

import drawingbot.utils.ProgressCallback;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Not a very good approach to the Travelling Salesman Problem but suits the path finding algorithms currently in use, could definitely be improved, possibly with a nearest neighbour search combined with a KDTree!
 */
@Deprecated
public abstract class TSPSequencer<T> {

    public List<T> cities;
    public boolean[] sorted;
    public int sortedCount = 0;

    public double measuredDistance;
    public double allowableDistance;

    public ProgressCallback progressCallback;

    public TSPSequencer(List<T> cities, double allowableDistance) {
        this.cities = cities;
        this.sorted = new boolean[cities.size()];
        this.allowableDistance = allowableDistance;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public List<T> sort() {
        List<T> sortedList = new ArrayList<>();
        if (cities.isEmpty()) {
            return sortedList;
        }
        T first = cities.get(0);
        sorted[0] = true;
        sortedCount++;
        sortedList.add(first);

        while (sortedCount < cities.size()) {
            T last = sortedList.get(sortedList.size() - 1);
            Coordinate endCoord = getCoordinateFromCity(last);
            T nearest = getNearestGeometry(endCoord);
            sortedList.add(nearest);

            if (progressCallback != null) {
                progressCallback.updateProgress(sortedCount, cities.size());
                progressCallback.updateMessage(sortedCount + " / " + cities.size());
            }
        }
        return sortedList;
    }

    protected abstract Coordinate getCoordinateFromCity(T geometry);

    protected abstract T getNearestGeometry(Coordinate point);

}
