package drawingbot.geom.operation;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.spatial.STRTreeSequencer;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.utils.UnitsLength;

import java.util.List;
import java.util.Map;

/**
 * This will change the order of geometries per pen per group
 */
public class GeometryOperationSortGeometries extends AbstractGeometryOperation{

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing newDrawing = createPlottedDrawing(originalDrawing);

        DBPreferences settings = DBPreferences.INSTANCE;
        double tolerance = UnitsLength.convert(settings.lineSortingTolerance.get(), settings.lineSortingUnits.get(), UnitsLength.MILLIMETRES);


        for(PlottedGroup group : originalDrawing.groups.values()) {
            PlottedGroup originalGroup = originalDrawing.getPlottedGroup(group.getGroupID());
            PlottedGroup newGroup = newDrawing.getMatchingPlottedGroup(originalGroup, forExport);

            for(Map.Entry<ObservableDrawingPen, List<IGeometry>> entry : group.getGeometriesPerPen().entrySet()){
                STRTreeSequencer.Geometry sequencer = new STRTreeSequencer.Geometry(entry.getValue(), tolerance);
                sequencer.sort().forEach(g -> newDrawing.addGeometry(g, newGroup));
            }
        }
        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }
}
