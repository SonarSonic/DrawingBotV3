package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;

import java.io.File;
import java.io.PrintWriter;
import java.util.function.BiFunction;

import static processing.core.PApplet.*;

//TODO CHECK SVG COLOUR/OPACITY ACCURACY - ENSURE DPI is relative to actual size not inches.
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

        PrintWriter output = DrawingBotV3.createWriter(saveLocation);
        output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        output.println("<svg width=\"" + svg_format(plottingTask.width() * plottingTask.getGCodeScale()) + "mm\" height=\"" + svg_format(plottingTask.getPlottingImage().height * plottingTask.getGCodeScale()) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
        plottingTask.plottedDrawing.setPenContinuationFlagsForSVG();

        int completedLines = 0;

        // Loop over pens backwards to display dark lines last.
        // Then loop over all displayed lines.
        for (int p = plottingTask.plottedDrawing.getPenCount()-1; p >= 0; p--) {
            ObservableDrawingPen pen = plottingTask.plottedDrawing.getPen(p);

            output.println("<g id=\"" + pen.getName() + "\">");
            for (int i = 0; i < plottingTask.plottedDrawing.getDisplayedLineCount(); i++) {
                PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
                if (line.pen_number == p) {

                    float gcode_scaled_x1 = line.x1 * plottingTask.getGCodeScale() * svgdpi;
                    float gcode_scaled_y1 = line.y1 * plottingTask.getGCodeScale() * svgdpi;
                    float gcode_scaled_x2 = line.x2 * plottingTask.getGCodeScale() * svgdpi;
                    float gcode_scaled_y2 = line.y2 * plottingTask.getGCodeScale() * svgdpi;

                    if (!line.pen_continuation && drawing_polyline) {
                        output.println("\" />");
                        drawing_polyline = false;
                    }

                    if (lineFilter.apply(line, pen)) {
                        if (line.pen_continuation) {
                            String buf = svg_format(gcode_scaled_x2) + "," + svg_format(gcode_scaled_y2);
                            output.println(buf);
                        } else {
                            output.println("<polyline fill=\"none\" stroke=\"#" + hex(pen.getRGBColour(), 6) + "\" stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
                            String buf = svg_format(gcode_scaled_x1) + "," + svg_format(gcode_scaled_y1);
                            output.println(buf);
                        }
                        drawing_polyline = true;
                    }
                    completedLines++;
                }
                exportTask.updateProgress(completedLines, plottingTask.plottedDrawing.getDisplayedLineCount());
            }
            if (drawing_polyline) {
                output.println("\" />");
                drawing_polyline = false;
            }
            output.println("</g>");
        }
        output.println("</svg>");
        output.flush();
        output.close();
        DrawingBotV3.println("SVG created:  " + saveLocation.getName());
    }
}
