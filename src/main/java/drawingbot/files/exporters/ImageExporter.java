package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.canvas.CanvasUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageExporter {

    public static boolean useAlphaChannelOnRaster(ExportTask exportTask){
        return exportTask.extension.equals(".png"); //TODO CHANGE ME?
    }

    public static BufferedImage createFreshBufferedImage(ExportTask exportTask, boolean isVideo){
        int rasterWidth = CanvasUtils.getRasterExportWidth(exportTask.exportDrawing.getCanvas(), ConfigFileHandler.getApplicationSettings().exportDPI, false);
        int rasterHeight = CanvasUtils.getRasterExportHeight(exportTask.exportDrawing.getCanvas(), ConfigFileHandler.getApplicationSettings().exportDPI, false);
        return createFreshBufferedImage(exportTask, rasterWidth, rasterHeight, isVideo);
    }

    public static BufferedImage createFreshBufferedImage(ExportTask exportTask, int canvasWidth, int canvasHeight, boolean isVideo){
        int width = CanvasUtils.getRasterExportWidth(canvasWidth, isVideo);
        int height = CanvasUtils.getRasterExportHeight(canvasHeight, isVideo);
        boolean useAlphaChannel = useAlphaChannelOnRaster(exportTask);
        return new BufferedImage(width, height, useAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }

    public static Graphics2D createFreshGraphics2D(ExportTask exportTask, BufferedImage image){
        Graphics2D graphics = image.createGraphics();
        if(!useAlphaChannelOnRaster(exportTask)){
            Graphics2DExporter.drawBackground(exportTask, graphics, image.getWidth(), image.getHeight());
        }
        setBlendMode(exportTask, graphics);
        return graphics;
    }

    public static void setBlendMode(ExportTask exportTask, Graphics2D graphics2D){
        EnumBlendMode blendMode = DrawingBotV3.INSTANCE.blendMode.get();
        if(blendMode != EnumBlendMode.NORMAL){
            graphics2D.setComposite(new BlendComposite(blendMode));
        }
    }

    public static void exportImage(ExportTask exportTask, File saveLocation) {
        BufferedImage image = createFreshBufferedImage(exportTask, false);
        Graphics2D graphics = createFreshGraphics2D(exportTask, image);

        Graphics2DExporter.preDraw(exportTask, graphics);
        Graphics2DExporter.drawGeometries(exportTask, graphics, IGeometryFilter.BYPASS_FILTER);
        Graphics2DExporter.postDraw(exportTask, graphics);

        try {
            ImageIO.write(image, exportTask.extension.substring(1), saveLocation);
        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
