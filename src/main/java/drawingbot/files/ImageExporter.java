package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.javafx.PGraphicsFX2D;

import java.io.File;

import static processing.core.PApplet.println;

public class ImageExporter {

    public static void saveImage(PlottingTask task, File file) {
        try{

            PGraphics graphics = DrawingBotV3.INSTANCE.createGraphics(task.img_plotting.width, task.img_plotting.height, PConstants.JAVA2D);

            graphics.beginDraw();
            for (int i = task.plottedDrawing.getDisplayedLineCount()-1; i >= 0; i--) {
                PlottedLine line = task.plottedDrawing.plottedLines.get(i);
                if (line.pen_down) {
                    ObservableDrawingPen pen = task.plottedDrawing.drawingPenSet.getPens().get(line.pen_number);
                    if(pen.isEnabled()){
                        graphics.stroke(pen.getRGBColour());
                        //graphics.blendMode(PConstants.MULTIPLY);
                        graphics.line(line.x1, line.y1, line.x2, line.y2);
                    }
                }
            }
            graphics.endDraw();
            graphics.save(file.getPath());
            println("Image created:  " + file.getPath());
        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
