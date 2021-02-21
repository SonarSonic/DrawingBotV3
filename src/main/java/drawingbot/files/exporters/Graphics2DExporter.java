package drawingbot.files.exporters;

import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;

import java.awt.*;
import java.util.function.BiFunction;

/**
 * Most exporters will use the an implementation of Graphics2D to handle rendering the drawing and can therefore use this universal exporter
 */
public class Graphics2DExporter {

    ///TODO CHECK THE CORRECT SCALE IS PASSED? DOES THIS MATTER?
    public static void drawGraphics(Graphics2D graphics, int width, int height, ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter) {
        EnumBlendMode blendMode = plottingTask.plottedDrawing.drawingPenSet.blendMode.get();
        if(blendMode.additive){
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, width, height);
        }
        graphics.translate(plottingTask.renderOffsetX, plottingTask.renderOffsetY);
        graphics.setComposite(new BlendComposite(blendMode));
        for (int i = plottingTask.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
            PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
            ObservableDrawingPen pen = plottingTask.plottedDrawing.getPen(line.pen_number);
            if (pen != null && lineFilter.apply(line, pen)) {
                plottingTask.plottedDrawing.renderLineAWT(graphics, pen, line);
            }
            if (i % (plottingTask.plottedDrawing.getDisplayedLineCount() / 100) == 0) { //only update for every percent lines
                exportTask.updateProgress(plottingTask.plottedDrawing.getDisplayedLineCount() - i, plottingTask.plottedDrawing.getDisplayedLineCount());
            }
        }
        graphics.dispose();
    }

}