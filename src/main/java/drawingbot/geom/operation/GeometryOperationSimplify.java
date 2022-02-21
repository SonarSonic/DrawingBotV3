package drawingbot.geom.operation;

import drawingbot.api.IGeometryFilter;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.GPath;
import drawingbot.geom.basic.IGeometry;
import drawingbot.geom.basic.IPathElement;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;

import java.util.ArrayList;
import java.util.List;

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
        for(IGeometry geometry : originalDrawing.geometries){
            PlottedGroup originalGroup = originalDrawing.getPlottedGroup(geometry.getGroupID());
            PlottedGroup newGroup = newDrawing.getMatchingPlottedGroup(originalGroup, forExport);
            ObservableDrawingPen pen = originalGroup.drawingSet.getPen(geometry.getPenIndex());

            if(geometryFilter.filter(originalDrawing, geometry, pen)){
                if(geometry instanceof IPathElement){
                    IPathElement element = (IPathElement) geometry;
                    IGeometry lastGeometry = newGroup.geometries.isEmpty() ? null : newGroup.geometries.get(newGroup.geometries.size()-1);

                    if(lastGeometry instanceof GPath){
                        GPath gPath = (GPath) lastGeometry;
                        //check the render colour and continuity if they match, add it too the path
                        if(GeometryUtils.compareRenderColour(pen, gPath, element)){
                            boolean continuity = GeometryUtils.comparePathContinuity(gPath, element);
                            if(continuity){
                                element.addToPath(false, gPath);
                                continue;
                            }else if(includeMultipleMoves && element.getGroupID() == gPath.getGroupID()){
                                element.addToPath(true, gPath);
                                continue;
                            }
                        }
                    }
                    //if the last geometry isn't a GPath or the element can't be added add a new GPath
                    if(element instanceof GPath){
                        newDrawing.addGeometry(element.copyGeometry(), newGroup);
                    }else{
                        newDrawing.addGeometry(new GPath(element), newGroup);
                    }
                }else{
                    newDrawing.addGeometry(geometry.copyGeometry(), newGroup);
                }
            }
            index++;
            updateProgress(index, originalDrawing.getGeometryCount());
        }

        //remove empty groups
        List<Integer> toRemove = new ArrayList<>();
        for(PlottedGroup group : newDrawing.groups.values()){
            if(group.geometries.isEmpty()){
                toRemove.add(group.groupID);
            }
        }

        for(Integer i : toRemove){
            newDrawing.groups.remove(i);
        }

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }


}
