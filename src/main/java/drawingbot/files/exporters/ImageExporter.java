package drawingbot.files.exporters;

import drawingbot.files.ExportTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottingTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ImageExporter {

    public static boolean useAlphaChannelOnRaster(ExportTask exportTask){
        return exportTask.extension.equals(".png");
    }

    public static int getRasterWidth(ExportTask exportTask){
        int width= (int) exportTask.exportResolution.getScaledWidth();
        if(width % 2 == 1){
            width-=1;
        }
        return width;
    }

    public static int getRasterHeight(ExportTask exportTask){
        int height = (int)exportTask.exportResolution.getScaledHeight();

        if(height % 2 == 1){
            height-=1;
        }

        return height;
    }

    public static BufferedImage createFreshBufferedImage(ExportTask exportTask){
        int width = getRasterWidth(exportTask);
        int height = getRasterHeight(exportTask);
        boolean useAlphaChannel = useAlphaChannelOnRaster(exportTask);
        return new BufferedImage(width, height, useAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }

    public static Graphics2D createFreshGraphics2D(ExportTask exportTask, BufferedImage image){
        int width = getRasterWidth(exportTask);
        int height = getRasterHeight(exportTask);
        Graphics2D graphics = image.createGraphics();
        EnumBlendMode blendMode = exportTask.plottingTask.plottedDrawing.drawingPenSet.blendMode.get();
        if(!useAlphaChannelOnRaster(exportTask) || blendMode != EnumBlendMode.NORMAL){
            if(blendMode.additive){
                graphics.setColor(Color.BLACK);
                graphics.drawRect(0, 0, width, height);
            }else{
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, width, height);
            }
        }
        setBlendMode(exportTask, graphics);
        return graphics;
    }

    public static void setBlendMode(ExportTask exportTask, Graphics2D graphics2D){
        EnumBlendMode blendMode = exportTask.plottingTask.plottedDrawing.drawingPenSet.blendMode.get();
        if(blendMode != EnumBlendMode.NORMAL){
            graphics2D.setComposite(new BlendComposite(blendMode));
        }
    }

    public static void exportImage(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        int width = getRasterWidth(exportTask);
        int height = getRasterHeight(exportTask);

        BufferedImage image = createFreshBufferedImage(exportTask);
        Graphics2D graphics = createFreshGraphics2D(exportTask, image);

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
