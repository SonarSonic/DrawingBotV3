package drawingbot.files.exporters;

import drawingbot.files.ExportTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ImageExporter {

    public static void exportImage(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        int width = (int)plottingTask.resolution.getScaledWidth();
        int height = (int)plottingTask.resolution.getScaledHeight();

        boolean useAlpha = !extension.equals(".jpg");
        BufferedImage image = new BufferedImage(width, height, useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        if(!useAlpha){
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
        }

        Graphics2DExporter.preDraw(exportTask, graphics, width, height, plottingTask);
        Graphics2DExporter.drawGeometryWithDrawingSet(exportTask, graphics, plottingTask.getDrawingSet(), geometries);
        Graphics2DExporter.postDraw(exportTask, graphics, width, height, plottingTask);

        try {
            ImageIO.write(image, extension.substring(1), saveLocation);
        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
