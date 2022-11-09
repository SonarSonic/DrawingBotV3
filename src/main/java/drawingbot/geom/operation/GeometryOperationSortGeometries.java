package drawingbot.geom.operation;

import drawingbot.DrawingBotV3;
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
        DBPreferences settings = DrawingBotV3.INSTANCE.getPreferences();
        double tolerance = UnitsLength.convert(settings.lineSortingTolerance.get(), settings.lineSortingUnits.get(), UnitsLength.MILLIMETRES);

        for(PlottedGroup group : originalDrawing.groups.values()){
            for(Map.Entry<ObservableDrawingPen, List<IGeometry>> entry : group.getGeometriesPerPen().entrySet()){
                STRTreeSequencer.Geometry sequencer = new STRTreeSequencer.Geometry(entry.getValue(), tolerance);
                entry.setValue(sequencer.sort());
            }
        }
        return originalDrawing;
    }

    @Override
    public boolean isDestructive() {
        return true;
    }
}
