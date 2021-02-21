package drawingbot.files.exporters;

import drawingbot.api.IPointFilter;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottingTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageExporter {

    public static void exportImage(ExportTask exportTask, PlottingTask plottingTask, IPointFilter lineFilter, String extension, File saveLocation) {

        int width = plottingTask.getPixelWidth();
        int height = plottingTask.getPixelHeight();

        boolean useAlpha = !extension.equals(".jpg");
        BufferedImage image = new BufferedImage(width, height, useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        if(!useAlpha){
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
        }
        Graphics2DExporter.drawGraphics(graphics, width, height, exportTask, plottingTask, lineFilter);
        try {
            ImageIO.write(image, extension.substring(1), saveLocation);
        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
