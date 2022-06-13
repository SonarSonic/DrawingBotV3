package drawingbot.plotting;

import drawingbot.api.ICanvas;
import drawingbot.api.IPlottingTools;
import drawingbot.drawing.DrawingSets;
import drawingbot.utils.Metadata;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PlottedDrawing {

    public ICanvas canvas;
    public DrawingSets drawingSets;

    public final List<IGeometry> geometries;
    public final Map<Integer, PlottedGroup> groups;
    public Map<Metadata<?>, Object> metadataMap;

    public long vertexCount;
    public int displayedShapeMin = -1;
    public int displayedShapeMax = -1;
    public boolean ignoreWeightedDistribution = false; //used for disabling distributions within sub tasks, will use the pfms default

    public PlottedDrawing(ICanvas canvas, DrawingSets drawingSets){
        this(canvas, drawingSets, true);
    }

    public PlottedDrawing(ICanvas canvas, DrawingSets drawingSets, boolean copyCanvas){
        this.canvas = copyCanvas ? new SimpleCanvas(canvas) : canvas;
        this.drawingSets = drawingSets;
        this.geometries = Collections.synchronizedList(new ArrayList<>());
        this.groups = new ConcurrentHashMap<>();
        this.metadataMap = new ConcurrentHashMap<>();
    }

    /**
     * Copies the base groups of the plotted drawing only
     */
    public void copyBase(PlottedDrawing reference){
        metadataMap = new ConcurrentHashMap<>(reference.metadataMap);
        for(PlottedGroup group : reference.groups.values()){
            PlottedGroup newGroup = new PlottedGroup(group);
            addPlottedGroup(newGroup);
        }
    }

    /**
     * Creates a full clone of the plotted drawing and all of the geometries used by it
     */
    public void copyAll(PlottedDrawing reference){
        copyBase(reference);
        reference.geometries.forEach(g -> addGeometry(g.copyGeometry()));
    }

    public PlottedDrawing copy(){
        PlottedDrawing copy = new PlottedDrawing(canvas, drawingSets);
        copy.copyAll(this);
        return copy;
    }

    public PlottedDrawing copyBase(){
        PlottedDrawing copy = new PlottedDrawing(canvas, drawingSets);
        copy.copyBase(this);
        return copy;
    }

    public PlottedDrawing newPlottedDrawing(){
        return new PlottedDrawing(canvas, drawingSets);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public <T> void setMetadata(Metadata<T> metadata, T value){
        if(value != null){
            metadataMap.put(metadata, value);
        }
    }

    public <T> T getMetadata(Metadata<T> metadata){
        Object obj = metadataMap.get(metadata);
        if(obj == null){
            return null;
        }
        if(!metadata.type.isInstance(obj)){
            return null;
        }
        return metadata.type.cast(obj);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public ICanvas getCanvas() {
        return canvas;
    }

    public int getDisplayedShapeMin(){
        if(displayedShapeMin == -1){
            return 0;
        }
        return displayedShapeMin;
    }

    public int getDisplayedShapeMax(){
        if(displayedShapeMax == -1){
            return getGeometryCount();
        }
        return displayedShapeMax;
    }

    public int getGeometryCount(){
        return geometries.size();
    }

    public long getVertexCount(){
        return vertexCount;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addGeometry(IGeometry geometry) {
        geometry.setGeometryIndex(geometries.size());
        geometries.add(geometry);
        vertexCount += geometry.getVertexCount();

        addGeometryToGroups(geometry);
    }

    public void addGeometry(IGeometry geometry, PlottedGroup group) {
        assert groups.containsValue(group);

        geometry.setGroupID(group.getGroupID());
        addGeometry(geometry);
    }

    public void addGeometry(List<IGeometry> orderedGeometries) {
        orderedGeometries.forEach(this::addGeometry);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private int groupID = 0;

    public int getNextGroupID(){
        if(groups.get(groupID) == null){
            return groupID;
        }
        groupID++;
        return getNextGroupID();
    }

    public PlottedGroup getPlottedGroup(int groupID){
        return groups.getOrDefault(groupID, groups.get(0));
    }

    public PlottedGroup newPlottedGroup(ObservableDrawingSet drawingSet, PFMFactory<?> pfmFactory){
        return addPlottedGroup(new PlottedGroup(getNextGroupID(), drawingSet, pfmFactory));
    }

    public PlottedGroup addPlottedGroup(PlottedGroup plottedGroup){
        plottedGroup.parent = this;
        groups.put(plottedGroup.groupID, plottedGroup);
        return plottedGroup;
    }

    public PlottedGroup getMatchingPlottedGroup(PlottedGroup group, boolean forExport){
        for(PlottedGroup plottedGroup : groups.values()){
            if(plottedGroup.canMerge(group, forExport)){
                return plottedGroup;
            }
        }
        return null;
    }

    public void reorderGroups(List<PlottedGroup> newOrder){
        int index = 0;
        for(PlottedGroup plottedGroup : newOrder){
            if(plottedGroup.getGroupID() != index){
                plottedGroup.changeGroupID(index);
            }
            index++;
        }
    }

    /**
     * This is a destructive action and invalidates the provided plotted group
     */
    public void mergePlottedGroup(PlottedGroup plottedGroup, boolean simplify, boolean forExport, BiConsumer<IGeometry, PlottedGroup> consumer){
        if(simplify){
            for(PlottedGroup group : groups.values()){
                if(group.canMerge(plottedGroup, forExport)){
                    plottedGroup.geometries.forEach(g -> consumer.accept(g, group));
                    return;
                }
            }
        }

        PlottedGroup newGroup = new PlottedGroup(plottedGroup);
        newGroup.groupID = getNextGroupID();
        addPlottedGroup(newGroup);
        for(IGeometry geometry : plottedGroup.geometries){
            consumer.accept(geometry, newGroup);
        }
    }


    public void mergePlottedDrawingClipped(PlottedDrawing drawing, boolean simplify, boolean forExport, PlottingTools tools){
        mergePlottedDrawing(drawing, simplify, forExport, (geometry, group) -> {
            tools.currentGroup = group;
            tools.addGeometry(geometry);
        });
    }

    public void mergePlottedDrawingDefault(PlottedDrawing drawing, boolean simplify, boolean forExport){
        mergePlottedDrawing(drawing, simplify, forExport, this::addGeometry);
    }

    /**
     * This is a destructive action and invalidates the provided plotted drawing and all of its plotted groups
     */
    public void mergePlottedDrawing(PlottedDrawing drawing, boolean simplify, boolean forExport, BiConsumer<IGeometry, PlottedGroup> consumer){
        drawing.groups.values().forEach(g -> mergePlottedGroup(g, simplify, forExport, consumer));
    }

    public void addGeometryToGroups(IGeometry geometry){
        getPlottedGroup(geometry.getGroupID()).addGeometry(geometry);
    }

    public void addGroupPFMType(int groupID, PFMFactory<?> factory){
        groups.get(groupID).setPFMFactory(factory);
    }

    public PFMFactory<?> getGroupPFMType(int groupID){
        return groups.get(groupID).pfmFactory;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void clearGeometries(){
        geometries.clear();
        vertexCount = 0;
        groups.values().forEach(PlottedGroup::clearGeometries);
    }


    public void reset(){
        clearGeometries();
        displayedShapeMax = -1;
        displayedShapeMin = -1;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public void updatePenDistribution(){
        if(!ignoreWeightedDistribution){
            groups.values().forEach(g -> g.needsDistribution = true);

            for(PlottedGroup group : groups.values()){
                if(!group.needsDistribution){
                    continue;
                }
                List<PlottedGroup> distributionSet = new ArrayList<>();
                if(group.groupType != PlottedGroup.GroupDistributionType.NONE){
                    for(PlottedGroup group2 : groups.values()){
                        if(group2.needsDistribution && group2.groupType == group.groupType && group.groupType.canCombine.apply(group, group2)){
                            distributionSet.add(group2);
                        }
                    }
                }else{
                    distributionSet.add(group);
                }
                group.getActiveDistributionType().distribute.accept(new DistributionSet(this, distributionSet));
                distributionSet.forEach(g -> {
                    g.onDistributionChanged();
                    g.needsDistribution = false;
                });
            }

            updatePerPenGeometryStats(this, getPerPenGeometryStats(this));
        }
    }

    public List<ObservableDrawingPen> getAllPens(){
        List<ObservableDrawingPen> allPens = new ArrayList<>();
        drawingSets.drawingSetSlots.get().forEach(drawingSet -> allPens.addAll(drawingSet.pens));
        return allPens;
    }

    /**
     * The order in which pens are being displayed to the user
     * If multiple drawing sets are active it will be ordered in ascending order of 'slot number'
     * Note: This may contain inactive pens
     */
    public List<ObservableDrawingPen> getGlobalDisplayOrder(){
        List<ObservableDrawingSet> drawingSets = new ArrayList<>();
        for(PlottedGroup group : groups.values()){
            if(!drawingSets.contains(group.drawingSet)){
                drawingSets.add(group.drawingSet);
            }
        }

        drawingSets.sort(Comparator.comparingInt(set -> this.drawingSets.drawingSetSlots.get().indexOf(set)));

        List<ObservableDrawingPen> globalOrder = new ArrayList<>();
        drawingSets.forEach(drawingSet -> globalOrder.addAll(drawingSet.pens));

        return globalOrder;
    }

    public List<ObservableDrawingPen> getGlobalRenderOrder(){
        return getGlobalRenderOrder(groups.values());
    }

    /**
     * The logical order in which pens should be rendered
     * If multiple drawing set are active they will be ordered using the most dominate distribution type
     * Note: This may contain inactive pens
     */
    public static List<ObservableDrawingPen> getGlobalRenderOrder(Collection<PlottedGroup> groups){
        List<ObservableDrawingSet> drawingSets = new ArrayList<>();
        List<ObservableDrawingPen> globalOrder = new ArrayList<>();
        for(PlottedGroup group : groups){
            if(!drawingSets.contains(group.drawingSet)){
                drawingSets.add(group.drawingSet);
            }

            for(ObservableDrawingPen drawingPen : group.drawingSet.pens){
                if(!globalOrder.contains(drawingPen)){
                    globalOrder.add(drawingPen);
                }
            }
        }

        int highestTally = 0;
        EnumDistributionOrder order = EnumDistributionOrder.DARKEST_FIRST;

        for(EnumDistributionOrder distributionOrder : EnumDistributionOrder.values()){
            int tally = 0;
            for(ObservableDrawingSet drawingSet : drawingSets){
                if(drawingSet.distributionOrder.get() == distributionOrder){
                    tally++;
                }
            }
            if(tally > highestTally){
                highestTally = tally;
                order = distributionOrder;
            }
        }

        globalOrder.sort(order.comparator);

        //TODO - THERE IS SOME INCONSISTENCY GOING ON WITH DISTRIBUTION ORDER, SURELY IT SHOULD ONLY BE USED FOR DISTRIBUTION AND NOT RENDER ORDER???
        Collections.reverse(globalOrder);

        return globalOrder;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void updatePenNumbers(DistributionSet set){
        for(int i = 0; i < set.source.drawingSet.pens.size(); i++){
            ObservableDrawingPen pen = set.source.drawingSet.pens.get(i);
            pen.penNumber.set(i); //update pens number based on position
        }
    }


    public static Map<ObservableDrawingPen, Integer> getPerPenGeometryStats(PlottedDrawing plottedDrawing){
        Map<PlottedGroup, Map<Integer, Integer>> perGroupStats = new HashMap<>();
        Map<ObservableDrawingPen, Integer> perPenStats = new HashMap<>();

        //create a tally for each group
        for(PlottedGroup group : plottedDrawing.groups.values()){
            Map<Integer, Integer> map = new HashMap<>();
            for(ObservableDrawingPen drawingPen : group.drawingSet.pens){
                perPenStats.putIfAbsent(drawingPen, 0);
                map.put(drawingPen.penNumber.get(), 0);
            }
            perGroupStats.put(group, map);
        }

        //tally all the geometries per group / per pen
        for(IGeometry geometry : plottedDrawing.geometries){
            if(geometry.getPenIndex() >= 0){
                Map<Integer, Integer> stats = perGroupStats.get(plottedDrawing.getPlottedGroup(geometry.getGroupID()));
                if(stats != null){
                    stats.putIfAbsent(geometry.getPenIndex(), 0);
                    stats.put(geometry.getPenIndex(), stats.get(geometry.getPenIndex())+1);
                }
            }
        }

        //combine the tallies into pen stats per unique pen
        for(Map.Entry<PlottedGroup, Map<Integer, Integer>> groupStats : perGroupStats.entrySet()){
            Map<Integer, Integer> stats = groupStats.getValue();
            for(ObservableDrawingPen drawingPen : groupStats.getKey().drawingSet.pens){
                perPenStats.putIfAbsent(drawingPen, 0);
                perPenStats.put(drawingPen, perPenStats.get(drawingPen)+stats.get(drawingPen.penNumber.get()));
            }
        }

        return perPenStats;
    }

    public static void updatePerPenGeometryStats(PlottedDrawing plottedDrawing, Map<ObservableDrawingPen, Integer> perPenStats){
        //apply the stats to the pen
        for(Map.Entry<ObservableDrawingPen, Integer> groupStats : perPenStats.entrySet()){
            groupStats.getKey().currentGeometries.set(groupStats.getValue());
            groupStats.getKey().currentPercentage.set(NumberFormat.getPercentInstance().format((float)groupStats.getValue() / plottedDrawing.geometries.size()));
        }
    }

    /**updates every pen's unique number, and sets the correct pen number for every line based on their weighted distribution*/
    public static void updateEvenDistribution(DistributionSet set, boolean weighted, boolean random){
        updatePenNumbers(set);

        int currentGeometry = 0;
        int totalWeight = 0;
        int[] weights = new int[set.source.drawingSet.pens.size()];
        for(int i = 0; i < set.source.drawingSet.pens.size(); i++){
            ObservableDrawingPen pen = set.source.drawingSet.pens.get(i);
            pen.penNumber.set(i); //update pens number based on position
            if(pen.isEnabled()){
                weights[i] = weighted ? pen.distributionWeight.get() : 100;
                totalWeight += weights[i];
            }
        }

        int[] renderOrder = set.source.drawingSet.calculateRenderOrder();

        if(!random){
            for(int i = 0; i < renderOrder.length; i++){
                int penNumber = renderOrder[i];
                ObservableDrawingPen pen = set.source.drawingSet.getPen(penNumber);
                if(pen.isEnabled()){

                    //update percentage
                    float percentage = (weighted ? (float)pen.distributionWeight.get() : 100) / totalWeight;
                    //update geometry count
                    int geometriesPerPen = (int)(percentage * set.getGeometryCount());

                    //set pen references
                    int end = i == renderOrder.length-1 ? set.getGeometryCount() : currentGeometry + geometriesPerPen;
                    for (; currentGeometry < end; currentGeometry++) {
                        IGeometry geometry = set.getGeometryList().get(currentGeometry);
                        geometry.setPenIndex(penNumber);
                    }
                }
            }
        }else{
            Random rand = new Random(0);

            for(IGeometry geometry : set.getGeometryList()){
                int weightedRand = rand.nextInt(totalWeight);
                int currentWeight = 0;
                for(int w = 0; w < weights.length; w++){
                    currentWeight += weights[w];
                    if(weightedRand < currentWeight){
                        geometry.setPenIndex(w);
                        break;
                    }
                }
            }
        }
    }

    public static void updatePreConfiguredPenDistribution(DistributionSet set){
        updatePenNumbers(set);

        for(PlottedGroup group : set.plottedGroups){
            for(IGeometry geometry : group.geometries){
                int originalIndex = geometry.getPFMPenIndex();
                ObservableDrawingPen drawingPen = group.originalDrawingSetOrder.get(originalIndex);
                int currentIndex = group.drawingSet.pens.indexOf(drawingPen);
                geometry.setPenIndex(currentIndex);
            }
        }


    }

    public static void updateSinglePenDistribution(DistributionSet set){
        updatePenNumbers(set);

        for(PlottedGroup group : set.plottedGroups){
            int[] renderOrder = group.drawingSet.calculateRenderOrder();

            for (int penNumber : renderOrder) {
                ObservableDrawingPen pen = group.drawingSet.pens.get(penNumber);
                if (pen.isEnabled()) {
                    ///add all the geometries to the first enabled pen
                    group.geometries.forEach(g -> g.setPenIndex(penNumber));
                    return;
                }
            }
        }


    }

    /**sets all the pens to default distribution / even*/
    public static void resetWeightedDistribution(PlottedGroup plottedGroup){
        for(ObservableDrawingPen pen : plottedGroup.drawingSet.getPens()){
            pen.distributionWeight.setValue(100);
        }
    }

    /**increases the distribution weight of a given pen*/
    public static void adjustWeightedDistribution(PlottedGroup plottedGroup, ObservableDrawingPen selected, int adjust){
        int current = selected.distributionWeight.get();
        selected.distributionWeight.set(Math.max(0, current + adjust));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// META DATA \\\\

    @Nullable
    public BufferedImage getOriginalImage() {
        return getMetadata(Register.INSTANCE.ORIGINAL_IMAGE);
    }

    @Nullable
    public BufferedImage getReferenceImage() {
        return getMetadata(Register.INSTANCE.REFERENCE_IMAGE);
    }

    @Nullable
    public BufferedImage getPlottingImage() {
        return getMetadata(Register.INSTANCE.PLOTTING_IMAGE);
    }

    @Nullable
    public BufferedImage getToneMap() {
        return getMetadata(Register.INSTANCE.TONE_MAP);
    }

    @Nullable
    public File getOriginalFile() {
        return getMetadata(Register.INSTANCE.ORIGINAL_FILE);
    }

}