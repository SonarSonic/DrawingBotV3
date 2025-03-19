package drawingbot.geom.operation;

import drawingbot.geom.shapes.GEllipse;
import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.registry.Register;

import java.awt.geom.PathIterator;

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

            float lastMoveX = Float.NaN;
            float lastMoveY = Float.NaN;

            float lastX = Float.NaN;
            float lastY = Float.NaN;
            float lastPenIndex = -1;


            for(IGeometry geometry : group.geometries){

                PathIterator pathIterator = geometry.getAWTShape().getPathIterator(null);
                float[] coords = new float[6];
                while (!pathIterator.isDone()){
                    int type = pathIterator.currentSegment(coords);
                    switch (type){
                        case PathIterator.SEG_MOVETO -> {
                            float nextX = coords[0];
                            float nextY = coords[1];
                            boolean matchingPen = lastPenIndex == geometry.getPenIndex();
                            boolean continuity = lastX == nextX && lastY == nextY;
                            boolean firstMove = Float.isNaN(lastX);

                            if(!firstMove && (!matchingPen || !continuity)){
                                IGeometry liftPoint = new GEllipse(lastX-0.5F, lastY-0.5F, 1, 1);
                                liftPoint.setPenIndex(2);
                                newDrawing.addGeometry(liftPoint, exportGroup);
                            }

                            if(firstMove || !matchingPen || !continuity){
                                IGeometry dropPoint = new GEllipse(nextX-0.5F, nextY-0.5F, 1, 1);
                                dropPoint.setPenIndex(0);
                                newDrawing.addGeometry(dropPoint, exportGroup);
                            }

                            if(!firstMove && matchingPen && !continuity){
                                IGeometry moveLine = new GLine(lastX, lastY, nextX, nextY);
                                moveLine.setPenIndex(1);
                                newDrawing.addGeometry(moveLine, exportGroup);
                            }

                            lastX = lastMoveX = nextX;
                            lastY = lastMoveY = nextY;
                            lastPenIndex = geometry.getPenIndex();
                        }
                        case PathIterator.SEG_LINETO -> {
                            lastX = coords[0];
                            lastY = coords[1];
                        }
                        case PathIterator.SEG_QUADTO -> {
                            lastX = coords[2];
                            lastY = coords[3];
                        }
                        case PathIterator.SEG_CUBICTO -> {
                            lastX = coords[4];
                            lastY = coords[5];
                        }
                        case PathIterator.SEG_CLOSE -> {
                            lastX = lastMoveX;
                            lastY = lastMoveY;
                        }
                    }
                    pathIterator.next();
                }
                newDrawing.addGeometry(geometry, newGroup);
            }

            if(!Float.isNaN(lastX)){
                IGeometry liftPointFinal = new GEllipse(lastX-0.5F, lastY-0.5F, 1, 1);
                liftPointFinal.setPenIndex(2);
                newDrawing.addGeometry(liftPointFinal, exportGroup);
            }
        }

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }
}
