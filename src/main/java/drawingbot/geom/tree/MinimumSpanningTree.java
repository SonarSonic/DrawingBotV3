package drawingbot.geom.tree;

import drawingbot.geom.tsp.TSPNode;
import drawingbot.utils.Utils;
import javafx.util.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeUtil;

import java.util.*;
import java.util.function.Consumer;

public class MinimumSpanningTree{

    //for 1-tree implementation
    public boolean create1Tree;
    public int[] dad;
    public MSTVertex firstVertex;
    public double[][] distanceTable;
    public double[][] nearnessTable;
    public double[][] betaTable;

    //provided
    public final int size;
    public List<Coordinate> coordinateList;

    //generated
    public List<MSTVertex> vertices;
    public List<MSTEdge> edges;

    //the order in which vertices were added to the tree
    public List<MSTVertex> orderedVertices;

    //progress
    public float progressPreparing = 0.0F;
    public float progressBuilding = 0.0F;

    public Consumer<Float> progressCallback = null;


    public MinimumSpanningTree(List<Coordinate> coordinateList, boolean create1Tree){
        this.size = coordinateList.size();
        this.dad = new int[size];
        this.create1Tree = create1Tree;
        this.coordinateList = coordinateList;
        this.edges = new ArrayList<>();
        this.orderedVertices = new ArrayList<>();
    }

    public void prepareTree(){

        //create the delaunay triangulation
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(coordinateList);
        QuadEdgeSubdivision quadEdgeSubdivision = builder.getSubdivision();

        ///create the vertex data list from all the coordinates
        vertices = new ArrayList<>();
        for(int i = 0 ; i < coordinateList.size(); i ++){
            vertices.add(new MSTVertex(i, coordinateList.get(i), new HashMap<>()));
        }

        List<QuadEdge> vertexUniqueEdges = quadEdgeSubdivision.getVertexUniqueEdges(true);
        Map<Integer, QuadEdge> edgeCandidateList = new HashMap<>();
        int edgeIDCount = 0;

        ///store all of the edges for each vertex
        int index = 0;
        for(QuadEdge edge : vertexUniqueEdges){
            MSTVertex origin = getVertexData(edge.orig().getCoordinate(), vertices);
            if(origin != null){
                List<QuadEdge> quadEdges = QuadEdgeUtil.findEdgesIncidentOnOrigin(edge);
                for(QuadEdge quadEdge : quadEdges){
                    if(quadEdge != null){
                        MSTVertex dest = getVertexData(quadEdge.dest().getCoordinate(), vertices);
                        if(dest != null){
                            origin.edges.put(dest, quadEdge);

                            if(quadEdge.getData() == null){
                                quadEdge.setData(new QuadEdgeData(edgeIDCount, origin, dest));
                                edgeCandidateList.put(edgeIDCount, quadEdge);
                                edgeIDCount++;
                            }
                        }
                    }
                }
            }
            if(shouldStop()){
                return;
            }
            index++;
            progressPreparing = (float)index / vertexUniqueEdges.size();
            if(progressCallback != null){
                progressCallback.accept(progressPreparing);
            }
        }

        //calculate all the lengths of the candidate edges
        for(int id = 0; id < edgeCandidateList.size(); id ++){
            QuadEdge quadEdge = edgeCandidateList.get(id);
            QuadEdgeData data = (QuadEdgeData) quadEdge.getData();
            data.length = Utils.distance(data.origin.coordX, data.origin.coordY, data.dest.coordX, data.dest.coordY);
        }

    }

    public void buildTree(){

        //set the vertex to start building the tree from, if we are creating a 1-tree, this vertex is the 2nd not the 1st.
        markVertexVisited(vertices.get(create1Tree ? 1 : 0));

        int visitedTally = create1Tree ? 1 : 0;

        ///iterate through all the unvisited coordinates adding the best edge each time
        while(visitedTally != vertices.size()){
            List<Pair<MSTVertex, QuadEdge>> bestEdges = new ArrayList<>();
            double bestLength = -1;
            double tolerance = create1Tree ? 0 : 1; // 1-tree only wants one edge

            for(MSTVertex vertex : vertices){
                if(vertex.isVisited){
                    for(Map.Entry<MSTVertex, QuadEdge> edgeCandidate : vertex.edges.entrySet()){
                        if(!edgeCandidate.getKey().isVisited && !isIncluded(edgeCandidate.getValue()) && (!create1Tree || edgeCandidate.getKey().id != 0)){
                            if(bestLength == -1 || getLength(edgeCandidate.getValue()) < bestLength + tolerance){
                                //if the length found is much better than the original best, wipe it and start again with a new best length
                                if(bestLength == -1  || getLength(edgeCandidate.getValue()) < bestLength - tolerance){
                                    bestEdges.clear();
                                    bestLength = getLength(edgeCandidate.getValue());
                                }

                                // 1-tree only wants one edge
                                if(create1Tree){
                                    bestEdges.clear();
                                }
                                bestEdges.add(new Pair<>(edgeCandidate.getKey(), edgeCandidate.getValue()));
                            }
                        }
                    }
                }
            }
            //if we failed to find any edges then the MST is finished
            if(bestEdges.isEmpty()){
                break;
            }

            //add the best edges to the MST
            for(Pair<MSTVertex, QuadEdge> bestEdge : bestEdges){
                if(!bestEdge.getKey().isVisited && !isIncluded(bestEdge.getValue())){ //check we didn't find the vertex / edge from multiple locations
                    QuadEdgeData data = (QuadEdgeData) bestEdge.getValue().getData();
                    setIncluded(bestEdge.getValue(), true);

                    markVertexVisited(data.origin);
                    markVertexVisited(data.dest);
                    visitedTally++;
                    createMSTEdge(data.origin, data.dest);
                }
            }

            //this process can take a long time, so make sure we have a callback, so using "reset" works
            if(shouldStop()){
                return;
            }

            progressBuilding = (float)visitedTally / vertices.size();

            if(progressCallback != null){
                progressCallback.accept(progressBuilding);
            }
        }

        if(create1Tree){
            MSTVertex vertex1  = vertices.get(0);

            double bestLength = Double.MAX_VALUE;
            Map.Entry<MSTVertex, QuadEdge> bestEdge1 = null;
            Map.Entry<MSTVertex, QuadEdge> bestEdge2 = null;

            for(Map.Entry<MSTVertex, QuadEdge> entry : vertex1.edges.entrySet()){
                if(getLength(entry.getValue()) < bestLength){
                    bestEdge2 = bestEdge1;
                    bestEdge1 = entry;
                }
            }

            assert bestEdge1 != null;
            assert bestEdge2 != null;

            createMSTEdge(vertex1, bestEdge1.getKey());
            createMSTEdge(vertex1, bestEdge2.getKey());
            markVertexVisited(vertex1);
            markVertexVisited(bestEdge2.getKey());
            firstVertex = vertex1;
        }

        progressBuilding = 1F;
    }

    public void createMSTEdge(MSTVertex origin, MSTVertex dest){
        MSTEdge edge = new MSTEdge(origin, dest);
        origin.mstEdgeMap.put(dest, edge);
        dest.mstEdgeMap.put(origin, edge);
        onEdgeCreated(edge);
        edges.add(edge);
        dad[dest.id] = origin.id;
    }

    public MSTEdge getEdge(int origin, int dest){
        MSTVertex originData = vertices.get(origin);
        MSTVertex destData = vertices.get(dest);
        return originData.mstEdgeMap.get(destData);
    }

    public void markVertexVisited(MSTVertex vertex){
        if(!vertex.isVisited){
            vertex.isVisited = true;
            vertex.rank = orderedVertices.size();
            orderedVertices.add(vertex);
        }
    }

    //// TSP HELPERS \\\\


    public void setupForLK(int neighbours){
        initDistanceTable();
        List<MSTVertex> sortableList = new ArrayList<>(vertices);

        for(MSTVertex origin : vertices){
            sortableList.sort(Comparator.comparingDouble(o -> distanceTable[o.id][origin.id]));
            origin.nearestVertices = new ArrayList<>(sortableList.subList(0, neighbours));
        }
    }

    public void setupForLKH(int neighbours){
        initDistanceTable();
        initNearnessTable();
        List<MSTVertex> sortableList = new ArrayList<>(vertices);

        for(MSTVertex origin : vertices){
            sortableList.sort(Comparator.comparingDouble(o -> nearnessTable[o.id][origin.id]));
            origin.nearestVertices = new ArrayList<>(sortableList.subList(0, neighbours));
        }
    }

    private void initDistanceTable() {
        double[][] distanceTable = new double[this.size][this.size];

        for(int i = 0; i < this.size; ++i) {
            for(int j = i + 1; j < this.size; ++j) {
                MSTVertex p1 = this.vertices.get(i);
                MSTVertex p2 = this.vertices.get(j);

                distanceTable[j][i] = distanceTable[i][j] = Math.sqrt(Math.pow(p2.coordX - p1.coordX, 2) + Math.pow(p2.coordY - p1.coordY, 2));
            }
        }
        this.distanceTable = distanceTable;
    }

    public void initNearnessTable() {
        double[][] betaTable = new double[size][size];

        for(int i = 1; i < this.size; i++) {
            MSTVertex originVertex = orderedVertices.get(i);
            int origin = originVertex.id;

            betaTable[origin][origin] = Double.MIN_VALUE;
            for(int j = i + 1; j < this.size; j++) {
                MSTVertex destVertex = orderedVertices.get(j);
                int dest = destVertex.id;

                betaTable[origin][dest] = betaTable[dest][origin] = Math.max(betaTable[dest][dad[dest]], distanceTable[dad[dest]][dest]);
            }
        }
        this.betaTable = betaTable;

        double[][] nearnessTable = new double[size][size];

        for(int i = 0; i < this.size; ++i) {
            for(int j = i + 1; j < this.size; ++j) {
                nearnessTable[j][i] = nearnessTable[i][j] = getNearness(j, i);
            }
        }

        this.nearnessTable = nearnessTable;
    }

    public double getNearness(int origin, int dest){
        if(origin == dest){
            return Double.MAX_VALUE;
        }
        MSTVertex originData = vertices.get(origin);
        MSTVertex destData = vertices.get(dest);

        //(a)
        // If (i,j) belongs to T, then T+(i,j) is equal to T.
        if(originData.mstEdgeMap.get(destData) != null){
            return 0;
        }
        //(b)
        // Otherwise, if (i,j) has 1 as end node (i = 1 Ú j = 1), then T+(i,j) is
        // obtained from T by replacing the longest of the two edges of T
        // incident to node 1 with (i,j).
        double cLength = distanceTable[origin][dest];

        if(origin == 0 || dest == 0){
            MSTVertex node1 = vertices.get(0);
            MSTEdge largestEdge = null;
            for(MSTEdge edge : node1.mstEdgeMap.values()){
                if(largestEdge == null || edge.length > largestEdge.length){
                    largestEdge = edge;
                }
            }
            assert largestEdge != null;
            return cLength - largestEdge.length;
        }

        // (c)
        // Otherwise, insert (i,j) in T. This creates a cycle containing (i,j)
        // in the spanning tree part of T. Then T+(i,j) is obtained by
        // removing the longest of the other edges on this cycle.
        return cLength - betaTable[origin][dest];
    }

    public List<TSPNode> createNodes(MinimumSpanningTree mst){
        List<TSPNode> nodes = new ArrayList<>();
        for(int i = 0; i < mst.coordinateList.size(); i ++) {
            TSPNode nextNode = new TSPNode(i);
            nextNode.id = i;
            nextNode.vertexData = MinimumSpanningTree.getVertexData(i, mst.vertices);
            assert nextNode.vertexData != null;
            nextNode.vertexData.node = nextNode;
            nodes.add(nextNode);
            nextNode.dad = vertices.get(dad[i]).node;
        }
        return nodes;
    }

    //// CALLBACKS \\\\

    public void onEdgeCreated(MSTEdge edge){}

    public boolean shouldStop(){
        return false;
    }

    //// UTILITIES \\\\

    public static double getLength(QuadEdge edge){
        return ((QuadEdgeData)edge.getData()).length;
    }

    public static boolean isIncluded(QuadEdge edge){
        return ((QuadEdgeData)edge.getData()).isIncluded;
    }

    public static void setIncluded(QuadEdge edge, boolean isIncluded){
        ((QuadEdgeData)edge.getData()).isIncluded = isIncluded;
    }

    public static MSTVertex getVertexData(Coordinate coordinate, List<MSTVertex> vertexDataList){
        int x = (int) coordinate.x;
        int y = (int) coordinate.y;
        for(MSTVertex data : vertexDataList){
            if(data.coordX == x && data.coordY == y){
                return data;
            }
        }
        return null;
    }

    public static MSTVertex getVertexData(int id, List<MSTVertex> vertexDataList){
        for(MSTVertex data : vertexDataList){
            if(data.id == id){
                return data;
            }
        }
        return null;
    }

    public static class QuadEdgeData {
        public int id;
        public double length;
        public boolean isIncluded;
        public MSTVertex origin;
        public MSTVertex dest;

        public QuadEdgeData(int id, MSTVertex origin, MSTVertex dest){
            this.id = id;
            this.origin = origin;
            this.dest = dest;
        }
    }

}
