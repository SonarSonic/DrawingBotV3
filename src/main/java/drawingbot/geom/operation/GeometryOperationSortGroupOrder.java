package drawingbot.geom.operation;

import drawingbot.geom.spatial.STRTreeSequencer;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import org.locationtech.jts.geom.Coordinate;

/**
 * This will change the order in which groups appear in the plotted drawing to optimize
 */
public class GeometryOperationSortGroupOrder extends AbstractGeometryOperation{

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {

        STRTreeSequencer<PlottedGroup> sequencer = new STRTreeSequencer<>(originalDrawing.groups.values(), 0) {

            @Override
            protected Coordinate getStartCoordinateFromCity(PlottedGroup group) {
                return group.geometries.get(0).getOriginCoordinate();
            }

            @Override
            protected Coordinate getEndCoordinateFromCity(PlottedGroup group) {
                return group.geometries.get(group.geometries.size()-1).getOriginCoordinate();
            }
        };

        originalDrawing.reorderGroups(sequencer.sort());

        return originalDrawing;
    }

    @Override
    public boolean isDestructive() {
        return true;
    }
}
