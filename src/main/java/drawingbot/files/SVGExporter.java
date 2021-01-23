package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.IDrawingPen;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.BiFunction;

import static processing.core.PApplet.*;

public class SVGExporter {

    // Thanks to Vladimir Bochkov for helping me debug the SVG international decimal separators problem.
    public static String svg_format (Float n) {
        final char regional_decimal_separator = ',';
        final char svg_decimal_seperator = '.';

        String s = nf(n, 0, DrawingBotV3.svg_decimals);
        s = s.replace(regional_decimal_separator, svg_decimal_seperator);
        return s;
    }

    // Thanks to John Cliff for getting the SVG output moving forward.
    public static void exportSVG(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        boolean  drawing_polyline = false;
        float  svgdpi = 96.0F / 25.4F; // Inkscape versions before 0.91 used 90dpi, Today most software assumes 96dpi.

        plottingTask.output = DrawingBotV3.createWriter(saveLocation);
        plottingTask.output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        plottingTask.output.println("<svg width=\"" + svg_format(plottingTask.width() * plottingTask.gcode_scale) + "mm\" height=\"" + svg_format(plottingTask.getPlottingImage().height * plottingTask.gcode_scale) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
        plottingTask.plottedDrawing.setPenContinuationFlagsForSVG();

        int completedLines = 0;

        // Loop over pens backwards to display dark lines last.
        // Then loop over all displayed lines.
        for (int p = plottingTask.plottedDrawing.getPenCount()-1; p >= 0; p--) {
            ObservableDrawingPen pen = plottingTask.plottedDrawing.getPen(p);

            plottingTask.output.println("<g id=\"" + pen.getName() + "\">");
            for (int i = 0; i < plottingTask.plottedDrawing.getDisplayedLineCount(); i++) {
                PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
                if (line.pen_number == p) {

                    // TODO OFFSETS... Do we add gcode_offsets needed by my bot, or zero based?
                    //float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale * svgdpi + gcode_offset_x;
                    //float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale * svgdpi + gcode_offset_y;
                    //float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale * svgdpi + gcode_offset_x;
                    //float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale * svgdpi + gcode_offset_y;

                    float gcode_scaled_x1 = line.x1 * plottingTask.gcode_scale * svgdpi;
                    float gcode_scaled_y1 = line.y1 * plottingTask.gcode_scale * svgdpi;
                    float gcode_scaled_x2 = line.x2 * plottingTask.gcode_scale * svgdpi;
                    float gcode_scaled_y2 = line.y2 * plottingTask.gcode_scale * svgdpi;

                    if (!line.pen_continuation && drawing_polyline) {
                        plottingTask.output.println("\" />");
                        drawing_polyline = false;
                    }

                    if (lineFilter.apply(line, pen)) {
                        if (line.pen_continuation) {
                            String buf = svg_format(gcode_scaled_x2) + "," + svg_format(gcode_scaled_y2);
                            plottingTask.output.println(buf);
                            drawing_polyline = true;
                        } else {
                            plottingTask.output.println("<polyline fill=\"none\" stroke=\"#" + hex(pen.getRGBColour(), 6) + "\" stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
                            String buf = svg_format(gcode_scaled_x1) + "," + svg_format(gcode_scaled_y1);
                            plottingTask.output.println(buf);
                            drawing_polyline = true;
                        }
                    }
                    completedLines++;
                }
                exportTask.updateProgress(completedLines, plottingTask.plottedDrawing.getDisplayedLineCount());
            }
            if (drawing_polyline) {
                plottingTask.output.println("\" />");
                drawing_polyline = false;
            }
            plottingTask.output.println("</g>");
        }
        plottingTask.output.println("</svg>");
        plottingTask.output.flush();
        plottingTask.output.close();
        println("SVG created:  " + saveLocation.getName());
    }
}
