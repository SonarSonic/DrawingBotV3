package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.ImageTools;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.render.RenderUtils;

import java.awt.*;

/**
 * Most exporters will use the an implementation of Graphics2D to handle rendering the drawing and can therefore use this universal exporter
 */
public class Graphics2DExporter {

    public static void drawBackground(ExportTask exportTask, Graphics2D graphics, int width, int height){
        graphics.setColor(ImageTools.getAWTFromFXColor(DrawingBotV3.INSTANCE.canvasColor.getValue()));
        graphics.fillRect(0, 0, width, height);
    }

    public static void preDraw(ExportTask exportTask, Graphics2D graphics){
        graphics.translate(exportTask.exportResolution.getScaledOffsetX(), exportTask.exportResolution.getScaledOffsetY());
        graphics.scale(exportTask.exportResolution.finalPrintScaleX, exportTask.exportResolution.finalPrintScaleY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void drawGeometries(ExportTask exportTask, Graphics2D graphics, IGeometryFilter geometryFilter){
        exportTask.exportIterator.reset();
        RenderUtils.renderDrawing(graphics, exportTask.exportIterator, geometryFilter, 0, (renderer, geometry, drawing, group, pen) -> {
            RenderUtils.renderGeometryAWT(graphics, geometry, drawing, group, pen);
            exportTask.onGeometryExported();
        });
    }

    public static void postDraw(ExportTask exportTask, Graphics2D graphics){
        graphics.dispose();
    }

}