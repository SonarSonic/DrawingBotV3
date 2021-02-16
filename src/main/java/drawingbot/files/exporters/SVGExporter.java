package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.*;
import java.util.function.BiFunction;

//Documentation/Source: https://xmlgraphics.apache.org/batik/using/svg-generator.html
public class SVGExporter {

    public static void exportSVG(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        try {
            int width = plottingTask.getPlottingImage().getWidth();
            int height = plottingTask.getPlottingImage().getHeight();

            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);

            // Create an instance of the SVG Generator.
            SVGGraphics2D graphics = new SVGGraphics2D(document);
            graphics.setSVGCanvasSize(new Dimension(width, height));

            // Ask the test to render into the SVG Graphics2D implementation.
            Graphics2DExporter.drawGraphics(graphics, exportTask, plottingTask, lineFilter);

            // Finally, stream out SVG to the standard output using UTF-8 encoding.
            exportTask.updateMessage("Encoding SVG");
            exportTask.updateProgress(-1, 1);

            boolean useCSS = true; // we want to use CSS style attributes
            graphics.stream(saveLocation.toString(), useCSS);
            exportTask.updateProgress(1, 1);

        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
