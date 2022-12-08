package drawingbot.geom.operation;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import org.locationtech.jts.geom.Coordinate;

/**
 * This will add multi-passes to every geometry, typically this is only used if the default Optimize operation is bypassed
 */
public class GeometryOperationBasicMultipass extends AbstractGeometryOperation{

    public int multipassCount;

    public GeometryOperationBasicMultipass(int multipassCount){
        this.multipassCount = multipassCount;
    }

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing newDrawing = createPlottedDrawing(originalDrawing);

        for(PlottedGroup group : originalDrawing.groups.values()) {
            PlottedGroup originalGroup = originalDrawing.getPlottedGroup(group.getGroupID());
            PlottedGroup newGroup = newDrawing.getMatchingPlottedGroup(originalGroup, forExport);

            for(IGeometry geometry : originalGroup.geometries){

                newDrawing.addGeometry(GeometryUtils.createMultiPassGeometry(geometry, multipassCount), newGroup);
            }
        }

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }
}
