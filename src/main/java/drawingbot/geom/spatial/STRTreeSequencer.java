package drawingbot.geom.spatial;

import drawingbot.DrawingBotV3;
import drawingbot.api.IProgressCallback;
import drawingbot.utils.LazyTimer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.ItemDistance;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class STRTreeSequencer<T> {

    public Collection<T> cities;
    public boolean[] sorted;
    public double allowableDistance;

    public STRtree tree;
    public List<STRNode<T>> nodes;

    public IProgressCallback progressCallback;

    public STRTreeSequencer(Collection<T> cities, double allowableDistance) {
        this.cities = cities;
        this.sorted = new boolean[cities.size()];
        this.allowableDistance = allowableDistance;
        build();
    }

    public void setProgressCallback(IProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public void build() {
        this.tree = new STRtree();
        this.nodes = new ArrayList<>();

        int i = 0;
        for (T city : cities) {
            Coordinate startCoordinate = getStartCoordinateFromCity(city);
            Coordinate endCoordinate = getEndCoordinateFromCity(city);
            Envelope envelope = new Envelope(startCoordinate, startCoordinate);

            STRNode<T> node = new STRNode<>(i, city, startCoordinate, endCoordinate, envelope, this::reverseCity);
            tree.insert(envelope, node);
            nodes.add(node);
            i++;
        }
        LazyTimer buildTimer = new LazyTimer();
        buildTimer.start();
        this.tree.build();
        buildTimer.finish();
        DrawingBotV3.logger.info("Build: " + buildTimer.getElapsedTimeFormatted());
    }

    public void rebuild(){
        this.tree = new STRtree();
        for(STRNode<T> node : nodes){
            if(!sorted[node.index]){
                tree.insert(node.envelope, node);
            }
        }
        this.tree.build();
    }

    public void sequence(BiConsumer<STRNode<T>, STRNode<T>> consumer){
        if(cities.isEmpty()){
            return;
        }
        STRNode<T> last = nodes.get(0);
        consumer.accept(null, last);
        sorted[0] = true;
        int sortedCount = 1;

        while (sortedCount < cities.size()) {
            STRNode<T> next = findNext(last);
            tree.remove(next.envelope, next);
            consumer.accept(last, next);
            last = next;
            sorted[last.index] = true;
            sortedCount++;

            if (progressCallback != null) {
                progressCallback.updateProgress(sortedCount, cities.size());
                progressCallback.updateMessage(sortedCount + " / " + cities.size());
            }
        }
    }

    public List<T> sort(){
        List<STRNode<T>> sortedList = new ArrayList<>();
        sequence((last, next) -> sortedList.add(next));
        return unwrap(sortedList);
    }

    public List<T> unwrap(List<STRNode<T>> nodes) {
        List<T> cities = new ArrayList<>();
        nodes.forEach(n -> cities.add(n.city));
        return cities;
    }

    public STRNode<T> findNext(STRNode<T> last) {
        Envelope searchEnvelope = new Envelope(last.endCoord.x, last.endCoord.x, last.endCoord.y, last.endCoord.y);
        Object[] neighbours = tree.nearestNeighbour(searchEnvelope, last, getDistanceMetricWithOrigin(last), 150);

        STRNode<T> next = null;
        for (Object obj : neighbours) {
            if(obj instanceof STRNode){
                STRNode<T> strNode = (STRNode<T>) obj;
                if (!sorted[strNode.index]) {
                    next = strNode;
                    break;
                }
            }
        }

        if(next == null || next.index == last.index ||sorted[next.index]){
            rebuild();
            return findNext(last);
        }

        if(canReverse(next.city) && last.endCoord.distance(next.startCoord) > last.endCoord.distance(next.endCoord)){
            next.reverse();
        }

        return next;
    }

    public STRNode<T> findNextBruteForce(STRNode<T> last) {
        double measuredDistance = -1;
        STRNode<T> next = null;

        for (STRNode<T> node : nodes) {
            if (node.index != last.index && !sorted[node.index]) {
                double dist = distanceSTRNodes(last, node);
                if (measuredDistance == -1 || dist < measuredDistance) {
                    measuredDistance = dist;
                    next = node;
                    if (measuredDistance <= allowableDistance) {
                        break;
                    }
                }
            }
        }

        if(next != null && canReverse(next.city) && last.endCoord.distance(next.startCoord) > last.endCoord.distance(next.endCoord)){
            next.reverse();
        }

        return next;
    }

    protected abstract Coordinate getStartCoordinateFromCity(T geometry);

    protected abstract Coordinate getEndCoordinateFromCity(T geometry);

    public ItemDistance getDistanceMetricWithOrigin(STRNode<T> queryOrigin){
        return (item1, item2) -> {
            STRNode<T> node1 = (STRNode<T>) item1.getItem();
            STRNode<T> node2 = (STRNode<T>) item2.getItem();

            STRNode<T> last = node1 == queryOrigin ? node1 : node2;
            STRNode<T> next = last == node1 ? node2 : node1;

            return distanceSTRNodes(last, next);
        };
    }

    public double distanceSTRNodes(STRNode<T> last, STRNode<T> next) {
        boolean canReverse = canReverse(next.city);
        return Math.min(last.endCoord.distance(next.startCoord), canReverse ? last.endCoord.distance(next.endCoord) : Double.MAX_VALUE);
    }

    public T reverseCity(T city){
        return city;
    }

    public boolean canReverse(T city){
        return false;
    }

}
