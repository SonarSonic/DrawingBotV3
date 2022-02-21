package drawingbot.geom.operation;

import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.types.ConfigApplicationSettings;
import drawingbot.geom.basic.IGeometry;
import drawingbot.geom.spatial.STRTreeSequencer;
import drawingbot.javafx.observables.ObservableDrawingPen;
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
        ConfigApplicationSettings settings = ConfigFileHandler.getApplicationSettings();
        float tolerance = UnitsLength.convert(settings.lineSortingTolerance, settings.lineSortingUnits, UnitsLength.MILLIMETRES);

        for(PlottedGroup group : originalDrawing.groups.values()){
            for(Map.Entry<ObservableDrawingPen, List<IGeometry>> entry : group.getGeometriesPerPen().entrySet()){
                STRTreeSequencer.IGeometry sequencer = new STRTreeSequencer.IGeometry(entry.getValue(), tolerance);
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
