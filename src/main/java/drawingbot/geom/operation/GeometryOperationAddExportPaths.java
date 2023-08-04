package drawingbot.geom.operation;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GEllipse;
import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.registry.Register;
import org.locationtech.jts.geom.Coordinate;

/**
 * This will add pen lifts / drops and moves too the Plotted Drawing for displaying in the viewport
 */
public class GeometryOperationAddExportPaths extends AbstractGeometryOperation{

    public GeometryOperationAddExportPaths(){}

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing newDrawing = originalDrawing.copyBase();

        PlottedGroup exportGroup = newDrawing.newPlottedGroup(Register.INSTANCE.EXPORT_PATH_DRAWING_SET, null);

        for(PlottedGroup group : originalDrawing.groups.values()) {

            PlottedGroup newGroup = newDrawing.getMatchingPlottedGroup(group, true);

            IGeometry lastGeometry = null;
            for(IGeometry geometry : group.geometries){

                Coordinate originCoord = geometry.getOriginCoordinate();
                IGeometry originEllipse = new GEllipse((float)originCoord.x-0.5F, (float)originCoord.y-0.5F, 1, 1);
                originEllipse.setPenIndex(2);
                newDrawing.addGeometry(originEllipse, exportGroup);

                newDrawing.addGeometry(geometry, newGroup);

                Coordinate dstCoord = geometry.getEndCoordinate();
                IGeometry dstEllipse = new GEllipse((float)dstCoord.x-0.5F, (float)dstCoord.y-0.5F, 1, 1);
                dstEllipse.setPenIndex(0);
                newDrawing.addGeometry(dstEllipse, exportGroup);


                if(lastGeometry != null && !GeometryUtils.comparePathContinuity(lastGeometry, geometry) && lastGeometry.getPenIndex() == geometry.getPenIndex()){
                    IGeometry moveLine = new GLine(lastGeometry.getEndCoordinate(), geometry.getOriginCoordinate());
                    moveLine.setPenIndex(1);
                    newDrawing.addGeometry(moveLine, exportGroup);
                }
                lastGeometry = geometry;
            }
        }

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }
}
