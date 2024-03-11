package drawingbot.plotting;

import drawingbot.api.ICanvas;
import drawingbot.drawing.DrawingSets;
import drawingbot.drawing.DrawingStats;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.image.ImageTools;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.Metadata;
import drawingbot.utils.MetadataMap;
import drawingbot.utils.flags.Flags;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class PlottedDrawing {

    public UUID uuid;
    public ICanvas canvas;
    public DrawingSets drawingSets;

    public final List<IGeometry> geometries;
    public final Map<Integer, PlottedGroup> groups;
    public MetadataMap metadata;

    public long vertexCount;
    public int displayedShapeMin = -1;
    public int displayedShapeMax = -1;
    public long displayedVertexCount = -1;
    public boolean ignoreWeightedDistribution = false; //used for disabling distributions within sub tasks, will use the pfms default

    private Map<ObservableDrawingPen, Integer> perPenGeometryStats;

    public PlottedDrawing(ICanvas canvas, DrawingSets drawingSets){
        this(canvas, drawingSets, true);
    }

    public PlottedDrawing(ICanvas canvas, DrawingSets drawingSets, boolean copyCanvas){
        this.uuid = UUID.randomUUID();
        this.canvas = copyCanvas ? new SimpleCanvas(canvas) : canvas;
        this.drawingSets = drawingSets;
        this.geometries = new ArrayList<>();
        this.groups = new HashMap<>();
        this.metadata = new MetadataMap(new HashMap<>());
    }

    /**
     * Copies the base groups of the plotted drawing only
     */
    public void copyBase(PlottedDrawing reference){
        metadata = new MetadataMap(new ConcurrentHashMap<>(reference.metadata.data));
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
        this.metadata.setMetadata(metadata, value);
    }

    public <T> T getMetadata(Metadata<T> metadata){
        return this.metadata.getMetadata(metadata);
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

    public long getDisplayedVertexCount(){
        if(displayedVertexCount == -1){
            return getVertexCount();
        }
        return displayedVertexCount;
    }

    public int getGeometryCount(){
        return geometries.size();
    }

    public long getVertexCount(){
        return vertexCount;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addGeometry(IGeometry geometry) {
        if(geometry == null){
            return;
        }
        geometry.setGeometryIndex(geometries.size());
        geometries.add(geometry);
        vertexCount += geometry.getVertexCount();

        addGeometryToGroups(geometry);
    }

    public void addGeometry(IGeometry geometry, PlottedGroup group) {
        if(geometry == null){
            return;
        }
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

            //Force refresh the geometry stats
            perPenGeometryStats = getPerPenGeometryStats(this);
        }
    }

    public Map<ObservableDrawingPen, Integer> getPerPenGeometryStats(){
        if(perPenGeometryStats == null){
            perPenGeometryStats = getPerPenGeometryStats(this);
        }
        return perPenGeometryStats;
    }

    public List<ObservableDrawingPen> getAllPens(){
        List<ObservableDrawingPen> allPens = new ArrayList<>();
        drawingSets.drawingSetSlots.forEach(drawingSet -> allPens.addAll(drawingSet.pens));
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

        drawingSets.sort(Comparator.comparingInt(set -> this.drawingSets.drawingSetSlots.indexOf(set)));

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
            if(pen.getPenNumber() != i){
                pen.setPenNumber(i); //update pens number based on position
            }
        }
    }

    public static Map<ObservableDrawingPen, Integer> getPerPenGeometryStats(PlottedDrawing plottedDrawing){
        Map<PlottedGroup, Map<Integer, Integer>> perGroupStats = new HashMap<>();
        Map<ObservableDrawingPen, Integer> perPenStats = new HashMap<>();

        int actualVertexCount = 0;

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
            if(geometry.getPenIndex() >= 0 && geometry.getGeometryIndex() >= plottedDrawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= plottedDrawing.getDisplayedShapeMax()){
                Map<Integer, Integer> stats = perGroupStats.get(plottedDrawing.getPlottedGroup(geometry.getGroupID()));
                if(stats != null){
                    stats.putIfAbsent(geometry.getPenIndex(), 0);
                    stats.put(geometry.getPenIndex(), stats.get(geometry.getPenIndex())+1);
                }
                actualVertexCount += geometry.getVertexCount();
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

        plottedDrawing.displayedVertexCount = actualVertexCount;
        return perPenStats;
    }

    /**
     * Applies the Geometry Stats from this {@link PlottedDrawing} to all of the {@link ObservableDrawingPen}s in use
     * @param drawing the drawing to take the geometry stats from
     */
    public static void updatePerPenGeometryStats(PlottedDrawing drawing){
        drawing.getPerPenGeometryStats().forEach((pen, count) -> {
            pen.setGeometryStats(count, drawing.getGeometryCount());
        });
    }

    public static boolean preDistributionSetup(DistributionSet set){
        updatePenNumbers(set);
        if(set.getGeometryList().isEmpty()){
            return false;
        }
        return true;
    }

    /**updates every pen's unique number, and sets the correct pen number for every line based on their weighted distribution*/
    public static void updateEvenDistribution(DistributionSet set, boolean weighted){
        if(!preDistributionSetup(set)){
            return;
        }

        List<ObservableDrawingPen> renderOrder = set.source.drawingSet.getRenderOrderEnabled();

        int[] weights = getPenWeights(renderOrder, weighted);
        int totalWeight = getTotalWeight(weights);

        int visibleShapeCount = 0;
        for(IGeometry geometry : set.getGeometryList()){
            if(geometry.getGeometryIndex() >= set.plottedDrawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= set.plottedDrawing.getDisplayedShapeMax()){ //TODO MAKE THIS A FILTER THING!!
                visibleShapeCount ++;
            }
        }

        int currentIndex = 0;
        order: for(int i = 0; i < renderOrder.size(); i++){
            ObservableDrawingPen pen = renderOrder.get(i);
            if(pen.isEnabled()){
                //update percentage
                float percentage = (float)weights[i] / totalWeight;

                //update geometry count
                int geometriesPerPen = (int)(percentage * visibleShapeCount);
                int currentCount = 0;

                //set pen references
                for (; currentIndex < set.getGeometryList().size(); currentIndex++) {
                    IGeometry geometry = set.getGeometryList().get(currentIndex);
                    if(geometry.getGeometryIndex() >= set.plottedDrawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= set.plottedDrawing.getDisplayedShapeMax()) { //TODO MAKE THIS A FILTER THING!!
                        geometry.setPenIndex(pen.getPenNumber());
                        currentCount++;
                    }else{
                        geometry.setPenIndex(-1);
                    }
                    if(currentCount >= geometriesPerPen && i != renderOrder.size()-1){ // if it's the last pen ignore the geometries per pen count;
                        continue order;
                    }
                }
            }
        }
    }

    public static void updateRandomDistribution(DistributionSet set, boolean weighted){
        if(!preDistributionSetup(set)){
            return;
        }

        List<ObservableDrawingPen> renderOrder = set.source.drawingSet.getRenderOrderEnabled();
        int[] weights = getPenWeights(renderOrder, weighted);
        int totalWeight = getTotalWeight(weights);

        Random rand = new Random(0);

        for(IGeometry geometry : set.getGeometryList()){
            geometry.setPenIndex(getRandomIndexWeighted(rand, weights, totalWeight));
        }
    }

    public static void updateLuminanceDistribution(DistributionSet set, boolean weighted){
        if(!preDistributionSetup(set)){
            return;
        }

        List<ObservableDrawingPen> renderOrder = set.source.drawingSet.getRenderOrderEnabled();
        int[] weights = getPenWeights(renderOrder, weighted);
        int totalWeight = getTotalWeight(weights);

        List<Squiggle> squiggles = getSquigglesFromGeometries(set.getGeometryList(), new ArrayList<>(), set.plottedDrawing.getDisplayedShapeMin(), set.plottedDrawing.getDisplayedShapeMax(), 500);
        squiggles.sort(Comparator.comparingDouble(Squiggle::getAverageLuminance));

        double lumMin = Integer.MAX_VALUE;
        double lumMax = 0;

        for(Squiggle squiggle : squiggles){
            lumMin = Math.min(lumMin, squiggle.getAverageLuminance());
            lumMax = Math.max(lumMax, squiggle.getAverageLuminance());
        }

        double penLumMin = Integer.MAX_VALUE;
        double penLumMax = 0;

        for(ObservableDrawingPen pen : renderOrder){
            float lum = ImageTools.getPerceivedLuminanceFromRGB(pen.getARGB());
            penLumMin = Math.min(penLumMin, lum);
            penLumMax = Math.max(penLumMax, lum);
        }



        int visibleShapeCount = 0;
        for(IGeometry geometry : set.getGeometryList()){
            if(geometry.getGeometryIndex() >= set.plottedDrawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= set.plottedDrawing.getDisplayedShapeMax()){ //TODO MAKE THIS A FILTER THING!!
                visibleShapeCount ++;
            }
        }


        double lumRange = lumMax-lumMin;

        double currentLuminanceThreshold = lumMin;
        int currentIndex = 0;

        order: for(int i = 0; i < renderOrder.size(); i++){
            ObservableDrawingPen pen = renderOrder.get(i);
            float percentage = (float)weights[i] / totalWeight;


            //update geometry count
            int geometriesPerPen = (int)(percentage * visibleShapeCount);
            int currentCount = 0;
            /*
            //set pen references
            while (currentIndex < squiggles.size()){
                Squiggle squiggle = squiggles.get(currentIndex);
                squiggle.geometries.forEach(s -> s.setPenIndex(pen.getPenNumber()));
                currentIndex++;
                currentCount+=squiggle.geometries.size();
                if(currentCount >= geometriesPerPen && i != renderOrder.size()-1){ // if it's the last pen ignore the geometries per pen count;
                    continue order;
                }
            }
             */
            currentLuminanceThreshold +=  percentage * lumRange;

            while (currentIndex < squiggles.size()){
                Squiggle squiggle = squiggles.get(currentIndex);
                if(i==renderOrder.size()-1 || squiggle.getAverageLuminance() < currentLuminanceThreshold){
                    squiggle.geometries.forEach(s -> s.setPenIndex(pen.getPenNumber()));
                    currentCount+=squiggle.geometries.size();
                    currentIndex++;
                }else{
                    break;
                }
            }


            /*


             */
        }
    }

    public static void updateRandomSquiggleDistribution(DistributionSet set, boolean weighted){
        //Some PFMs don't create squiggles, and are instead multiple disconnected geometries, look for the flag bypass them to avoid unnecessary overhead / slow downs.
        if(set.source.pfmFactory != null && !set.source.pfmFactory.getFlags().getFlag(Flags.PFM_ALLOW_SQUIGGLE_DISTRIBUTION)){
            updateRandomDistribution(set, weighted);
            return;
        }

        if(!preDistributionSetup(set)){
            return;
        }

        List<ObservableDrawingPen> renderOrder = set.source.drawingSet.getRenderOrder();
        int[] weights = getPenWeights(renderOrder, weighted);
        int totalWeight = getTotalWeight(weights);

        Random rand = new Random(0);
        List<Squiggle> squiggles = getSquigglesFromGeometries(set.getGeometryList(), new ArrayList<>(), set.plottedDrawing.getDisplayedShapeMin(), set.plottedDrawing.getDisplayedShapeMax(), 500);
        int totalSquiggles = squiggles.size();

        for(int i = 0; i < renderOrder.size() ; i++){
            ObservableDrawingPen pen = renderOrder.get(i);
            float percentage = (float)weights[i] / totalWeight;
            int squigglesPerPen = (int)(percentage * totalSquiggles);

            for(int s = 0; i != renderOrder.size()-1 ? s < squigglesPerPen : !squiggles.isEmpty(); s++){
                Squiggle squiggle = squiggles.remove(rand.nextInt(0, squiggles.size()));
                squiggle.geometries.forEach(g -> g.setPenIndex(pen.getPenNumber()));
            }
        }
    }

    public boolean contains(Coordinate coord) {
        for(IGeometry geometry : geometries){
            if(geometry.getAWTShape().contains(coord.x, coord.y)){
                return true;
            }
        }
        return false;
    }

    public static class Squiggle{
        public double totalLuminance = 0;
        public List<IGeometry> geometries = new ArrayList<>();

        public double getAverageLuminance(){
            return totalLuminance / geometries.size();
        }
    }

    public static List<Squiggle> getSquigglesFromGeometries(List<IGeometry> geometries, List<Squiggle> dstSquiggles, int minIndex, int maxIndex, int maxSquiggleLength){
        IGeometry lastGeometry = null;
        dstSquiggles.clear();
        Squiggle squiggle = null;
        for(IGeometry geometry : geometries){
            if(geometry.getGeometryIndex() >= minIndex && geometry.getGeometryIndex() <= maxIndex) {
                boolean continuity = GeometryUtils.comparePathContinuity(lastGeometry, geometry);
                if (!continuity || (maxSquiggleLength != -1 && squiggle.geometries.size() >= maxSquiggleLength)) {
                    dstSquiggles.add(squiggle = new Squiggle());
                }
                squiggle.totalLuminance += ImageTools.getPerceivedLuminanceFromRGB(geometry.getSampledRGBA());
                squiggle.geometries.add(geometry);
                lastGeometry = geometry;
            }
        }
        return dstSquiggles;
    }


    public static int[] getPenWeights(List<ObservableDrawingPen> pens, boolean useWeighting){
        int[] weights = new int[pens.size()];
        for(int i = 0; i < pens.size(); i++){
            ObservableDrawingPen pen = pens.get(i);
            if(pen.isEnabled()){
                weights[i] = useWeighting ? pen.distributionWeight.get() : 100;
            }
        }
        return weights;
    }

    public static int getTotalWeight(int[] weights){
        int totalWeight = 0;
        for(int i : weights){
            totalWeight+=i;
        }
        return totalWeight;
    }

    public static int getRandomIndexWeighted(Random rand, int[] weights, int totalWeight){
        int weightedRand = rand.nextInt(totalWeight);
        int currentWeight = 0;
        for(int w = 0; w < weights.length; w++){
            currentWeight += weights[w];
            if(weightedRand < currentWeight){
                return w;
            }
        }
        return 0;
    }

    public static void updatePreConfiguredPenDistribution(DistributionSet set){

        updatePenNumbers(set);

        for(PlottedGroup group : set.plottedGroups){

            Map<Integer, Integer> remapIndexMap = new LinkedHashMap<>();
            pens: for(Map.Entry<Integer, ObservableDrawingPen> entry : group.originalDrawingSetOrder.entrySet()){
                int originalIndex = entry.getKey();
                int currentIndex = group.drawingSet.pens.indexOf(entry.getValue());
                if(currentIndex != -1){
                    remapIndexMap.put(originalIndex, currentIndex);
                    continue ;
                }

                //The pen doesn't exist anymore so lets try and find the most suitable match from it's code name
                List<ObservableDrawingPen> matches = group.drawingSet.getMatchingPens(entry.getValue());
                if(matches.isEmpty()){
                    remapIndexMap.put(originalIndex, -1);
                    continue;
                }
                if(matches.size() > 1) {
                    //if there are multiple matches, see if the original index still exists
                    for (ObservableDrawingPen pen : matches) {
                        if (pen.getPenNumber() == originalIndex) {
                            continue pens;
                        }
                    }
                }
                int newIndex = matches.get(0).getPenNumber();
                if(originalIndex != newIndex){
                    remapIndexMap.put(originalIndex, newIndex);
                }
            }

            for(IGeometry geometry : group.geometries){
                int originalIndex = geometry.getPFMPenIndex();
                int currentIndex = remapIndexMap.getOrDefault(originalIndex, originalIndex);
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

    public DrawingStats getOrCreateDrawingStats(){
        DrawingStats stats = getMetadata(Register.INSTANCE.DRAWING_STATS);
        if(stats == null){
            stats = new DrawingStats(this);
            setMetadata(Register.INSTANCE.DRAWING_STATS, stats);
        }
        return getMetadata(Register.INSTANCE.DRAWING_STATS);
    }

    @Override
    public String toString() {
        return "Drawing: %s %s x %s %s, Shapes: %s".formatted(canvas.getWidth(), canvas.getUnits().getSuffix(), canvas.getHeight(), canvas.getUnits().getSuffix(), getGeometryCount());
    }
}