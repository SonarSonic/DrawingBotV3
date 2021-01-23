package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.IDrawingPen;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;
import processing.core.PGraphics;

import java.io.File;

import static processing.core.PApplet.println;
import static processing.core.PConstants.PDF;

public class PDFExporter {

    public static void exportPDF(PlottingTask task, File file) {
        PGraphics pdf = DrawingBotV3.INSTANCE.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, PDF, file.getAbsolutePath());
        pdf.beginDraw();
        pdf.background(255, 255, 255);
        for(int i = task.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
            PlottedLine line = task.plottedDrawing.plottedLines.get(i);
            if(line.pen_down) {
                int rgb = task.plottedDrawing.drawingPenSet.getPens().get(line.pen_number).getRGBColour();
                pdf.stroke(rgb, 255);
                pdf.line(line.x1, line.y1, line.x2, line.y2);
            }
        }
        pdf.dispose();
        pdf.endDraw();
        println("PDF created:  " + file.getName());
    }

    public static void exportPDFPerPen(PlottingTask task, File file) {
        File path = FileUtils.removeExtension(file);
        for (int p = 0; p < task.plottedDrawing.getPenCount(); p ++) {
            IDrawingPen pen = task.plottedDrawing.drawingPenSet.getPens().get(p);
            String pdfName = path.getPath() + "_pen" + p + "_" + pen.getName() + ".pdf";
            PGraphics pdf = DrawingBotV3.INSTANCE.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, PDF, pdfName);
            pdf.beginDraw();
            pdf.background(255, 255, 255);
            for (int i = task.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
                PlottedLine line = task.plottedDrawing.plottedLines.get(i);
                if (line.pen_down & line.pen_number == p) {
                    pdf.stroke(pen.getRGBColour(), 255);
                    pdf.line(line.x1, line.y1, line.x2, line.y2);
                }
            }
            pdf.dispose();
            pdf.endDraw();
            println("PDF created:  " + pdfName);
        }
    }
}
