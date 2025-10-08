package drawingbot.geom;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.api.IProgressCallback;
import drawingbot.files.ExportTask;
import drawingbot.geom.operation.AbstractGeometryOperation;
import drawingbot.geom.operation.GeometryOperationOptimize;
import drawingbot.geom.operation.GeometryOperationSimplify;
import drawingbot.geom.operation.GeometryOperationSortGeometries;
import drawingbot.geom.shapes.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.PathBuilder;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.Utils;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LinearComponentExtracter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class GeometryUtils {

    public static GeometryFactory factory = new GeometryFactory(new PrecisionModel(1000));

    public static PlottedDrawing getOptimisedPlottedDrawing(ExportTask task, IGeometryFilter filter, boolean forceBypassOptimisation){

        IProgressCallback progressCallback = new IProgressCallback() {
            @Override
            public void updateTitle(String title) {
                task.updateMessage(title);
            }

            @Override
            public void updateMessage(String message) {
                task.updateMessage(message);
            }

            @Override
            public void updateProgress(double progress, double max) {
                task.updateProgress(progress, max);
            }
        };

        List<AbstractGeometryOperation> geometryOperations = getGeometryExportOperations(task, filter, forceBypassOptimisation);

        PlottedDrawing plottedDrawing = task.plottedDrawing;

        int i = 0;
        for(AbstractGeometryOperation operation : geometryOperations){
            if(i == 0 && operation.isDestructive()){
                PlottedDrawing newDrawing = operation.createPlottedDrawing(plottedDrawing);
                newDrawing.copyAll(plottedDrawing);
                plottedDrawing = newDrawing;
            }
            operation.progressCallback = progressCallback;
            plottedDrawing = operation.run(plottedDrawing);
            i++;
        }

        return plottedDrawing;
    }

    public static List<AbstractGeometryOperation> getGeometryExportOperations(ExportTask task, IGeometryFilter filter, boolean forceBypassOptimisation){
        List<AbstractGeometryOperation> geometryOperations = new ArrayList<>();
        geometryOperations.add(new GeometryOperationSimplify(filter, true, false));

        if(task.exportHandler.isVector && !forceBypassOptimisation && DBPreferences.INSTANCE.pathOptimisationEnabled.getValue()){
            geometryOperations.add(new GeometryOperationOptimize(CanvasUtils.createCanvasScaleTransform(task.plottedDrawing.getCanvas())));
            if(DBPreferences.INSTANCE.lineSortingEnabled.get()){
                geometryOperations.add(new GeometryOperationSortGeometries());
            }
        }
        if(DBPreferences.INSTANCE.allowMultiplePathMoves.get()) {
            geometryOperations.add(new GeometryOperationSimplify(filter, true, true));
        }
        return geometryOperations;
    }

    public static boolean compareRenderColour(ObservableDrawingPen pen, IGeometry geometry1, IGeometry geometry2){
        if(Objects.equals(geometry1.getPenIndex(), geometry2.getPenIndex())){
            int pathRGBA = geometry1.getSampledRGBA() != -1 ? pen.getARGB(geometry1.getSampledRGBA()) : pen.getARGB();
            int geoRGBA = geometry2.getSampledRGBA() != -1 ? pen.getARGB(geometry2.getSampledRGBA()) : pen.getARGB();
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
        return lastGeometry.getEndCoordinate().equals(nextGeometry.getOriginCoordinate());
    }

    public static boolean comparePathContinuityFlipped(IGeometry lastGeometry, IGeometry nextGeometry){
        if(lastGeometry == null || lastGeometry.getGroupID() != nextGeometry.getGroupID()){
            return false;
        }
        if(nextGeometry instanceof IPathElement){
            IPathElement element = (IPathElement) nextGeometry;
            if(lastGeometry instanceof IPathElement){
                return lastGeometry.getOriginCoordinate().equals(element.getEndCoordinate());
            }
            if(lastGeometry instanceof GPath){
                Coordinate coordinate = lastGeometry.getEndCoordinate();
                return ((float)coordinate.x) == element.getEndCoordinate().getX() && ((float)coordinate.y) == element.getEndCoordinate().getY();
            }
        }
        return false;
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


    public static List<LineString> toLineStrings(List<IGeometry> geometries, AffineTransform transform) {
        List<LineString> lineStrings = new ArrayList<>();
        for (IGeometry g : geometries) {
            GeometryUtils.toLineStrings(g, transform, lineStrings);
        }
        return lineStrings;
    }

    public static void toLineStrings(IGeometry geometry, AffineTransform transform, List<LineString> lineStrings){
        List<Coordinate[]> coordinates = ShapeReader.toCoordinates(new FlatteningPathIterator(geometry.getAWTShape().getPathIterator(transform), 6D));
        for (Coordinate[] coordinate : coordinates) {
            if(coordinate.length < 2){
                continue;
            }
            LineString lineString = factory.createLineString(coordinate);
            lineStrings.add(lineString);
        }
    }

    public static List<IGeometry> fromLineStrings(List<LineString> lineStrings, AffineTransform transform){
        List<IGeometry> geometries = new ArrayList<>();
        for(LineString g : lineStrings){
            geometries.add(GeometryUtils.fromLineStrings(g, transform));
        }
        return geometries;
    }

    public static IGeometry fromLineStrings(LineString string, AffineTransform transform){
        return geometryToGPath(string, transform);
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

    public static List<Geometry> getGeometriesFromCollection(GeometryCollection collection){
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < collection.getNumGeometries(); i ++) {
            Geometry geometry = collection.getGeometryN(i);
            geometries.add(geometry);
        }
        return geometries;
    }


    public static GPath geometryToGPath(Geometry string, AffineTransform transform){
        return new GPath(new ShapeWriter().toShape(string), transform);
    }

    public static IGeometry createMultiPassGeometry(IGeometry geometry, int multiPassCount){
        if(multiPassCount <= 1){
            return geometry.copyGeometry();
        }
        GPath finalPath = new GPath(geometry.getAWTShape());
        GeometryUtils.copyGeometryData(finalPath, geometry);

        GPath normalPath = (GPath) finalPath.copyGeometry();
        GPath reversedPath = GeometryUtils.reverseGPath(normalPath);

        for(int i = 1; i < multiPassCount; i ++){
            if((i % 2) == 0){ //if the pass is even
                normalPath.addToPath(false, finalPath);
            }else{ //if the pass is odd
                reversedPath.addToPath(false, finalPath);
            }
        }
        return finalPath;
    }

    public static GPath reverseGPath(GPath gPath){
        GPath reversedPath = new GPath();
        GeometryUtils.copyGeometryData(reversedPath, gPath);

        List<IGeometry> geometries = new ArrayList<>();
        splitGPath(gPath, geometries::add);
        Collections.reverse(geometries);

        IPathElement last = null;
        for(IGeometry geometry : geometries){
            IPathElement pathElement = (IPathElement) geometry;

            if(last == null || !last.getOriginCoordinate().equals(pathElement.getEndCoordinate())){
                reversedPath.moveTo(pathElement.getEndCoordinate().getX(), pathElement.getEndCoordinate().getY());
            }

            if(geometry instanceof GLine){
                GLine gLine = (GLine) geometry;
                reversedPath.lineTo(gLine.getX1(), gLine.getY1());
            }
            if(geometry instanceof GQuadCurve){
                GQuadCurve gQuad = (GQuadCurve) geometry;
                reversedPath.quadTo(gQuad.getCtrlX(), gQuad.getCtrlY(), gQuad.getX1(), gQuad.getY1());
            }
            if(geometry instanceof GCubicCurve){
                GCubicCurve gCubic = (GCubicCurve) geometry;
                reversedPath.curveTo(gCubic.getCtrlX2(), gCubic.getCtrlY2(), gCubic.getCtrlX1(), gCubic.getCtrlY1(), gCubic.getX1(), gCubic.getY1());
            }
            last = pathElement;
        }
        return reversedPath;
    }

    public static void splitGPath(GPath gPath, Consumer<IGeometry> consumer){
        PathIterator pathIterator = gPath.getAWTShape().getPathIterator(null);

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
                copyGeometryData(geometry, gPath);
                consumer.accept(geometry);
            }

            pathIterator.next();
        }
    }


    public static void splitGPathIntoSubPaths(GPath gPath, Consumer<IGeometry> consumer){
        PathIterator pathIterator = gPath.getAWTShape().getPathIterator(null);

        float lastMoveX = 0;
        float lastMoveY = 0;
        boolean hasMoveTo = false;

        float currentX = 0;
        float currentY = 0;

        PathBuilder pathBuilder = new PathBuilder(s -> {
            GeometryUtils.copyGeometryData(s, gPath);
            consumer.accept(s);
        });

        pathBuilder.startPath();
        float[] coords = new float[6];
        for(; !pathIterator.isDone(); pathIterator.next()){
            int type = pathIterator.currentSegment(coords);
            switch (type){
                case PathIterator.SEG_MOVETO:
                    if(!pathBuilder.isContinuation(coords[0], coords[1])){
                        pathBuilder.endPath();
                        pathBuilder.startPath();
                    }

                    if(!pathBuilder.hasMoveTo()){
                        pathBuilder.moveTo(coords[0], coords[1]);
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    pathBuilder.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    pathBuilder.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    pathBuilder.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    pathBuilder.closePath();
                    pathBuilder.endPath();
                    break;
            }
        }
        pathBuilder.endPath();
    }

    public static IGeometry copyGeometryData(IGeometry copy, IGeometry reference){
        copy.setGeometryIndex(reference.getGeometryIndex());
        copy.setPFMPenIndex(reference.getPFMPenIndex());
        copy.setPenIndex(reference.getPenIndex());
        copy.setSampledRGBA(reference.getSampledRGBA());
        copy.setGroupID(reference.getGroupID());
        copy.setFillType(reference.getFillType());
        return copy;
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

    public static int getSubPathCount(Shape shape){
        int count = 0;
        PathIterator iterator = shape.getPathIterator(null);
        double[] coords = new double[6];
        while(!iterator.isDone()){
            int segType = iterator.currentSegment(coords);
            if(segType == PathIterator.SEG_MOVETO){
                count++;
            }
            iterator.next();
        }
        return count;
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

    public static boolean hasSegments(Shape shape){
        PathIterator iterator = shape.getPathIterator(null);
        return !iterator.isDone();
    }

    public static boolean hasCurveSegments(Shape shape){
        PathIterator iterator = shape.getPathIterator(null);
        double[] coords = new double[6];
        while(!iterator.isDone()){
            int segType = iterator.currentSegment(coords);
            if(segType == PathIterator.SEG_QUADTO || segType == PathIterator.SEG_CUBICTO){
                return true;
            }
            iterator.next();
        }
        return false;
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

    public static javafx.scene.shape.Shape convertGeometryToJFXShape(IGeometry geometry){
        for(JFXGeometryConverter converter : MasterRegistry.INSTANCE.jfxGeometryConverters){
            if(converter.canConvert(geometry)){
                return converter.convert(geometry);
            }
        }
        return MasterRegistry.INSTANCE.getFallbackJFXGeometryConverter().convert(geometry);
    }

    public static boolean updateJFXShapeFromGeometry(javafx.scene.shape.Shape shape, IGeometry geometry){
        for(JFXGeometryConverter converter : MasterRegistry.INSTANCE.jfxGeometryConverters){
            if(converter.canUpdate(shape)){
                converter.update(shape, geometry);
                return true;
            }
        }
        JFXGeometryConverter fallback = MasterRegistry.INSTANCE.getFallbackJFXGeometryConverter();
        if(fallback.canUpdate(shape)){
            fallback.update(shape, geometry);
            return true;
        }
        return false;
    }

}
