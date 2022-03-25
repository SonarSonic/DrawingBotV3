package drawingbot.geom;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.api.IProgressCallback;
import drawingbot.geom.operation.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.geom.shapes.*;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LinearComponentExtracter;

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
            public void updateProgress(float progress, float max) {
                task.updateProgress(progress, max);
            }
        };

        List<AbstractGeometryOperation> geometryOperations = getGeometryExportOperations(task, task.plottingTask.pfmFactory, filter, forceBypassOptimisation);

        PlottedDrawing plottedDrawing = task.plottingTask.plottedDrawing;

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

    public static List<AbstractGeometryOperation> getGeometryExportOperations(ExportTask task, @Nullable PFMFactory<?> factory, IGeometryFilter filter, boolean forceBypassOptimisation){
        List<AbstractGeometryOperation> geometryOperations = new ArrayList<>();
        geometryOperations.add(new GeometryOperationSimplify(filter, true, false));
        if(task.exportHandler.isVector && !forceBypassOptimisation && ConfigFileHandler.getApplicationSettings().pathOptimisationEnabled){

            if(factory != null && !factory.bypassOptimisation){
                geometryOperations.add(new GeometryOperationOptimize(task.plottingTask.createPrintTransform()));
            }else if(ConfigFileHandler.getApplicationSettings().lineSortingEnabled){
                geometryOperations.add(new GeometryOperationSortGeometries());
            }
            //geometryOperations.add(new GeometryOperationSortGroupOrder());
        }

        return geometryOperations;

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
        if(lastGeometry == null || lastGeometry.getGroupID() != nextGeometry.getGroupID()){
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
            LineString lineString = factory.createLineString(coordinate);
            lineStrings.add(lineString);
        }
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

    public static IGeometry copyGeometryData(IGeometry copy, IGeometry reference){
        copy.setGeometryIndex(reference.getGeometryIndex());
        copy.setPFMPenIndex(reference.getPFMPenIndex());
        copy.setPenIndex(reference.getPenIndex());
        copy.setSampledRGBA(reference.getSampledRGBA());
        copy.setGroupID(reference.getGroupID());
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

}
