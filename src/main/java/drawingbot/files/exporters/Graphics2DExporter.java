package drawingbot.files.exporters;

import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.ExportTask;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.IGeometry;
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

    public static void preDraw(Graphics2D graphics, int width, int height, ExportTask exportTask, PlottingTask plottingTask){
        EnumBlendMode blendMode = plottingTask.plottedDrawing.drawingPenSet.blendMode.get();
        if(blendMode.additive){
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, width, height);
        }
        graphics.setComposite(new BlendComposite(blendMode));
        graphics.translate(plottingTask.resolution.getScaledOffsetX(), plottingTask.resolution.getScaledOffsetY());
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void drawGeometryWithDrawingSet(Graphics2D graphics, ObservableDrawingSet drawingSet, Map<Integer, List<IGeometry>> geometries){
        int totalGeometries = GeometryUtils.getTotalGeometries(geometries);
        int[] renderOrder = drawingSet.getCurrentRenderOrder();
        for(int i = 0; i < renderOrder.length; i++){
            int penIndex = renderOrder[renderOrder.length-1-i];
            ObservableDrawingPen pen = drawingSet.getPen(penIndex);
            drawGeometryWithDrawingPen(graphics, pen, geometries.get(penIndex));
        }
    }

    public static void drawGeometryWithDrawingPen(Graphics2D graphics, ObservableDrawingPen pen, List<IGeometry> geometries){
        int renderCount = 0;
        for(IGeometry g : geometries){
            g.renderAWT(graphics, pen);
            renderCount++;
            //exportTask.updateProgress(renderCount, totalGeometries);
        }
    }

    public static void postDraw(Graphics2D graphics, int width, int height, ExportTask exportTask, PlottingTask plottingTask){
        graphics.dispose();
    }

}