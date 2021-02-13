package drawingbot.files.exporters;

import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;

import java.awt.*;
import java.util.function.BiFunction;

public class Graphics2DExporter {

    public static void exportImage(Graphics2D graphics, ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter) {
        for (int i = plottingTask.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
            PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
            ObservableDrawingPen pen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(line.pen_number);
            if (lineFilter.apply(line, pen)) {
                graphics.setColor(new Color(pen.getARGB(), true));
                graphics.drawLine((int)line.x1, (int)line.y1, (int)line.x2, (int)line.y2);
            }
            if (i % (plottingTask.plottedDrawing.getDisplayedLineCount() / 100) == 0) { //only update for every percent lines
                exportTask.updateProgress(plottingTask.plottedDrawing.getDisplayedLineCount() - i, plottingTask.plottedDrawing.getDisplayedLineCount());
            }
        }
        graphics.dispose();
    }

}