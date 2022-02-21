package drawingbot.geom.spatial;

import drawingbot.geom.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.linearref.LinearGeometryBuilder;

import java.util.ArrayList;
import java.util.List;

public class STRTreeSequencerLineString extends STRTreeSequencer<LineString>{

    public List<STRNode<LineString>> endNodes;

    public STRTreeSequencerLineString(List<LineString> cities, double allowableDistance) {
        super(cities, allowableDistance);
    }

    @Override
    public void build() {
        this.tree = new STRtree();
        this.nodes = new ArrayList<>();
        this.endNodes = new ArrayList<>();

        int i = 0;
        for (LineString city : cities) {
            Coordinate startCoordinate = getStartCoordinateFromCity(city);
            Envelope startEnvelope = new Envelope(startCoordinate.x, startCoordinate.x, startCoordinate.y, startCoordinate.y);
            Coordinate endCoordinate = getEndCoordinateFromCity(city);
            Envelope endEnvelope = new Envelope(endCoordinate.x, endCoordinate.x, endCoordinate.y, endCoordinate.y);

            STRNode<LineString> startNode = new STRNode<>(i, city, startCoordinate, startEnvelope, endCoordinate, endEnvelope);
            STRNode<LineString> endNode = new STRNode<>(i, city.reverse(), endCoordinate, endEnvelope, startCoordinate, startEnvelope);

            tree.insert(startEnvelope, startNode);
            tree.insert(endEnvelope, endNode);
            nodes.add(startNode);
            endNodes.add(endNode);
            i++;
        }
        this.tree.build();
    }

    @Override
    public void rebuild(){
        this.tree = new STRtree();
        for(STRNode<LineString> node : nodes){
            if(!sorted[node.index]){
                tree.insert(node.startEnvelope, node);
            }
        }
        for(STRNode<LineString> node : endNodes){
            if(!sorted[node.index]){
                tree.insert(node.startEnvelope, node);
            }
        }
        this.tree.build();
    }

    public List<LineString> merge(){
        LinearGeometryBuilder builder = new LinearGeometryBuilder(GeometryUtils.factory);
        builder.setIgnoreInvalidLines(true);
        sequence((last, next) -> {

            if(last != null && distance(last, next) > allowableDistance){
                builder.endLine();
            }

            for (Coordinate coordinate : next.city.getCoordinates()) {
                builder.add(coordinate, false);
            }

        });
        return LinearComponentExtracter.getLines(builder.getGeometry(), true);
    }


    @Override
    public STRNode<LineString> findNextBruteForce(STRNode<LineString> last) {
        double measuredDistance = -1;
        STRNode<LineString> next = null;

        for (int i = 0; i < nodes.size(); i++) {
            if(!sorted[i] && i != last.index){
                STRNode<LineString> startNode = nodes.get(i);
                if (startNode.index != last.index) {
                    double dist = distance(last, startNode);
                    if (measuredDistance == -1 || dist < measuredDistance) {
                        measuredDistance = dist;
                        next = startNode;
                        if (measuredDistance <= allowableDistance) {
                            break;
                        }
                    }
                }

                STRNode<LineString> endNode = endNodes.get(i);
                if (endNode.index != last.index) {
                    double dist = distance(last, endNode);
                    if (measuredDistance == -1 || dist < measuredDistance) {
                        measuredDistance = dist;
                        next = endNode;
                        if (measuredDistance <= allowableDistance) {
                            break;
                        }
                    }
                }
            }

        }
        return next;
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