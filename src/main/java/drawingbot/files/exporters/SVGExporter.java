package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPointFilter;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.Units;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;

//Documentation/Source: https://xmlgraphics.apache.org/batik/using/svg-generator.html
//Check with: https://validator.w3.org/
//TODO: NEED HELP MAKING INKSCAPE RECOGNISE THE LAYERS
public class SVGExporter {

    public static final String SVG = "svg";
    public static final String SVG_NS = SVGConstants.SVG_NAMESPACE_URI;
    public static final String XMLNS = SVGConstants.XMLNS_NAMESPACE_URI;
    //public static final String INKSCAPE_NS = "http://www.inkscape.org/namespaces/inkscape";

    public static void exportSVG(ExportTask exportTask, PlottingTask plottingTask, IPointFilter lineFilter, String extension, File saveLocation) {
        try {
            int width = (int)plottingTask.resolution.getScaledWidth();
            int height = (int)plottingTask.resolution.getScaledHeight();

            // Calculate the page size relative to the configured SVG DPI
            int scaledPageWidth = (int)Math.ceil((plottingTask.resolution.printPageWidth / Units.INCHES.convertToMM) * DrawingBotV3.SVG_DPI);
            int scaledPageHeight = (int)Math.ceil((plottingTask.resolution.printPageHeight / Units.INCHES.convertToMM) * DrawingBotV3.SVG_DPI);
            double scale = (double)scaledPageWidth / width;

            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            Document document = domImpl.createDocument(SVG_NS, SVG, null);

            // Get the root element (the 'svg' element).
            Element svgRoot = document.getDocumentElement();

            // Set the attributes on the root 'svg' element.
            svgRoot.setAttributeNS(null, "width", String.valueOf(scaledPageWidth));
            svgRoot.setAttributeNS(null, "height", String.valueOf(scaledPageHeight));
            //svgRoot.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape" );

            ObservableDrawingSet drawingSet = plottingTask.plottedDrawing.drawingPenSet;
            int[] renderOrder = drawingSet.getCurrentRenderOrder();
            for (int p = renderOrder.length-1; p >= 0; p --) {
                // Find the pen to render
                ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(renderOrder[p]);
                String safeName = drawingPen.getDisplayName().replace(' ', '_');

                // Create a fresh document to draw each pen into
                Document graphicsDocument = domImpl.createDocument(SVG_NS, SVG, null);

                // Create a new instance of the SVG Generator for the new document
                SVGGraphics2D graphics = new SVGGraphics2D(graphicsDocument);
                Element group = graphicsDocument.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);

                group.setAttribute("id", safeName);
                //group.setAttribute("inkscape:groupmode", "layer");
                //group.setAttribute("inkscape:label", safeName);

                // Draw the pen's paths
                graphics.setTopLevelGroup(group);
                graphics.setSVGCanvasSize(new Dimension(scaledPageWidth, scaledPageHeight));
                graphics.transform(AffineTransform.getScaleInstance(scale, scale));
                Graphics2DExporter.drawGraphics(graphics, width, height, exportTask, plottingTask, (line, pen) -> lineFilter.filter(line, pen) && pen == drawingPen);

                // Transfer the graphics document into the host document
                if(group.hasChildNodes()){
                    Node graphicsNode = document.importNode(group, true);
                    svgRoot.appendChild(graphicsNode);
                    graphics.dispose();
                }
            }


            // Finally, stream out SVG to the standard output using UTF-8 encoding.
            exportTask.updateMessage("Encoding SVG");
            exportTask.updateProgress(-1, 1);


            SVGGraphics2D generator = new SVGGraphics2D(document);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(saveLocation.toString()), SVGGraphics2D.DEFAULT_XML_ENCODING);
            generator.stream(svgRoot, writer, true, true);
            writer.flush();
            writer.close();

            exportTask.updateProgress(1, 1);

        } catch (IOException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
