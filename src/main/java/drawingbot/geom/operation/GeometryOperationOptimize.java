package drawingbot.geom.operation;

import drawingbot.api.ICustomPen;
import drawingbot.api.IProgressCallback;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.json.presets.ConfigApplicationSettings;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.spatial.STRTreeSequencerLineString;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.utils.UnitsLength;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optimises the geometries, including simplifying, merging, filtering and sorting.
 * Uses the JTS library operations.
 */
public class GeometryOperationOptimize extends AbstractGeometryOperation{

    public AffineTransform printTransform;

    public GeometryOperationOptimize(AffineTransform printTransform){
        this.printTransform = printTransform;
    }

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing newDrawing = createPlottedDrawing(originalDrawing);

        AffineTransform toJTS = AffineTransform.getScaleInstance(printTransform.getScaleX(), printTransform.getScaleY());
        AffineTransform fromJTS = AffineTransform.getScaleInstance(1/printTransform.getScaleX(), 1/printTransform.getScaleY());

        for(PlottedGroup group : originalDrawing.groups.values()){
            PlottedGroup originalGroup = originalDrawing.getPlottedGroup(group.getGroupID());
            PlottedGroup newGroup = newDrawing.getMatchingPlottedGroup(originalGroup, forExport);


            for(Map.Entry<ObservableDrawingPen, List<IGeometry>> entry : group.getGeometriesPerPen().entrySet()){
                if(!(entry.getKey().source instanceof ICustomPen)){

                    if(group.pfmFactory != null && group.pfmFactory.shouldBypassOptimisation()){
                        originalGroup.geometries.forEach(geometry -> newGroup.addGeometry(geometry.copyGeometry()));
                    }else{
                        List<IGeometry> geometries = optimiseBasicGeometry(entry.getValue(), toJTS, fromJTS, progressCallback);
                        for(IGeometry geometry : geometries){
                            geometry.setPenIndex(entry.getKey().penNumber.get());
                            geometry.setGroupID(newGroup.getGroupID());

                            //group id and geometry index will be set by the addGeometry so don't need to be set manually
                            newDrawing.addGeometry(geometry, newGroup);
                        }
                    }
                }else{
                    originalGroup.geometries.forEach(g -> newDrawing.addGeometry(g.copyGeometry(), newGroup));
                }
            }
        }

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }

    public static List<IGeometry> optimiseBasicGeometry(List<IGeometry> geometries, AffineTransform toJTS, AffineTransform fromJTS, IProgressCallback progressCallback) {
        if(geometries.isEmpty()){
            return new ArrayList<>();
        }
        List<LineString> lineStrings = new ArrayList<>();
        for(IGeometry g : geometries){
            GeometryUtils.toLineStrings(g, toJTS, lineStrings);
        }

        lineStrings = optimiseJTSGeometry(lineStrings, progressCallback);

        List<IGeometry> optimised = new ArrayList<>();
        for(LineString g : lineStrings){
            optimised.add(GeometryUtils.fromLineStrings(g, fromJTS));
        }
        return optimised;
    }


    /**
     * Performs the configured optimisation on the set of line strings
     */
    public static List<LineString> optimiseJTSGeometry(List<LineString> lineStrings, IProgressCallback progressCallback){
        if(lineStrings.isEmpty()){
            return new ArrayList<>();
        }

        GeometryUtils.printEstimatedTravelDistance(lineStrings);
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
        GeometryUtils.printEstimatedTravelDistance(lineStrings);
        return lineStrings;
    }



    /**
     * Filters out line strings with a length below the given minimum length
     */
    public static List<LineString> lineFilter(List<LineString> lineStrings, double minLength, IProgressCallback progressCallback){
        List<LineString> filtered = new ArrayList<>();
        int index = 0;
        for(LineString lineString : lineStrings){
            if(lineString.getLength() >= minLength){
                filtered.add(lineString);
            }
            index++;
            progressCallback.updateProgress(index, lineStrings.size());
        }
        return filtered;
    }

    /**
     * Simplifies lines using a given tolerance.
     */
    public static List<LineString> lineSimplify(List<LineString> lineStrings, double tolerance, IProgressCallback progressCallback){
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
            progressCallback.updateProgress(index, lineStrings.size());
        }
        return simplified;
    }

    /**
     * Merges lines at their start/end point within the given tolerance.
     */
    public static List<LineString> lineMerge(List<LineString> lineStrings, double tolerance, IProgressCallback progressCallback, int pass){
        for(int i = 0; i < pass; i++){
            int prevLength = lineStrings.size();
            progressCallback.updateTitle("Line Merging " + (i+1) + " of " + pass + ": ");
            STRTreeSequencerLineString sequencer = new STRTreeSequencerLineString(lineStrings, tolerance);
            sequencer.setProgressCallback(progressCallback);
            lineStrings = sequencer.merge();
            if(prevLength == lineStrings.size()){
                break;
            }
        }
        return lineStrings;
    }

    /**
     * Orders lines to minimise air time, by finding the nearest line to the current point.
     * This is a simple version and doesn't provide a perfect solution
     */
    public static List<LineString> lineSort(List<LineString> lineStrings, double allowableDistance, IProgressCallback progressCallback){
        STRTreeSequencerLineString sequencer = new STRTreeSequencerLineString(lineStrings, allowableDistance);
        sequencer.setProgressCallback(progressCallback);
        return sequencer.sort();
    }

}
