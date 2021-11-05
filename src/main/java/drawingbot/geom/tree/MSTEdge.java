package drawingbot.geom.tree;

import drawingbot.geom.tree.MSTVertex;
import drawingbot.utils.Utils;

public class MSTEdge {

    public MSTVertex origin;
    public MSTVertex dest;
    public double length;

    public MSTEdge(MSTVertex origin, MSTVertex dest) {
        this.origin = origin;
        this.dest = dest;
        this.length = Utils.distance(origin.coordX, origin.coordY, dest.coordX, dest.coordY);
    }

}
