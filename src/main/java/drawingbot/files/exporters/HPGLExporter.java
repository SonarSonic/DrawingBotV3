package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


/// W.I.P
public class HPGLExporter {

    public static void exportGCode(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        PrintWriter output = FileUtils.createWriter(saveLocation);
        HPGLBuilder builder = new HPGLBuilder(plottingTask, output);

        builder.open();

        AffineTransform transform = plottingTask.createGCodeTransform();

        float[] coords = new float[6];
        for(List<IGeometry> geometryList : geometries.values()){
            builder.startLayer();
            int i = 0;
            for(IGeometry geometry : geometryList){
                PathIterator iterator = geometry.getAWTShape().getPathIterator(transform);
                while(!iterator.isDone()){
                    int type = iterator.currentSegment(coords);
                    builder.move(coords, type);
                    iterator.next();
                }
                i++;
                exportTask.updateProgress(i, geometryList.size()-1);
            }
            builder.endLayer();
        }
        builder.close();
        DrawingBotV3.logger.info("GCode File Created:  " +  saveLocation);
    }

}
