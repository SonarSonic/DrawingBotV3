package drawingbot.files.exporters;

import drawingbot.api.IPointFilter;
import drawingbot.files.ExportTask;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottedPath;
import drawingbot.plotting.PlottingTask;

import java.awt.*;
import java.util.List;

/**
 * Most exporters will use the an implementation of Graphics2D to handle rendering the drawing and can therefore use this universal exporter
 */
public class Graphics2DExporter {

    public static void drawGraphics(Graphics2D graphics, int width, int height, ExportTask exportTask, PlottingTask plottingTask, IPointFilter lineFilter) {
        EnumBlendMode blendMode = plottingTask.plottedDrawing.drawingPenSet.blendMode.get();
        if(blendMode.additive){
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, width, height);
        }
        graphics.translate(plottingTask.resolution.getScaledOffsetX(), plottingTask.resolution.getScaledOffsetY());
        graphics.setComposite(new BlendComposite(blendMode));
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<PlottedPath> plottedPaths = plottingTask.plottedDrawing.generatePlottedPaths(lineFilter);
        for(int i = 0; i < plottedPaths.size(); i++){
            PlottedPath plottedPath = plottedPaths.get(i);
            graphics.setStroke(plottedPath.stroke);
            graphics.setColor(plottedPath.color);
            graphics.draw(plottedPath.path);
            exportTask.updateProgress(i, plottedPaths.size());
        }
        graphics.dispose();
    }

}