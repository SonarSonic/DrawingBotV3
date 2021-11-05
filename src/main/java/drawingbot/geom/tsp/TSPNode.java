package drawingbot.geom.tsp;

import drawingbot.geom.tree.MSTVertex;
import drawingbot.geom.tree.MinimumSpanningTree;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;

import java.util.Map;

public class TSPNode {

    //original input id
    public int id;

    //the adjacent nodes
    public TSPNode prev, next;

    // vertex data from the MST
    public MSTVertex vertexData;

    //calculating data only
    public TSPNode mark;
    public long beta;
    public TSPNode dad;

    public TSPNode(int id){
        this.id = id;
    }

    public TSPNode getNearestNode(){
        return getNearestEntry().getKey().node;
    }

    public Map.Entry<MSTVertex, QuadEdge> getNearestEntry(){
        double minLength = Double.MAX_VALUE;
        Map.Entry<MSTVertex, QuadEdge> entry = null;

        for(Map.Entry<MSTVertex, QuadEdge> edgeEntry : vertexData.edges.entrySet()){
            double length = MinimumSpanningTree.getLength(edgeEntry.getValue());
            if(length < minLength){
                minLength = length;
                entry = edgeEntry;
            }
        }
        return entry;
    }


    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
