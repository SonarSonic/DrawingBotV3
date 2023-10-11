package drawingbot.geom.operation;

import drawingbot.api.ICustomPen;
import drawingbot.api.IProgressCallback;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.spatial.STRTreeSequencerLineString;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.linearref.LinearGeometryBuilder;
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
                if(!(entry.getKey().source instanceof ICustomPen) || ((ICustomPen)entry.getKey().source).canOptimisePenPaths()){

                    FlagStates pfmFlags = group.pfmFactory == null ? Flags.DEFAULT_PFM_STATE : group.pfmFactory.getFlags();
                    if(group.pfmFactory != null && (!pfmFlags.getFlag(Flags.PFM_LINE_OPTIMISING) || !pfmFlags.anyMatch(Flags.PFM_LINE_SIMPLIFY, Flags.PFM_LINE_MERGING, Flags.PFM_LINE_FILTERING, Flags.PFM_LINE_SORTING))){
                        entry.getValue().forEach(geometry -> {
                            IGeometry newGeometry;
                            if(DBPreferences.INSTANCE.multipassEnabled.get() && pfmFlags.getFlag(Flags.PFM_GEOMETRY_MULTIPASS)){
                                newGeometry = GeometryUtils.createMultiPassGeometry(geometry, DBPreferences.INSTANCE.multipassCount.get());
                            }else{
                                newGeometry = geometry.copyGeometry();
                            }
                            newDrawing.addGeometry(newGeometry, newGroup);
                        });
                    }else{
                        List<IGeometry> geometries = optimiseBasicGeometry(entry.getValue(), toJTS, fromJTS, progressCallback, pfmFlags);
                        for(IGeometry geometry : geometries){
                            geometry.setPenIndex(entry.getKey().penNumber.get());
                            geometry.setGroupID(newGroup.getGroupID());

                            //group id and geometry index will be set by the addGeometry so don't need to be set manually
                            newDrawing.addGeometry(geometry, newGroup);
                        }
                    }
                }else{
                    entry.getValue().forEach(g -> newDrawing.addGeometry(g.copyGeometry(), newGroup));
                }
            }
        }

        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }

    public static List<IGeometry> optimiseBasicGeometry(List<IGeometry> geometries, AffineTransform toJTS, AffineTransform fromJTS, IProgressCallback progressCallback, FlagStates pfmFlags) {
        if(geometries.isEmpty()){
            return new ArrayList<>();
        }
        List<LineString> lineStrings = GeometryUtils.toLineStrings(geometries, toJTS);

        lineStrings = optimiseJTSGeometry(lineStrings, progressCallback, pfmFlags);

        return GeometryUtils.fromLineStrings(lineStrings, fromJTS);
    }


    /**
     * Performs the configured optimisation on the set of line strings
     */
    public static List<LineString> optimiseJTSGeometry(List<LineString> lineStrings, IProgressCallback progressCallback, FlagStates pfmFlags){
        if(lineStrings.isEmpty()){
            return new ArrayList<>();
        }

        GeometryUtils.printEstimatedTravelDistance(lineStrings);
        DBPreferences settings = DBPreferences.INSTANCE;

        if(settings.lineSimplifyEnabled.get() && pfmFlags.getFlag(Flags.PFM_LINE_SIMPLIFY)){
            progressCallback.updateTitle("Line Simplifying: ");
            double tolerance = UnitsLength.convert(settings.lineSimplifyTolerance.get(), settings.lineSimplifyUnits.get(), UnitsLength.MILLIMETRES);
            lineStrings = lineSimplify(lineStrings, tolerance, progressCallback);
        }

        if(settings.lineMergingEnabled.get() && pfmFlags.getFlag(Flags.PFM_LINE_MERGING)){
            progressCallback.updateTitle("Line Merging: ");
            double tolerance = UnitsLength.convert(settings.lineMergingTolerance.get(), settings.lineMergingUnits.get(), UnitsLength.MILLIMETRES);
            lineStrings = lineMerge(lineStrings, tolerance, progressCallback, 3);
        }

        if(settings.lineFilteringEnabled.get() && pfmFlags.getFlag(Flags.PFM_LINE_FILTERING)){
            progressCallback.updateTitle("Line Filtering: ");
            double tolerance = UnitsLength.convert(settings.lineFilteringTolerance.get(), settings.lineFilteringUnits.get(), UnitsLength.MILLIMETRES);
            lineStrings = lineFilter(lineStrings, tolerance, progressCallback);
        }

        if(settings.multipassEnabled.get() && pfmFlags.getFlag(Flags.PFM_GEOMETRY_MULTIPASS) && settings.multipassCount.get() > 1){
            LinearGeometryBuilder builder = new LinearGeometryBuilder(GeometryUtils.factory);
            for(LineString oldString : lineStrings){

                for(int i = 0; i < settings.multipassCount.get(); i ++){
                    Coordinate[] coordinates = oldString.getCoordinates();
                    if((i % 2) == 0){ //if the pass is even
                        for(int c = 0; c < coordinates.length; c++){
                            builder.add(coordinates[c]);
                        }
                    }else{ //if the pass is odd
                        for(int c = coordinates.length-1; c >= 0; c--){
                            builder.add(coordinates[c]);
                        }
                    }
                }
                builder.endLine();
            }
            lineStrings = LinearComponentExtracter.getLines(builder.getGeometry(), true);
        }

        if(settings.lineSortingEnabled.get() && pfmFlags.getFlag(Flags.PFM_LINE_SORTING)){
            progressCallback.updateTitle("Line Sorting: ");
            double tolerance = UnitsLength.convert(settings.lineSortingTolerance.getValue(), settings.lineSortingUnits.get(), UnitsLength.MILLIMETRES);
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
            if(prevLength <= lineStrings.size()){
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
