package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.IDrawingPen;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;

import java.io.File;

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


    private static boolean drawing_polyline;
    private static float svgdpi;

    // Thanks to John Cliff for getting the SVG output moving forward.
    public static void exportSVG(PlottingTask task, File file) {
        drawing_polyline = false;
        svgdpi = 96.0F / 25.4F; // Inkscape versions before 0.91 used 90dpi, Today most software assumes 96dpi.

        task.output = DrawingBotV3.createWriter(file);
        task.output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        task.output.println("<svg width=\"" + svg_format(task.width() * task.gcode_scale) + "mm\" height=\"" + svg_format(task.getPlottingImage().height * task.gcode_scale) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
        task.plottedDrawing.setPenContinuationFlagsForSVG();

        // Loop over pens backwards to display dark lines last.
        // Then loop over all displayed lines.
        for (int p = task.plottedDrawing.getPenCount()-1; p >= 0; p--) {
            IDrawingPen pen = task.plottedDrawing.getPen(p);
            exportPenToSVG(task, pen, p);
        }
        task.output.println("</svg>");
        task.output.flush();
        task.output.close();
        println("SVG created:  " + file.getName());
    }

    public static void exportSVGPerPen(PlottingTask task, File file) {
        File path = FileUtils.removeExtension(file);

        drawing_polyline = false;
        svgdpi = 96.0F / 25.4F; // Inkscape versions before 0.91 used 90dpi, Today most software assumes 96dpi.

        for (int p = task.plottedDrawing.getPenCount()-1; p >= 0; p--) {
            IDrawingPen pen = task.plottedDrawing.getPen(p);
            String svgName = path + "_pen" + p + "_" + pen.getName() + ".svg";
            task.output = DrawingBotV3.INSTANCE.createWriter(svgName);
            task.output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            task.output.println("<svg width=\"" + svg_format(task.width() * task.gcode_scale) + "mm\" height=\"" + svg_format(task.getPlottingImage().height * task.gcode_scale) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
            task.plottedDrawing.setPenContinuationFlagsForSVG();
            exportPenToSVG(task, pen, p);
            task.output.println("</svg>");
            task.output.flush();
            task.output.close();
            println("SVG created:  " + svgName);
        }
    }


    public static void exportPenToSVG(PlottingTask task, IDrawingPen pen, int penNumber){
        task.output.println("<g id=\"" + pen.getName() + "\">");
        for (int i = 0; i < task.plottedDrawing.getDisplayedLineCount(); i++) {
            PlottedLine line = task.plottedDrawing.plottedLines.get(i);
            if (line.pen_number == penNumber) {

                // TODO OFFSETS... Do we add gcode_offsets needed by my bot, or zero based?
                //float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale * svgdpi + gcode_offset_x;
                //float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale * svgdpi + gcode_offset_y;
                //float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale * svgdpi + gcode_offset_x;
                //float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale * svgdpi + gcode_offset_y;

                float gcode_scaled_x1 = line.x1 * task.gcode_scale * svgdpi;
                float gcode_scaled_y1 = line.y1 * task.gcode_scale * svgdpi;
                float gcode_scaled_x2 = line.x2 * task.gcode_scale * svgdpi;
                float gcode_scaled_y2 = line.y2 * task.gcode_scale * svgdpi;

                if (!line.pen_continuation && drawing_polyline) {
                    task.output.println("\" />");
                    drawing_polyline = false;
                }

                if (line.pen_down) {
                    if (line.pen_continuation) {
                        String buf = svg_format(gcode_scaled_x2) + "," + svg_format(gcode_scaled_y2);
                        task.output.println(buf);
                        drawing_polyline = true;
                    } else {
                        task.output.println("<polyline fill=\"none\" stroke=\"#" + hex(pen.getRGBColour(), 6) + "\" stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
                        String buf = svg_format(gcode_scaled_x1) + "," + svg_format(gcode_scaled_y1);
                        task.output.println(buf);
                        drawing_polyline = true;
                    }
                }
            }
        }
        if (drawing_polyline) {
            task.output.println("\" />");
            drawing_polyline = false;
        }
        task.output.println("</g>");
    }
}
