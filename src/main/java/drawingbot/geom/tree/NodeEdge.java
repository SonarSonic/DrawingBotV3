package drawingbot.geom.tree;

import drawingbot.geom.tsp.TSPNode;

public class NodeEdge {

    public TSPNode start;
    public TSPNode stop;

    public NodeEdge(TSPNode origin, TSPNode stop) {
        this.start = origin;
        this.stop = stop;
    }

    public boolean isEdgePrev(NodeEdge edge){
        return edge.stop.id == start.id;
    }

    public boolean isEdgeNext(NodeEdge edge){
        return edge.start.id == stop.id;
    }

    @Override
    public int hashCode() {
        //int min = Math.min(origin.id, dest.id);
        //int max = Math.max(origin.id, dest.id);
        return start.id * 31 + stop.id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj instanceof NodeEdge){
            NodeEdge edge = (NodeEdge) obj;
            return (edge.start.id == start.id && edge.stop.id == stop.id);
        }
        return false;
    }
}
