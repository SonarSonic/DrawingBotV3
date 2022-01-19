package drawingbot.files;

import drawingbot.files.ExportTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IExportMethod {

    void export(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation);

}
