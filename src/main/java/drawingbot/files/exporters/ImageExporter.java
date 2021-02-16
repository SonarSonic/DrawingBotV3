package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;

public class ImageExporter {

    public static void exportImage(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        BufferedImage image = new BufferedImage(plottingTask.img_plotting.getWidth(), plottingTask.img_plotting.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2DExporter.drawGraphics(image.createGraphics(), exportTask, plottingTask, lineFilter);
        try {
            ImageIO.write(image, extension.substring(1), saveLocation);
        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
