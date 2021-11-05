package drawingbot.geom.tree;

import drawingbot.geom.tsp.TSPNode;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NodeGraph {

    public List<Coordinate> points;
    public LinkedList<TSPNode> nodes = new LinkedList<>();

    public NodeGraph(List<Coordinate> coordinateList){
        assert !coordinateList.isEmpty();
        points = List.copyOf(coordinateList);
        TSPNode lastNode = null;
        for(int i = 0; i < coordinateList.size(); i ++){
            TSPNode nextNode = new TSPNode(i);
            nextNode.prev = lastNode;
            if(lastNode != null){
                lastNode.next = nextNode;
            }
            nodes.add(nextNode);
            lastNode = nextNode;
        }
        assert lastNode != null;
        TSPNode firstNode = nodes.get(0);
        lastNode.next = firstNode;
        firstNode.prev = lastNode;
    }


    /*

    public void updateOrderedTour(){
        List<TSPNode> orderedTour = new ArrayList<>();
        List<NodeEdge> orderedEdges = new ArrayList<>();

        TSPNode lastNode = null;
        for(TSPNode node : tour){
            orderedTour.add(node);
            if(lastNode != null){
                orderedEdges.add(new NodeEdge(node, lastNode));
            }
            lastNode = node;
        }
        orderedEdges.add(new NodeEdge(lastNode, orderedTour.get(0)));

        this.orderedEdges = orderedEdges;
        this.orderedNodes = orderedTour;
    }
     */

}
