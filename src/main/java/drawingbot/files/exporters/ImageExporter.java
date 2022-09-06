package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.image.ImageTools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageExporter {

    public static boolean useAlphaChannelOnRaster(ExportTask exportTask){
        return exportTask.extension.equals(".png"); //TODO CHANGE ME?
    }

    public static void exportImage(ExportTask exportTask, File saveLocation) {

        ImageRenderer renderer = new ImageRenderer(exportTask, false);

        Graphics2D graphics = renderer.getGraphics();
        Graphics2DExporter.preDraw(exportTask, graphics);
        Graphics2DExporter.drawGeometries(exportTask, graphics, IGeometryFilter.BYPASS_FILTER);
        Graphics2DExporter.postDraw(exportTask, graphics);
        renderer.dispose();

        try {
            if(ImageIO.write(renderer.createExportImage(), exportTask.extension.substring(1), saveLocation)){
                exportTask.updateProgress(1, 1);
            }else{
                exportTask.setError("Image Export Failed");
            }
        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void exportReferenceImage(ExportTask exportTask, File saveLocation) {
        BufferedImage referenceImage = exportTask.exportDrawing.getReferenceImage();
        if(referenceImage == null) {
            exportTask.updateMessage("Invalid Reference Image");
            exportTask.updateProgress(1, 1);
            return;
        }
        BufferedImage image = new BufferedImage((int)exportTask.exportDrawing.getCanvas().getScaledWidth(), (int)exportTask.exportDrawing.getCanvas().getScaledHeight(), ImageExporter.useAlphaChannelOnRaster(exportTask) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(ImageTools.getAWTFromFXColor(exportTask.context.project.getDrawingArea().canvasColor.get()));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.drawImage(referenceImage, null, (int)exportTask.exportDrawing.getCanvas().getScaledDrawingOffsetX(), (int)exportTask.exportDrawing.getCanvas().getScaledDrawingOffsetY());
        graphics.dispose();

        try {
            if(ImageIO.write(image, exportTask.extension.substring(1), saveLocation)){
                exportTask.updateProgress(1, 1);
            }else{
                exportTask.setError("Image Export Failed");
            }
        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
