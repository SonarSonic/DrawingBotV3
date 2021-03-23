package drawingbot.geom;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICustomPen;
import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.presets.types.ConfigApplicationSettings;
import drawingbot.geom.basic.*;
import drawingbot.utils.Units;
import drawingbot.utils.Utils;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.linearref.LinearGeometryBuilder;
import org.locationtech.jts.noding.*;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.linemerge.LineSequencer;
import org.locationtech.jts.operation.overlay.snap.LineStringSnapper;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class GeometryUtils {

    public static GeometryFactory factory = new GeometryFactory();

    public static Map<Integer, List<IGeometry>> getGeometriesForExportTask(ExportTask task, IGeometryFilter filter){
        if(!task.format.isVector || task.plottingTask.pfmFactory.bypassOptimisation){
            return getBasicGeometriesForExport(task.plottingTask.plottedDrawing.geometries, filter, task.plottingTask.createPrintTransform(), task.plottingTask.plottedDrawing.drawingPenSet);
        }
        return getOptimisedGeometriesForExport(task.plottingTask.plottedDrawing.geometries, filter, task.plottingTask.createPrintTransform(), task.plottingTask.plottedDrawing.drawingPenSet);
    }

    public static Map<Integer, List<IGeometry>> getOptimisedGeometriesForExport(List<IGeometry> geometries, IGeometryFilter geometryFilter, AffineTransform printTransform, ObservableDrawingSet drawingSet) {

        Map<Integer, List<IGeometry>> combinedGeometry = combineBasicGeometries(geometryFilter, drawingSet, geometries);

        if(ConfigFileHandler.getApplicationSettings().pathOptimisationEnabled){

            DrawingBotV3.logger.info("--------- Geometry - Pre-Optimisation ---------");
            printEstimatedTravelDistance(combinedGeometry, printTransform);

            AffineTransform toJTS = AffineTransform.getScaleInstance(printTransform.getScaleX(), printTransform.getScaleY());
            AffineTransform fromJTS = AffineTransform.getScaleInstance(1/printTransform.getScaleX(), 1/printTransform.getScaleY());
            for(Map.Entry<Integer, List<IGeometry>> entry : combinedGeometry.entrySet()){
                ObservableDrawingPen pen = drawingSet.getPen(entry.getKey());
                if(!(pen.source instanceof ICustomPen)){
                    entry.setValue(optimiseBasicGeometry(entry.getValue(), toJTS, fromJTS));
                }
            }

            DrawingBotV3.logger.info("--------- Geometry - Post-Optimisation ---------");
            printEstimatedTravelDistance(combinedGeometry, printTransform);

        }

        return combinedGeometry;
    }
    public static Map<Integer, List<IGeometry>> getBasicGeometriesForExport(List<IGeometry> geometries, IGeometryFilter geometryFilter, AffineTransform printTransform, ObservableDrawingSet drawingSet) {
        return combineBasicGeometries(geometryFilter, drawingSet, geometries);
    }

    /**
     * Combines any obvious path elements with obvious continuity
     */
    private static Map<Integer, List<IGeometry>> combineBasicGeometries(IGeometryFilter geometryFilter, ObservableDrawingSet drawingSet, List<IGeometry> geometries){
        Map<Integer, List<IGeometry>> optimised = new HashMap<>();

        for(ObservableDrawingPen set : drawingSet.getPens()){
            optimised.put(set.penNumber.get(), new ArrayList<>());
        }

        for(IGeometry geometry : geometries){
            List<IGeometry> subList = optimised.get(geometry.getPenIndex());
            ObservableDrawingPen pen = drawingSet.getPen(geometry.getPenIndex());
            if(geometryFilter.filter(geometry, pen)){
                if(geometry instanceof IPathElement){
                    IPathElement element = (IPathElement) geometry;
                    IGeometry lastGeometry = subList.isEmpty() ? null : subList.get(subList.size()-1);

                    if(lastGeometry instanceof GPath){
                        GPath gPath = (GPath) lastGeometry;
                        //check the render colour and continuity if they match, add it too the path
                        if(GeometryUtils.compareRenderColour(pen, gPath, element) && GeometryUtils.comparePathContinuity(gPath, element)){
                            element.addToPath(false, gPath);
                            continue;
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
        }

        return optimised;
    }

    public static boolean compareRenderColour(ObservableDrawingPen pen, IGeometry geometry1, IGeometry geometry2){
        if(Objects.equals(geometry1.getPenIndex(), geometry2.getPenIndex())){
            int pathRGBA = geometry1.getCustomRGBA() != null ? pen.getCustomARGB(geometry1.getCustomRGBA()) : pen.getARGB();
            int geoRGBA = geometry2.getCustomRGBA() != null ? pen.getCustomARGB(geometry2.getCustomRGBA()) : pen.getARGB();
            return pathRGBA == geoRGBA;
        }
        return false;
    }

    public static boolean comparePathContinuity(GPath path, IPathElement element){
        return path.getCurrentPoint().equals(element.getP1());
    }


    public static List<IGeometry> optimiseBasicGeometry(List<IGeometry> geometries, AffineTransform toJTS, AffineTransform fromJTS) {
        if(geometries.isEmpty()){
            return new ArrayList<>();
        }
        List<LineString> lineStrings = new ArrayList<>();
        for(IGeometry g : geometries){
            toLineStrings(g, toJTS, lineStrings);
        }

        lineStrings = optimiseJTSGeometry(lineStrings);

        List<IGeometry> optimised = new ArrayList<>();
        for(LineString g : lineStrings){
            optimised.add(fromLineStrings(g, fromJTS));
        }
        return optimised;
    }


    /**
     * Performs the configured optimisation on the set of line strings
     */
    public static List<LineString> optimiseJTSGeometry(List<LineString> lineStrings){
        if(lineStrings.isEmpty()){
            return new ArrayList<>();
        }
        ConfigApplicationSettings settings = ConfigFileHandler.getApplicationSettings();

        if(settings.lineSimplifyEnabled){
            float tolerance = Units.convert(settings.lineSimplifyTolerance, settings.lineSimplifyUnits, Units.MILLIMETRES);
            lineStrings = lineSimplify(lineStrings, tolerance);
        }

        if(settings.lineMergingEnabled){
            float tolerance = Units.convert(settings.lineMergingTolerance, settings.lineMergingUnits, Units.MILLIMETRES);
            lineStrings = lineMerge(lineStrings, tolerance);
        }

        if(settings.lineFilteringEnabled){
            float tolerance = Units.convert(settings.lineFilteringTolerance, settings.lineFilteringUnits, Units.MILLIMETRES);
            lineStrings = lineFilter(lineStrings, tolerance);
        }

        if(settings.lineSortingEnabled){
            float tolerance = Units.convert(settings.lineSortingTolerance, settings.lineSortingUnits, Units.MILLIMETRES);
            lineStrings = lineSort(lineStrings, tolerance);
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
    public static List<LineString> lineFilter(List<LineString> lineStrings, double minLength){
        List<LineString> filtered = new ArrayList<>();
        for(LineString lineString : lineStrings){
            if(lineString.getLength() >= minLength){
                filtered.add(lineString);
            }
        }
        return filtered;
    }

    /**
     * Simplifies lines using a given tolerance.
     */
    public static List<LineString> lineSimplify(List<LineString> lineStrings, double tolerance){
        List<LineString> simplified = new ArrayList<>();
        for(LineString s : lineStrings){
            Geometry geometry = DouglasPeuckerSimplifier.simplify(s, tolerance);
            if(geometry instanceof LineString){
                simplified.add((LineString) geometry);
            }else{
                simplified.addAll(LinearComponentExtracter.getLines(geometry, true));
            }
        }
        return simplified;
    }

    /**
     * Merges lines at their start/end point within the given tolerance.
     */
    public static List<LineString> lineMerge(List<LineString> lineStrings, double tolerance){
        TSPSequencer sequencer = new TSPSequencer(lineStrings, tolerance);
        return sequencer.lineMerge();
    }

    /**
     * Orders lines to minimise air time, by finding the nearest line to the current point.
     * This is a simple version and doesn't provide a perfect solution
     */
    public static List<LineString> lineSort(List<LineString> lineStrings, double allowableDistance){
        TSPSequencer sequencer = new TSPSequencer(lineStrings, allowableDistance);
        return sequencer.lineSort();
    }

    /**
     * Not a very good approach to the Travelling Salesman Problem but suits the path finding algorithms currently in use, could definitely be improved, possibly with a nearest neighbour search combined with a KDTree!
     */
    public static class TSPSequencer {

        public List<LineString> lineStrings;
        public boolean[] sorted;
        public int sortedCount = 0;

        public double measuredDistance;
        public double allowableDistance;

        public TSPSequencer(List<LineString> lineStrings, double allowableDistance){
            this.lineStrings = lineStrings;
            this.sorted = new boolean[lineStrings.size()];
            this.allowableDistance = allowableDistance;
        }

        protected List<LineString> lineSort(){
            List<LineString> sortedList = new ArrayList<>();

            LineString first = lineStrings.get(0);
            sorted[0] = true;
            sortedCount++;
            sortedList.add(first);

            while(sortedCount < lineStrings.size()){
                LineString last = sortedList.get(sortedList.size()-1);
                Coordinate endCoord = last.getCoordinateN(last.getNumPoints()-1);
                LineString nearest = getNearestLineString(endCoord);
                sortedList.add(nearest);
            }
            return sortedList;
        }

        protected List<LineString> lineMerge(){
            LinearGeometryBuilder builder = new LinearGeometryBuilder(factory);
            LineString first = lineStrings.get(0);
            sorted[0] = true;
            sortedCount++;
            mergeLine(builder, first);

            while(sortedCount < lineStrings.size()){
                LineString nearest = getNearestLineString(builder.getLastCoordinate());
                if(measuredDistance > allowableDistance){
                    builder.endLine();
                }
                mergeLine(builder, nearest);
            }
            return LinearComponentExtracter.getLines(builder.getGeometry(), true);
        }

        public void mergeLine(LinearGeometryBuilder builder, LineString string){
            for(Coordinate coordinate : string.getCoordinates()){
                builder.add(coordinate, false);
            }
        }

        protected LineString getNearestLineString(Coordinate point){
            LineString nearest = null;
            measuredDistance = -1;
            boolean reversed = false;
            int index = 0;
            for(int i = 0; i < sorted.length; i++){
                if(!sorted[i]){
                    LineString lineString = lineStrings.get(i);
                    Coordinate startCoord = lineString.getCoordinateN(0);
                    double toStart = point.distance(startCoord);
                    if(measuredDistance == -1 || toStart < measuredDistance){
                        measuredDistance = toStart;
                        nearest = lineString;
                        reversed = false;
                        index = i;
                        if(measuredDistance <= allowableDistance){
                            break;
                        }
                    }
                    Coordinate endCoord = lineString.getCoordinateN(lineString.getNumPoints()-1);
                    double toEnd = point.distance(endCoord);
                    if(measuredDistance == -1 || toEnd < measuredDistance){
                        measuredDistance = toEnd;
                        nearest = lineString;
                        reversed = true;
                        index = i;
                        if(measuredDistance <= allowableDistance){
                            break;
                        }
                    }
                }
            }
            sorted[index] = true;
            sortedCount++;
            return reversed ? nearest.reverse() : nearest;
        }

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
        for(int i = 0 ; i < coordinates.size();i ++){
            lineStrings.add(factory.createLineString(coordinates.get(i)));
        }
    }

    public static IGeometry fromLineStrings(LineString string, AffineTransform transform){
        GShape shape = new GShape(new ShapeWriter().toShape(string));
        shape.transform(transform);
        return shape;
    }

    public static GPath geometryToGPath(Geometry string, AffineTransform transform){
        return new GPath(new ShapeWriter().toShape(string), transform);
    }

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
