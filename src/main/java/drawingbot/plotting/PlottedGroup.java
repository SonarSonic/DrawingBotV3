package drawingbot.plotting;

import drawingbot.geom.basic.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumDistributionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlottedGroup {

    public int groupID;
    public ObservableDrawingSet drawingSet;
    public PFMFactory<?> pfmFactory;

    public long vertexCount;
    public List<IGeometry> geometries;
    public List<ObservableDrawingPen> originalDrawingSetOrder;

    public EnumDistributionType overrideDistributionType = null;

    public PlottedGroup(int groupID, ObservableDrawingSet drawingSet, PFMFactory<?> pfmFactory){
        this.groupID = groupID;
        this.drawingSet = drawingSet;
        this.pfmFactory = pfmFactory;
        this.geometries = new ArrayList<>();
        this.originalDrawingSetOrder = List.copyOf(drawingSet.pens);
        this.vertexCount = 0;
    }

    protected void changeGroupID(int groupID){
        this.groupID = groupID;
        geometries.forEach(g -> g.setGroupID(groupID));
        geometriesPerPen = null;
    }

    protected void clearGeometries(){
        geometries.clear();
        geometriesPerPen = null;
        vertexCount = 0;
    }

    public boolean canMerge(PlottedGroup group, boolean forExport){
        return group.drawingSet == drawingSet && group.pfmFactory == pfmFactory && (forExport || (group.overrideDistributionType == overrideDistributionType));
    }

    public void addGeometry(IGeometry geometry) {
        geometries.add(geometry);
        vertexCount += geometry.getVertexCount();
    }

    public int getGeometryCount(){
        return geometries.size();
    }

    public long getVertexCount(){
        return vertexCount;
    }

    public void setPFMFactory(PFMFactory<?> pfmFactory){
        this.pfmFactory = pfmFactory;
    }

    public void updateDistribution(PlottedDrawing drawing){
        if(overrideDistributionType != null){
            overrideDistributionType.distribute.accept(this);
        }else{
            drawingSet.distributionType.get().distribute.accept(this);
        }
        geometriesPerPen = null;
    }

    public int getPenCount() {
        return drawingSet.pens.size();
    }

    public int getGroupID() {
        return groupID;
    }

    public int getDrawingSetSlot(){
        int slot = drawingSet.getDrawingSetSlot();
        return slot == -1 ? 0 : slot;
    }


    //this list will be modified to represent the optimised order for the geometries, should be invalidated when anything in the group changes
    private Map<ObservableDrawingPen, List<IGeometry>> geometriesPerPen;

    /**
     * All geometry operations should ignore the standard geometry list and use this instead
     */
    public Map<ObservableDrawingPen, List<IGeometry>> getGeometriesPerPen(){
        if(geometriesPerPen == null){
            geometriesPerPen = new HashMap<>();
            for(IGeometry geometry : geometries){
                ObservableDrawingPen drawingPen = drawingSet.getPen(geometry.getPenIndex());
                geometriesPerPen.putIfAbsent(drawingPen, new ArrayList<>());
                geometriesPerPen.get(drawingPen).add(geometry);
            }
        }
        return geometriesPerPen;
    }
}
