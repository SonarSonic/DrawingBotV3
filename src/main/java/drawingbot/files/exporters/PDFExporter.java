package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;
import processing.core.PGraphics;

import java.io.File;
import java.util.function.BiFunction;

import static processing.core.PApplet.println;
import static processing.core.PConstants.PDF;

public class PDFExporter {

    public static void exportPDF(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        PGraphics pdf = DrawingBotV3.INSTANCE.createGraphics(plottingTask.getPlottingImage().width, plottingTask.getPlottingImage().height, PDF, saveLocation.getAbsolutePath());
        pdf.beginDraw();
        pdf.background(255, 255, 255);
        for(int i = plottingTask.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
            PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
            ObservableDrawingPen pen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(line.pen_number);
            if(lineFilter.apply(line, pen)) {
                int rgb = pen.getRGBColour();
                pdf.stroke(rgb, 255);
                pdf.line(line.x1, line.y1, line.x2, line.y2);
            }
            exportTask.updateProgress(plottingTask.plottedDrawing.getDisplayedLineCount() - i, plottingTask.plottedDrawing.getDisplayedLineCount());
        }
        pdf.dispose();
        pdf.endDraw();
        println("PDF created:  " + saveLocation.getName());
    }
}
