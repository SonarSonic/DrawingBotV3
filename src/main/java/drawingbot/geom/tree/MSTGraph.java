package drawingbot.geom.tree;

import drawingbot.geom.tsp.TSPNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MSTGraph {

    public MinimumSpanningTree minimumSpanningTree;
    public LinkedHashMap<Integer, TSPNode> nodes = new LinkedHashMap<>();
    public List<NodeEdge> edges = new ArrayList<>();

    public MSTGraph(MinimumSpanningTree minimumSpanningTree){
        this.minimumSpanningTree = minimumSpanningTree;
        TSPNode lastNode = null;
        for(int i = 0; i < minimumSpanningTree.coordinateList.size(); i ++){
            TSPNode nextNode = new TSPNode(i);
            nextNode.prev = lastNode;
            nextNode.vertexData = MinimumSpanningTree.getVertexData(i, minimumSpanningTree.vertices);
            assert nextNode.vertexData != null;
            nextNode.vertexData.node = nextNode;
            if(lastNode != null){
                lastNode.next = nextNode;
                edges.add(new NodeEdge(lastNode, nextNode));
            }
            nodes.put(i, nextNode);
            lastNode = nextNode;

        }
        assert lastNode != null;
        TSPNode firstNode = nodes.get(0);
        lastNode.next = firstNode;
        firstNode.prev = lastNode;
        edges.add(new NodeEdge(lastNode, firstNode));
    }


    public void updateTour(List<NodeEdge> nodeEdges){
        //edges.clear();
        //edges = nodeEdges;
        for(NodeEdge edge : nodeEdges){
            edge.start.next = edge.stop;
            edge.stop.prev = edge.start;
        }
    }

    public TSPNode getNextNode(TSPNode node){
        for(NodeEdge nodeEdge : edges){
            if(nodeEdge.start.id == node.id){
                return nodeEdge.stop;
            }
        }
        return null; //shouldn't happen
    }

    public TSPNode getPrevNode(TSPNode node){
        for(NodeEdge nodeEdge : edges){
            if(nodeEdge.stop.id == node.id){
                return nodeEdge.start;
            }
        }
        return null; //shouldn't happen
    }




}
