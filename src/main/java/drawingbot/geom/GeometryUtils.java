package drawingbot.geom;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICustomPen;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.spatial.STRTreeSequencer;
import drawingbot.geom.spatial.STRTreeSequencerLineString;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.presets.types.ConfigApplicationSettings;
import drawingbot.geom.basic.*;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.ProgressCallback;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.Utils;
import javafx.scene.canvas.GraphicsContext;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class GeometryUtils {

    public static GeometryFactory factory = new GeometryFactory();

    public static Map<Integer, List<IGeometry>> getGeometriesForExportTask(ExportTask task, IGeometryFilter filter, boolean forceBypassOptimisation){

        ProgressCallback progressCallback = new ProgressCallback();

        progressCallback.progressCallback = p -> task.updateProgress(p, 1);
        progressCallback.messageCallback = task::updateMessage;

        if(!task.exportHandler.isVector || forceBypassOptimisation){
            return getBasicGeometriesForExport(task, task.plottingTask.plottedDrawing.geometries, filter, task.plottingTask.createPrintTransform(), task.plottingTask.plottedDrawing.drawingPenSet, progressCallback);
        }

        Map<Integer, List<List<IGeometry>>> perGroupOptimisedGeometries = new HashMap<>(); //pen id -> list of geometries with matching group and pen id

        for(Map.Entry<Integer, List<IGeometry>> groupEntry : task.plottingTask.plottedDrawing.groups.entrySet()){
           // progressCallback.startTask();
            Map<Integer, List<IGeometry>> optimisedGeometries = getOptimisedGeometriesForExport(task, task.plottingTask.plottedDrawing.getGroupPFMType(groupEntry.getKey()), groupEntry.getValue(), filter, task.plottingTask.createPrintTransform(), task.plottingTask.plottedDrawing.drawingPenSet, progressCallback);
            for(Map.Entry<Integer, List<IGeometry>> penGroupEntry : optimisedGeometries.entrySet()){
                if(!penGroupEntry.getValue().isEmpty()){
                    perGroupOptimisedGeometries.putIfAbsent(penGroupEntry.getKey(), new ArrayList<>());
                    perGroupOptimisedGeometries.get(penGroupEntry.getKey()).add(penGroupEntry.getValue());
                }
            }
            //progressCallback.finishTask();
        }

        Map<Integer, List<IGeometry>> optimisedGeometries = new HashMap<>();

        //place empty lists for each pen
        for(ObservableDrawingPen set : task.plottingTask.plottedDrawing.drawingPenSet.getPens()){
            optimisedGeometries.put(set.penNumber.get(), new ArrayList<>());
        }

        progressCallback.updateMessage("Optimising Geometry Groups");
        for(Map.Entry<Integer, List<List<IGeometry>>> groupEntry : perGroupOptimisedGeometries.entrySet()){
            progressCallback.startTask();

            STRTreeSequencer<List<IGeometry>> sequencer = new STRTreeSequencer<>(groupEntry.getValue(), 0) {
                @Override
                protected Coordinate getStartCoordinateFromGeometry(List<drawingbot.geom.basic.IGeometry> geometry) {
                    return geometry.get(0).getOriginCoordinate();
                }

                @Override
                protected Coordinate getEndCoordinateFromGeometry(List<drawingbot.geom.basic.IGeometry> geometry) {
                    return geometry.get(0).getOriginCoordinate();
                }
            };

            sequencer.setProgressCallback(progressCallback);

            List<List<IGeometry>> sortedGroups = groupEntry.setValue(sequencer.sort());
            List<IGeometry> perPenList = new ArrayList<>();
            sortedGroups.forEach(perPenList::addAll);
            optimisedGeometries.put(groupEntry.getKey(), perPenList);

            progressCallback.finishTask();
        }

        return optimisedGeometries;
    }

    public static Map<Integer, List<IGeometry>> getOptimisedGeometriesForExport(ExportTask task, @Nullable PFMFactory<?> factory, List<IGeometry> geometries, IGeometryFilter geometryFilter, AffineTransform printTransform, ObservableDrawingSet drawingSet, ProgressCallback progressCallback) {


        Map<Integer, List<IGeometry>> combinedGeometry = combineBasicGeometries(task.plottingTask, geometryFilter, drawingSet, geometries, progressCallback, false);

        if(ConfigFileHandler.getApplicationSettings().pathOptimisationEnabled){

            //DrawingBotV3.logger.info("--------- Geometry - Pre-Optimisation ---------");
            //printEstimatedTravelDistance(combinedGeometry, printTransform);

            if(factory != null && !factory.bypassOptimisation){
                AffineTransform toJTS = AffineTransform.getScaleInstance(printTransform.getScaleX(), printTransform.getScaleY());
                AffineTransform fromJTS = AffineTransform.getScaleInstance(1/printTransform.getScaleX(), 1/printTransform.getScaleY());
                for(Map.Entry<Integer, List<IGeometry>> entry : combinedGeometry.entrySet()){
                    ObservableDrawingPen pen = drawingSet.getPen(entry.getKey());
                    if(!(pen.source instanceof ICustomPen)){
                        entry.setValue(optimiseBasicGeometry(entry.getValue(), toJTS, fromJTS, progressCallback));
                    }
                }
            }else {
                ConfigApplicationSettings settings = ConfigFileHandler.getApplicationSettings();

                if(settings.lineSortingEnabled){
                    float tolerance = UnitsLength.convert(settings.lineSortingTolerance, settings.lineSortingUnits, UnitsLength.MILLIMETRES);

                    for(Map.Entry<Integer, List<IGeometry>> entry : combinedGeometry.entrySet()){
                        STRTreeSequencer.IGeometry sequencer = new STRTreeSequencer.IGeometry(entry.getValue(), tolerance);
                        entry.setValue(sequencer.sort());
                    }
                }
                return combinedGeometry;
            }


            //DrawingBotV3.logger.info("--------- Geometry - Post-Optimisation ---------");
            //printEstimatedTravelDistance(combinedGeometry, printTransform);

        }

        return combinedGeometry;
    }

    public static Map<Integer, List<IGeometry>> getBasicGeometriesForExport(ExportTask task, List<IGeometry> geometries, IGeometryFilter geometryFilter, AffineTransform printTransform, ObservableDrawingSet drawingSet, ProgressCallback callback) {
        return combineBasicGeometries(task.plottingTask, geometryFilter, drawingSet, geometries, callback, false);
    }

    /**
     * Combines any obvious path elements with obvious continuity
     */
    public static Map<Integer, List<IGeometry>> combineBasicGeometries(PlottingTask plottingTask, IGeometryFilter geometryFilter, ObservableDrawingSet drawingSet, List<IGeometry> geometries, @Nullable ProgressCallback callback, boolean includeMultipleMoves){
        Map<Integer, List<IGeometry>> optimised = new HashMap<>();

        for(ObservableDrawingPen set : drawingSet.getPens()){
            optimised.put(set.penNumber.get(), new ArrayList<>());
        }

        int index = 0;
        for(IGeometry geometry : geometries){
            List<IGeometry> subList = optimised.get(geometry.getPenIndex());
            ObservableDrawingPen pen = drawingSet.getPen(geometry.getPenIndex());
            if(geometryFilter.filter(plottingTask.plottedDrawing, geometry, pen)){
                if(geometry instanceof IPathElement){
                    IPathElement element = (IPathElement) geometry;
                    IGeometry lastGeometry = subList.isEmpty() ? null : subList.get(subList.size()-1);

                    if(lastGeometry instanceof GPath){
                        GPath gPath = (GPath) lastGeometry;
                        //check the render colour and continuity if they match, add it too the path
                        if(GeometryUtils.compareRenderColour(pen, gPath, element)){
                            boolean continuity = GeometryUtils.comparePathContinuity(gPath, element);
                            if(continuity){
                                element.addToPath(false, gPath);
                                continue;
                            }else if(includeMultipleMoves){
                                element.addToPath(true, gPath);
                                continue;
                            }
                        }
                    }
                    //if the last geometry isn't a GPath or the element can't be added add a new GPath
                    if(element instanceof GPath){
                        subList.add(element);
                    }else{
                        subList.add(new GPath(element));
                    }
                }else{
                    subList.add(geometry);
                }
            }
            index++;
            if(callback != null){
                callback.updateProgress((float) index / geometries.size());
            }
        }

        return optimised;
    }

    public static boolean compareRenderColour(ObservableDrawingPen pen, IGeometry geometry1, IGeometry geometry2){
        if(Objects.equals(geometry1.getPenIndex(), geometry2.getPenIndex())){
            int pathRGBA = geometry1.getSampledRGBA() != -1 ? pen.getCustomARGB(geometry1.getSampledRGBA()) : pen.getARGB();
            int geoRGBA = geometry2.getSampledRGBA() != -1 ? pen.getCustomARGB(geometry2.getSampledRGBA()) : pen.getARGB();
            return pathRGBA == geoRGBA;
        }
        return false;
    }

    public static boolean comparePathContinuity(IGeometry lastGeometry, IGeometry nextGeometry, boolean flipped){
        return flipped ? comparePathContinuity(nextGeometry, lastGeometry) : comparePathContinuity(lastGeometry, nextGeometry);
    }

    public static boolean comparePathContinuity(IGeometry lastGeometry, IGeometry nextGeometry){
        if(lastGeometry == null || lastGeometry.getGroupID() != nextGeometry.getGroupID()){
            return false;
        }
        if(nextGeometry instanceof IPathElement){
            IPathElement element = (IPathElement) nextGeometry;

            if(lastGeometry instanceof GPath){
                GPath path = (GPath) lastGeometry;
                return path.getCurrentPoint().equals(element.getP1());
            }

            if(lastGeometry instanceof IPathElement){
                return ((IPathElement) lastGeometry).getP2().equals(element.getP1());
            }
        }
        return false;
    }

    public static boolean comparePathContinuityFlipped(IGeometry lastGeometry, IGeometry nextGeometry){
        if(lastGeometry == null){
            return false;
        }
        if(nextGeometry instanceof IPathElement){
            IPathElement element = (IPathElement) nextGeometry;
            if(lastGeometry instanceof IPathElement){
                return ((IPathElement) lastGeometry).getP1().equals(element.getP2());
            }
            if(lastGeometry instanceof GPath){
                Coordinate coordinate = ((GPath) lastGeometry).getEndCoordinate();
                return ((float)coordinate.x) == element.getP2().getX() && ((float)coordinate.y) == element.getP2().getY();
            }
        }
        return false;
    }

    public static List<IGeometry> optimiseBasicGeometry(List<IGeometry> geometries, AffineTransform toJTS, AffineTransform fromJTS, ProgressCallback progressCallback) {
        if(geometries.isEmpty()){
            return new ArrayList<>();
        }
        List<LineString> lineStrings = new ArrayList<>();
        for(IGeometry g : geometries){
            toLineStrings(g, toJTS, lineStrings);
        }

        lineStrings = optimiseJTSGeometry(lineStrings, progressCallback);

        List<IGeometry> optimised = new ArrayList<>();
        for(LineString g : lineStrings){
            optimised.add(fromLineStrings(g, fromJTS));
        }
        return optimised;
    }


    /**
     * Performs the configured optimisation on the set of line strings
     */
    public static List<LineString> optimiseJTSGeometry(List<LineString> lineStrings, ProgressCallback progressCallback){
        if(lineStrings.isEmpty()){
            return new ArrayList<>();
        }
        ConfigApplicationSettings settings = ConfigFileHandler.getApplicationSettings();

        if(settings.lineSimplifyEnabled){
            progressCallback.updateTitle("Line Simplifying: ");
            float tolerance = UnitsLength.convert(settings.lineSimplifyTolerance, settings.lineSimplifyUnits, UnitsLength.MILLIMETRES);
            lineStrings = lineSimplify(lineStrings, tolerance, progressCallback);
        }

        if(settings.lineMergingEnabled){
            progressCallback.updateTitle("Line Merging: ");
            float tolerance = UnitsLength.convert(settings.lineMergingTolerance, settings.lineMergingUnits, UnitsLength.MILLIMETRES);
            lineStrings = lineMerge(lineStrings, tolerance, progressCallback, 3);
        }

        if(settings.lineFilteringEnabled){
            progressCallback.updateTitle("Line Filtering: ");
            float tolerance = UnitsLength.convert(settings.lineFilteringTolerance, settings.lineFilteringUnits, UnitsLength.MILLIMETRES);
            lineStrings = lineFilter(lineStrings, tolerance, progressCallback);
        }

        if(settings.lineSortingEnabled){
            progressCallback.updateTitle("Line Sorting: ");
            float tolerance = UnitsLength.convert(settings.lineSortingTolerance, settings.lineSortingUnits, UnitsLength.MILLIMETRES);
            lineStrings = lineSort(lineStrings, tolerance, progressCallback);
        }
        return lineStrings;
    }

    public static List<LineString> extractLines(Geometry geometry){
        return (List<LineString>)LinearComponentExtracter.getLines(geometry, true);
    }

    public static List<LineString> extractSegments(List<LineString> lineStrings){
        List<LineString> segments = new ArrayList<>();
        for(LineString line : lineStrings){
            for (int i = 1; i < line.getNumPoints(); i++) {
                LineString seg = factory.createLineString(new Coordinate[] { line.getCoordinateN(i-1), line.getCoordinateN(i)});
                segments.add(seg);
            }
        }
        return segments;
    }

    public static List<Coordinate> extractCoordinates(List<Geometry> geometries){
        List<Coordinate> coordinates = new ArrayList<>();
        for(Geometry geometry : geometries){
            coordinates.addAll(Arrays.asList(geometry.getCoordinates()));
        }
        return coordinates;
    }

    /**
     * Filters out line strings with a length below the given minimum length
     */
    public static List<LineString> lineFilter(List<LineString> lineStrings, double minLength, ProgressCallback progressCallback){
        List<LineString> filtered = new ArrayList<>();
        int index = 0;
        for(LineString lineString : lineStrings){
            if(lineString.getLength() >= minLength){
                filtered.add(lineString);
            }
            index++;
            progressCallback.updateProgress((float)index / lineStrings.size());
        }
        return filtered;
    }

    /**
     * Simplifies lines using a given tolerance.
     */
    public static List<LineString> lineSimplify(List<LineString> lineStrings, double tolerance, ProgressCallback progressCallback){
        List<LineString> simplified = new ArrayList<>();
        int index = 0;
        for(LineString s : lineStrings){
            Geometry geometry = DouglasPeuckerSimplifier.simplify(s, tolerance);
            if(geometry instanceof LineString){
                simplified.add((LineString) geometry);
            }else{
                simplified.addAll(LinearComponentExtracter.getLines(geometry, true));
            }
            index++;
            progressCallback.updateProgress((float)index / lineStrings.size());
        }
        return simplified;
    }

    /**
     * Merges lines at their start/end point within the given tolerance.
     */
    public static List<LineString> lineMerge(List<LineString> lineStrings, double tolerance, ProgressCallback progressCallback, int pass){
        for(int i = 0; i < pass; i++){
            progressCallback.updateTitle("Line Merging " + (i+1) + " of " + pass + ": ");
            STRTreeSequencerLineString sequencer = new STRTreeSequencerLineString(lineStrings, tolerance);
            sequencer.setProgressCallback(progressCallback);
            lineStrings = sequencer.merge();
        }
        return lineStrings;
    }

    /**
     * Orders lines to minimise air time, by finding the nearest line to the current point.
     * This is a simple version and doesn't provide a perfect solution
     */
    public static List<LineString> lineSort(List<LineString> lineStrings, double allowableDistance, ProgressCallback progressCallback){
        STRTreeSequencerLineString sequencer = new STRTreeSequencerLineString(lineStrings, allowableDistance);
        sequencer.setProgressCallback(progressCallback);
        return sequencer.sort();

    }


    public static void printEstimatedTravelDistance(Map<Integer, List<IGeometry>> geometries, AffineTransform printTransform){
        double distanceUp = 0;
        double distanceDown = 0;
        int geometryCount = 0;
        long coordCount = 0;

        double maxX = 0;
        double maxY = 0;

        float lastX = 0;
        float lastY = 0;

        for(List<IGeometry> geometryList : geometries.values()){
            for(IGeometry geometry : geometryList){
                PathIterator it = geometry.getAWTShape().getPathIterator(printTransform);
                float[] coords = new float[6];
                while(!it.isDone()){
                    int type = it.currentSegment(coords);
                    switch (type){
                        case PathIterator.SEG_MOVETO:
                            distanceUp += Point2D.distance(lastX, lastY, coords[0], coords[1]);
                            break;
                        case PathIterator.SEG_LINETO:
                            distanceDown += Point2D.distance(lastX, lastY, coords[0], coords[1]);
                            break;
                    }
                    lastX = coords[0];
                    lastY = coords[1];
                    maxX = Math.max(maxX, lastX);
                    maxY = Math.max(maxY, lastY);
                    it.next();
                    coordCount++;
                }
                geometryCount++;
            }
        }

        DrawingBotV3.logger.fine("Geometry: Total travel distance: " + (distanceDown + distanceUp)/100 + " m");
        DrawingBotV3.logger.fine("Geometry: Total active distance: " + distanceDown/100 + " m");
        DrawingBotV3.logger.fine("Geometry: Total airtime distance: " + distanceUp/100 + " m");
        DrawingBotV3.logger.fine("Geometry: Line Count / Tool lifts: " + geometryCount);
        DrawingBotV3.logger.fine("Geometry: Vertex Count: " + coordCount);
        DrawingBotV3.logger.fine("Geometry: Max X: " + Utils.roundToPrecision(maxX, 3) + " mm" + " Max Y: " + Utils.roundToPrecision(maxY, 3) + " mm");
    }

    public static void printEstimatedTravelDistance(List<LineString> lineStrings){
        double distanceUp = 0;
        double distanceDown = 0;
        int lineCount = 0;
        long coordCount = 0;

        double maxX = 0;
        double maxY = 0;

        Coordinate last = new CoordinateXY(0, 0);

        for(LineString string : lineStrings){
            boolean addedFirst = false;
            for(Coordinate coord : string.getCoordinateSequence().toCoordinateArray()){
                if(!addedFirst){
                    distanceUp += last.distance(coord);
                    addedFirst = true;
                }else{
                    distanceDown += last.distance(coord);
                }
                maxX = Math.max(maxX, coord.x);
                maxY = Math.max(maxY, coord.y);
                last = coord;
                coordCount++;
            }
            lineCount++;
        }
        DrawingBotV3.logger.fine("Geometry: Total travel distance: " + (distanceDown + distanceUp)/100 + " m");
        DrawingBotV3.logger.fine("Geometry: Total active distance: " + distanceDown/100 + " m");
        DrawingBotV3.logger.fine("Geometry: Total airtime distance: " + distanceUp/100 + " m");
        DrawingBotV3.logger.fine("Geometry: Line Count / Tool lifts: " + lineCount);
        DrawingBotV3.logger.fine("Geometry: Vertex Count: " + coordCount);
        DrawingBotV3.logger.fine("Geometry: Max X: " + Utils.roundToPrecision(maxX, 3) + " mm" + " Max Y: " + Utils.roundToPrecision(maxY, 3) + " mm");
    }


    public static void toLineStrings(IGeometry geometry, AffineTransform transform, List<LineString> lineStrings){
        List<Coordinate[]> coordinates = ShapeReader.toCoordinates(new FlatteningPathIterator(geometry.getAWTShape().getPathIterator(transform), 6D));
        for (Coordinate[] coordinate : coordinates) {
            lineStrings.add(factory.createLineString(coordinate));
        }
    }

    public static IGeometry fromLineStrings(LineString string, AffineTransform transform){
        GShape shape = new GShape(new ShapeWriter().toShape(string));
        shape.transform(transform);
        return shape;
    }

    public static void lineStringToGLines(LineString lineString, Consumer<GLine> consumer){
        for(int i = 0; i < lineString.getNumPoints(); i++){
            Coordinate last = i == 0 ? null : lineString.getCoordinateN(i-1);
            Coordinate next = lineString.getCoordinateN(i);
            if(last != null && next != null){
                consumer.accept(new GLine((float)last.x, (float)last.y, (float)next.x, (float)next.y));
            }
        }
    }

    public static GPath geometryToGPath(Geometry string, AffineTransform transform){
        return new GPath(new ShapeWriter().toShape(string), transform);
    }

    public static void splitGPath(GPath gPath, Consumer<IGeometry> consumer){
        PathIterator pathIterator = gPath.getPathIterator(null);

        float lastMoveX = 0;
        float lastMoveY = 0;

        float currentX = 0;
        float currentY = 0;

        float[] coords = new float[6];
        while(!pathIterator.isDone()){
            int type = pathIterator.currentSegment(coords);
            IGeometry geometry = null;
            switch (type){
                case PathIterator.SEG_MOVETO:
                    currentX = lastMoveX = coords[0];
                    currentY = lastMoveY = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    geometry = new GLine(currentX, currentY, coords[0], coords[1]);
                    currentX = coords[0];
                    currentY = coords[1];
                    break;
                case PathIterator.SEG_QUADTO:
                    geometry = new GQuadCurve(currentX, currentY, coords[0], coords[1], coords[2], coords[3]);
                    currentX = coords[2];
                    currentY = coords[3];
                    break;
                case PathIterator.SEG_CUBICTO:
                    geometry = new GCubicCurve(currentX, currentY, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    currentX = coords[4];
                    currentY = coords[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    geometry = new GLine(currentX, currentY, lastMoveX, lastMoveY);
                    break;
            }
            if(geometry != null){
                geometry.setSampledRGBA(gPath.getSampledRGBA());
                geometry.setPenIndex(gPath.getPenIndex());
                geometry.setGroupID(gPath.getGroupID());
                consumer.accept(geometry);
            }

            pathIterator.next();
        }
    }

    public static String serializeCoords(float[] coords){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < coords.length; i++){
            stringBuilder.append(removeTrailingZeros(coords[i]));
            if(i != coords.length-1){
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static float[] deserializeCoords(String string){
        String[] values = string.split(",");
        float[] coords = new float[values.length];
        for(int i = 0; i < values.length; i++){
            coords[i] = Float.parseFloat(values[i]);
        }
        return coords;
    }

    public static String removeTrailingZeros(float f){
        String string = Float.toString(f);
        if(string.contains(".")){
            return string.replaceAll("0*$","").replaceAll("\\.$","");
        }
        return string;
    }

    public static int getSegmentCount(Shape shape){
        int segmentCount = 0;
        PathIterator iterator = shape.getPathIterator(null);
        while(!iterator.isDone()){
            iterator.next();
            segmentCount++;
        }
        return segmentCount;
    }

    @Deprecated
    public static int getVertexCount(Shape shape){
        int vertexCount = 0;
        PathIterator iterator = shape.getPathIterator(null);
        double[] coords = new double[6];
        while(!iterator.isDone()){
            int segType = iterator.currentSegment(coords);
            switch (segType){
                case PathIterator.SEG_QUADTO:
                    vertexCount+=3;
                    break;
                case PathIterator.SEG_CUBICTO:
                    vertexCount+=3;
                    break;
                default:
                    vertexCount+=1;
                    break;
            }
            vertexCount++;
            iterator.next();
        }
        return vertexCount;
    }


    public static int getTotalGeometries(Map<Integer, List<IGeometry>> geometryMap){
        int count = 0;
        for(List<IGeometry> geometryList : geometryMap.values()){
            count+=geometryList.size();
        }
        return count;
    }

    private static final double[] coords = new double[6];

    public static void renderAWTShapeToFX(GraphicsContext graphics, Shape s) {
        graphics.beginPath();
        PathIterator iterator = s.getPathIterator(null);
        while (!iterator.isDone()) {
            int segType = iterator.currentSegment(coords);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    graphics.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    graphics.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    graphics.quadraticCurveTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    graphics.bezierCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    graphics.closePath();
                    break;
                default:
                    throw new RuntimeException("Unrecognised segment type " + segType);
            }
            iterator.next();
        }
        graphics.stroke();
    }

}
