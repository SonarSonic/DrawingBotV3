package drawingbot.plotting;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class PlottedGroup {

    public int groupID;
    public ObservableDrawingSet drawingSet;
    public PFMFactory<?> pfmFactory;

    public long vertexCount;
    public List<IGeometry> geometries;
    public List<ObservableDrawingPen> originalDrawingSetOrder;

    protected boolean needsDistribution = true;
    public EnumDistributionType overrideDistributionType = null;
    public GroupDistributionType groupType = GroupDistributionType.NONE;

    /**
     * Note: this doesn't copy the geometries
     */
    public PlottedGroup(PlottedGroup toCopy){
        this.groupID = toCopy.groupID;
        this.drawingSet = toCopy.drawingSet;
        this.pfmFactory = toCopy.pfmFactory;
        this.vertexCount = 0;
        this.geometries = new ArrayList<>();
        this.originalDrawingSetOrder = List.copyOf(toCopy.originalDrawingSetOrder);
        this.needsDistribution = toCopy.needsDistribution;
        this.overrideDistributionType = toCopy.overrideDistributionType;
        this.groupType = toCopy.groupType;
    }

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
        return canMerge(group, true, !forExport, !forExport);
    }

    public boolean canMerge(PlottedGroup group, boolean checkDrawingSet, boolean checkPFM, boolean checkDistribution){
        return (!checkDrawingSet || group.drawingSet == drawingSet) && (!checkPFM || group.pfmFactory == pfmFactory) && (!checkDistribution || (group.getActiveDistributionType() == getActiveDistributionType()));
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

    public void onDistributionChanged(){
        geometriesPerPen = null;
    }

    public EnumDistributionType getActiveDistributionType(){
        return overrideDistributionType != null ? overrideDistributionType : drawingSet.distributionType.get();
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
    private transient Map<ObservableDrawingPen, List<IGeometry>> geometriesPerPen;

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

    public enum GroupDistributionType {
        NONE((group1, group2) -> false), //groups will be distributed individually
        ORDERED_PER_PFM((group1, group2) -> group1.canMerge(group2, true, true, true)), //groups with matching drawing set and pfm will be distributed together
        ORDERED((group1, group2) -> group1.canMerge(group2, true, false, true)); //groups with matching drawing set will be distributed together

        public final BiFunction<PlottedGroup, PlottedGroup, Boolean> canCombine;

        GroupDistributionType(BiFunction<PlottedGroup, PlottedGroup, Boolean> canCombine){
            this.canCombine = canCombine;
        }

        @Override
        public String toString() {
            return Utils.capitalize(name());
        }

    }
}
