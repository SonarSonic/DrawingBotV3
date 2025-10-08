package drawingbot.geom.operation;

import drawingbot.api.IGeometryFilter;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.IPathElement;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;

/**
 * This operation is always run when performing a vector-based export, and creates a copy of the Geometries
 * It combines any obvious path elements with obvious continuity, i.e. continuity which was established by the PFM
 */
public class GeometryOperationSimplify extends AbstractGeometryOperation {

    public IGeometryFilter geometryFilter;
    public boolean includeMultipleMoves;

    private GPath currentPath;
    private PlottedDrawing newDrawing;
    private PlottedGroup newGroup;

    public GeometryOperationSimplify(IGeometryFilter geometryFilter, boolean forExport, boolean includeMultipleMoves) {
        super();
        this.geometryFilter = geometryFilter;
        this.forExport = forExport;
        this.includeMultipleMoves = includeMultipleMoves;
    }

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        this.newDrawing = createPlottedDrawing(originalDrawing);

        int index = 0;
        for(PlottedGroup group : originalDrawing.groups.values()){
            this.newGroup = newDrawing.getPlottedGroup(group.getGroupID());
            FlagStates pfmFlags = group.pfmFactory == null ? Flags.DEFAULT_PFM_STATE : group.pfmFactory.getFlags();
            if(pfmFlags.getFlag(Flags.PFM_BYPASS_GEOMETRY_OPTIMISING) || !pfmFlags.getFlag(Flags.PFM_GEOMETRY_SIMPLIFY)){
                group.geometries.forEach(g -> newDrawing.addGeometry(g, newGroup));
                continue;
            }

            for(IGeometry geometry : group.geometries){
                ObservableDrawingPen pen = group.drawingSet.getPen(geometry.getPenIndex());
                if(geometryFilter.filter(originalDrawing, geometry, pen)){
                    splitGeometry(geometry, newGroup, pen);
                }
                index++;
                updateProgress(index, originalDrawing.getGeometryCount());
            }
            finishPath();
        }
        return newDrawing;
    }

    private void startPath(){
        if(currentPath != null){
            finishPath();
        }
        currentPath = new GPath();
    }

    private void finishPath(){
        if(currentPath != null && currentPath.getVertexCount() != 0){
            newDrawing.addGeometry(currentPath, newGroup);
        }
        currentPath = null;
    }

    public boolean tryExtendCurrentPath(IGeometry geometry, ObservableDrawingPen pen){
        if(currentPath == null){
            return false;
        }
        if(geometry instanceof IPathElement element){
            if(GeometryUtils.compareRenderColour(pen, currentPath, element)){
                boolean continuity = GeometryUtils.comparePathContinuity(currentPath, element);
                if(continuity){
                    element.addToPath(false, currentPath);
                    return true;
                }else if(includeMultipleMoves && element.getGroupID() == currentPath.getGroupID()){
                    element.addToPath(true, currentPath);
                    return true;
                }
            }
        }
        return false;
    }

    public void splitGeometry(IGeometry geometry, PlottedGroup newGroup, ObservableDrawingPen pen){
        if(!includeMultipleMoves && geometry instanceof GPath path){
            if(GeometryUtils.getSubPathCount(path.awtPath) > 1){
                GeometryUtils.splitGPathIntoSubPaths(path, subPath -> consumeGeometry(subPath, newGroup, pen));
                return;
            }
        }
        consumeGeometry(geometry, newGroup, pen);
    }

    public void consumeGeometry(IGeometry geometry, PlottedGroup newGroup, ObservableDrawingPen pen){
        if(currentPath != null && tryExtendCurrentPath(geometry, pen)){
            return;
        }

        finishPath();

        if(geometry instanceof IPathElement element){
            currentPath = element instanceof GPath ? ((GPath) element).copyOpenPath() :  new GPath(element, true);
        }else{
            newDrawing.addGeometry(geometry.copyGeometry(), newGroup);
        }
    }

    @Override
    public boolean isDestructive() {
        return false;
    }


}
