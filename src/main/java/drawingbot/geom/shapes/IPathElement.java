package drawingbot.geom.shapes;

public interface IPathElement extends IGeometry {

    void addToPath(boolean addMove, GPath path);

}
