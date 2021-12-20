package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.ExportTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.ImageTools;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottingTask;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Most exporters will use the an implementation of Graphics2D to handle rendering the drawing and can therefore use this universal exporter
 */
public class Graphics2DExporter {

    public static void drawBackground(ExportTask exportTask, Graphics2D graphics, int width, int height){
        graphics.setColor(ImageTools.getAWTFromFXColor(DrawingBotV3.INSTANCE.canvasColor.getValue()));
        graphics.fillRect(0, 0, width, height);
    }

    public static void preDraw(ExportTask exportTask, Graphics2D graphics, int width, int height, PlottingTask plottingTask){
        graphics.translate(exportTask.exportResolution.getScaledOffsetX(), exportTask.exportResolution.getScaledOffsetY());
        graphics.scale(exportTask.exportResolution.finalPrintScaleX, exportTask.exportResolution.finalPrintScaleY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void drawGeometryWithDrawingSet(ExportTask exportTask, Graphics2D graphics, ObservableDrawingSet drawingSet, Map<Integer, List<IGeometry>> geometries){
        int[] renderOrder = drawingSet.calculateRenderOrder();
        for(int i = 0; i < renderOrder.length; i++){
            int penIndex = renderOrder[renderOrder.length-1-i];
            ObservableDrawingPen pen = drawingSet.getPen(penIndex);
            drawGeometryWithDrawingPen(exportTask, graphics, pen, geometries.get(penIndex));
        }
    }

    public static void drawGeometryWithDrawingPen(ExportTask exportTask, Graphics2D graphics, ObservableDrawingPen pen, List<IGeometry> geometries){
        for(IGeometry g : geometries){
            g.renderAWT(graphics, pen);
            exportTask.onGeometryRendered();
        }
    }

    public static void postDraw(ExportTask exportTask, Graphics2D graphics, int width, int height, PlottingTask plottingTask){
        graphics.dispose();
    }

}