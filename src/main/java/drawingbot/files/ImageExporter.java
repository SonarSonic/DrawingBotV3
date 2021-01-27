package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import javafx.stage.FileChooser;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.io.File;
import java.util.function.BiFunction;

import static processing.core.PApplet.println;

public class ImageExporter {

    public static void exportImage(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        PGraphics graphics = DrawingBotV3.INSTANCE.createGraphics(plottingTask.img_plotting.width, plottingTask.img_plotting.height, PConstants.JAVA2D);
        graphics.beginDraw();
        if(!extension.equals(".png")){ //ALLOW PNG TRANSPARENCY
            if(plottingTask.plottedDrawing.drawingPenSet.blendMode.get().additive){
                graphics.background(0, 0, 0);
            }else{
                graphics.background(255, 255, 255);
            }
        }
        graphics.blendMode(plottingTask.plottedDrawing.drawingPenSet.blendMode.get().constant);
        for (int i = plottingTask.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
            PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
            ObservableDrawingPen pen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(line.pen_number);
            if (lineFilter.apply(line, pen)) {
                graphics.stroke(pen.getRGBColour());
                graphics.line(line.x1, line.y1, line.x2, line.y2);
            }
            exportTask.updateProgress(plottingTask.plottedDrawing.getDisplayedLineCount() - i, plottingTask.plottedDrawing.getDisplayedLineCount());
        }
        graphics.endDraw();
        graphics.save(saveLocation.getPath());
        println("Image created:  " + saveLocation.getPath());
    }
}
