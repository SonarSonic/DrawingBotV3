package drawingbot.geom.operation;

import drawingbot.api.IGeometryFilter;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.IPathElement;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;

/**
 * This operation is always run when performing a vector based export, and creates a copy of the Geometries
 * It combines any obvious path elements with obvious continuity, i.e. continuity which was established by the PFM
 */
public class GeometryOperationSimplify extends AbstractGeometryOperation {

    public IGeometryFilter geometryFilter;
    public boolean includeMultipleMoves;

    public GeometryOperationSimplify(IGeometryFilter geometryFilter, boolean forExport, boolean includeMultipleMoves) {
        super();
        this.geometryFilter = geometryFilter;
        this.forExport = forExport;
        this.includeMultipleMoves = includeMultipleMoves;
    }

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing newDrawing = createPlottedDrawing(originalDrawing);

        int index = 0;
        for(PlottedGroup group : originalDrawing.groups.values()){
            PlottedGroup newGroup = newDrawing.getPlottedGroup(group.getGroupID());
            GPath currentPath = null;
            for(IGeometry geometry : group.geometries){
                ObservableDrawingPen pen = group.drawingSet.getPen(geometry.getPenIndex());
                if(geometryFilter.filter(originalDrawing, geometry, pen)){
                    if(geometry instanceof IPathElement){
                        IPathElement element = (IPathElement) geometry;

                        if(currentPath != null){
                            //check the render colour and continuity if they match, add it too the path
                            if(GeometryUtils.compareRenderColour(pen, currentPath, element)){
                                boolean continuity = GeometryUtils.comparePathContinuity(currentPath, element);
                                if(continuity){
                                    element.addToPath(false, currentPath);
                                    continue;
                                }else if(includeMultipleMoves && element.getGroupID() == currentPath.getGroupID()){
                                    element.addToPath(true, currentPath);
                                    continue;
                                }
                            }
                            //add the completed path to the drawing
                            newDrawing.addGeometry(currentPath, newGroup);
                        }

                        //if the last geometry isn't a GPath or the Element can't be added create a new GPath
                        currentPath = element instanceof GPath ? (GPath) element.copyGeometry() :  new GPath(element, true);
                    }else{
                        if(currentPath != null){
                            //add the completed path to the drawing
                            newDrawing.addGeometry(currentPath, newGroup);
                        }
                        newDrawing.addGeometry(geometry.copyGeometry(), newGroup);
                    }
                }
                index++;
                updateProgress(index, originalDrawing.getGeometryCount());
            }

            if(currentPath != null){
                newDrawing.addGeometry(currentPath, newGroup);
            }
        }

        //remove empty groups
        /*
        List<Integer> toRemove = new ArrayList<>();
        for(PlottedGroup group : newDrawing.groups.values()){
            if(group.geometries.isEmpty()){
                toRemove.add(group.groupID);
            }
        }

        for(Integer i : toRemove){
            newDrawing.groups.remove(i);
        }
         */

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }


}
