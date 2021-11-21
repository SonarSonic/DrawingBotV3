package drawingbot.geom.tree;

import drawingbot.geom.tsp.TSPNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MSTVertex {
    public int id;
    public int rank;

    public Coordinate coordinate;
    public int coordX, coordY;

    //the nearest neighbour edges as constructed by triangulation
    public Map<MSTVertex, QuadEdge> edges;

    //the edges belonging to the Minimum Spanning Tree as computed by pimm's algorithm
    public Map<MSTVertex, MSTEdge> mstEdgeMap;

    //the nearest vertices as computed by a-nearness
    public List<MSTVertex> nearestVertices;

    public boolean isVisited;

    public TSPNode node;


    public MSTVertex(int id, Coordinate coordinate, Map<MSTVertex, QuadEdge> edges) {
        this.id = id;
        this.coordinate = coordinate;
        this.coordX = (int) coordinate.x;
        this.coordY = (int) coordinate.y;
        this.edges = edges;
        this.isVisited = false;
        this.mstEdgeMap = new HashMap<>();
        this.nearestVertices = new ArrayList<>();
    }
}
