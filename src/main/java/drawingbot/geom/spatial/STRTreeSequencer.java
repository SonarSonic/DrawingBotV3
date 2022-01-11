package drawingbot.geom.spatial;

import drawingbot.DrawingBotV3;
import drawingbot.utils.LazyTimer;
import drawingbot.utils.ProgressCallback;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.index.strtree.ItemDistance;
import org.locationtech.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class STRTreeSequencer<T> implements ItemDistance {

    public List<T> cities;
    public boolean[] sorted;
    public double allowableDistance;

    public STRtree tree;
    public List<STRNode<T>> nodes;

    public ProgressCallback progressCallback;

    public STRTreeSequencer(List<T> cities, double allowableDistance) {
        this.cities = cities;
        this.sorted = new boolean[cities.size()];
        this.allowableDistance = allowableDistance;
        build();
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public void build() {
        this.tree = new STRtree();
        this.nodes = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            T city = cities.get(i);
            Coordinate startCoordinate = getStartCoordinateFromGeometry(city);
            Envelope startEnvelope = new Envelope(startCoordinate.x, startCoordinate.x, startCoordinate.y, startCoordinate.y);
            Coordinate endCoordinate = getEndCoordinateFromGeometry(city);
            Envelope endEnvelope = new Envelope(endCoordinate.x, endCoordinate.x, endCoordinate.y, endCoordinate.y);

            STRNode<T> node = new STRNode<>(i, city, startCoordinate, startEnvelope, endCoordinate, endEnvelope);
            tree.insert(startEnvelope, node);
            nodes.add(node);
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
                tree.insert(node.startEnvelope, node);
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
            STRNode<T> next = findNext(last.reverse());
            consumer.accept(last, next);
            last = next;
            sorted[last.index] = true;
            sortedCount++;

            if (progressCallback != null) {
                progressCallback.updateProgress((float) sortedCount / cities.size());
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
        Object[] neighbours = tree.nearestNeighbour(last.startEnvelope, last, this, 150);
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

        if(next == null){
            rebuild();
            return findNext(last);
        }

        return next;
    }

    public STRNode<T> findNextBruteForce(STRNode<T> last) {
        double measuredDistance = -1;
        STRNode<T> next = null;

        for (STRNode<T> node : nodes) {
            if (node.index != last.index && !sorted[node.index]) {
                double dist = distance(last, node);
                if (measuredDistance == -1 || dist < measuredDistance) {
                    measuredDistance = dist;
                    next = node;
                    if (measuredDistance <= allowableDistance) {
                        break;
                    }
                }
            }
        }
        return next;
    }

    protected abstract Coordinate getStartCoordinateFromGeometry(T geometry);

    protected abstract Coordinate getEndCoordinateFromGeometry(T geometry);

    @Override
    public double distance(ItemBoundable item1, ItemBoundable item2) {
        STRNode<T> node1 = (STRNode<T>) item1.getItem();
        STRNode<T> node2 = (STRNode<T>) item2.getItem();
        return distance(node1, node2);
    }

    public double distance(STRNode<T> node1, STRNode<T> node2) {
        return node1.startCoord.distance(node2.startCoord);
    }

    public static class IGeometry extends STRTreeSequencer<drawingbot.geom.basic.IGeometry> {

        public IGeometry(List<drawingbot.geom.basic.IGeometry> lineStrings, double allowableDistance) {
            super(lineStrings, allowableDistance);
        }

        @Override
        protected Coordinate getStartCoordinateFromGeometry(drawingbot.geom.basic.IGeometry geometry) {
            return geometry.getOriginCoordinate();
        }

        @Override
        protected Coordinate getEndCoordinateFromGeometry(drawingbot.geom.basic.IGeometry geometry) {
            return geometry.getOriginCoordinate();
        }
    }

}
