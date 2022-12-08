package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageTools;
import drawingbot.render.RenderUtils;

import java.awt.*;

/**
 * Most exporters will use the an implementation of Graphics2D to handle rendering the drawing and can therefore use this universal exporter
 */
public class Graphics2DExporter {

    public static void drawBackground(DBTaskContext context, Graphics2D graphics, int width, int height){
        graphics.setColor(ImageTools.getAWTFromFXColor(context.project.getDrawingArea().canvasColor.getValue()));
        graphics.fillRect(0, 0, width, height);
    }

    public static void preDraw(ExportTask exportTask, Graphics2D graphics){
        graphics.translate(exportTask.exportDrawing.getCanvas().getScaledDrawingOffsetX(), exportTask.exportDrawing.getCanvas().getScaledDrawingOffsetY());
        graphics.scale(exportTask.exportDrawing.getCanvas().getCanvasScale(), exportTask.exportDrawing.getCanvas().getCanvasScale());
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