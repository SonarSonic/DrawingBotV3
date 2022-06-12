package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.utils.UnitsLength;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;

//Documentation/Source: https://xmlgraphics.apache.org/batik/using/svg-generator.html
//Check with: https://validator.w3.org/
public class SVGExporter {

    public static final String SVG = "svg";
    public static final String SVG_NS = SVGConstants.SVG_NAMESPACE_URI;
    public static final String XMLNS = SVGConstants.XMLNS_NAMESPACE_URI;
    public static final String INKSCAPE_NS = "http://www.inkscape.org/namespaces/inkscape";

    public static void exportBasicSVG(ExportTask exportTask, File saveLocation){
        exportSVG(exportTask, saveLocation, false);
    }

    public static void exportInkscapeSVG(ExportTask exportTask, File saveLocation){
        exportSVG(exportTask, saveLocation, true);
    }

    public static void exportSVG(ExportTask exportTask, File saveLocation, boolean inkscape) {
        try {
            int width = (int)exportTask.exportDrawing.getCanvas().getScaledWidth();
            int height = (int)exportTask.exportDrawing.getCanvas().getScaledHeight();

            // Calculate the page size relative to the configured SVG DPI
            int scaledPageWidth = (int)CanvasUtils.getExportWidth(exportTask.exportDrawing.getCanvas(), DrawingBotV3.SVG_DPI);
            int scaledPageHeight = (int)CanvasUtils.getExportHeight(exportTask.exportDrawing.getCanvas(), DrawingBotV3.SVG_DPI);

            double scale = (double)scaledPageWidth / width;

            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            Document document = domImpl.createDocument(SVG_NS, SVG, null);

            // Get the root element (the 'svg' element).
            Element svgRoot = document.getDocumentElement();

            // Set the attributes on the root 'svg' element.
            svgRoot.setAttributeNS(null, "width", exportTask.exportDrawing.getCanvas().getWidth(UnitsLength.MILLIMETRES) + exportTask.exportDrawing.getCanvas().getUnits().getSuffix());
            svgRoot.setAttributeNS(null, "height", exportTask.exportDrawing.getCanvas().getHeight(UnitsLength.MILLIMETRES) + exportTask.exportDrawing.getCanvas().getUnits().getSuffix());

            if(inkscape){
                svgRoot.setAttributeNS(XMLNS, "xmlns:inkscape", INKSCAPE_NS);
            }

            /////BACKGROUND
            // Create a fresh document to draw background
            Document backgroundGraphicsDocument = domImpl.createDocument(SVG_NS, SVG, null);

            // Create a new instance of the SVG Generator for the new background document
            SVGGraphics2D backgroundGraphics = new SVGGraphics2D(backgroundGraphicsDocument);
            Element background = backgroundGraphicsDocument.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);

            background.setAttribute("id", "Background");
            if(inkscape){
                background.setAttribute("inkscape:groupmode", "layer");
                background.setAttribute("inkscape:label", "Background");
            }

            // Draw the background
            backgroundGraphics.setTopLevelGroup(background);
            backgroundGraphics.setSVGCanvasSize(new Dimension(scaledPageWidth, scaledPageHeight));
            backgroundGraphics.transform(AffineTransform.getScaleInstance(scale, scale));

            Graphics2DExporter.drawBackground(backgroundGraphics, width, height);
            Graphics2DExporter.preDraw(exportTask, backgroundGraphics);
            Graphics2DExporter.postDraw(exportTask, backgroundGraphics);

            // Transfer the background graphics document into the host document
            if(background.hasChildNodes()){
                Node graphicsNode = document.importNode(background, true);
                svgRoot.appendChild(graphicsNode);
                backgroundGraphics.dispose();
            }

            /////PENS

            int index = 0;
            for(ObservableDrawingPen drawingPen : exportTask.exportRenderOrder){

                // Find the pen to render
                String safeName = drawingPen.getDisplayName().replace(' ', '_');

                // Create a fresh document to draw each pen into
                Document graphicsDocument = domImpl.createDocument(SVG_NS, SVG, null);

                // Create a new instance of the SVG Generator for the new document
                SVGGraphics2D graphics = new SVGGraphics2D(graphicsDocument);
                Element group = graphicsDocument.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);

                group.setAttribute("id", safeName);
                if(inkscape){
                    group.setAttribute("inkscape:groupmode", "layer");
                    group.setAttribute("inkscape:label", ConfigFileHandler.getApplicationSettings().svgLayerRenaming ? "Pen" + (index+1) : safeName);
                }

                // Draw the pen's paths
                graphics.setTopLevelGroup(group);
                graphics.setSVGCanvasSize(new Dimension(scaledPageWidth, scaledPageHeight));
                graphics.transform(AffineTransform.getScaleInstance(scale, scale));

                Graphics2DExporter.preDraw(exportTask, graphics);
                Graphics2DExporter.drawGeometries(exportTask, graphics, (drawing, geometry, pen) -> pen == drawingPen);
                Graphics2DExporter.postDraw(exportTask, graphics);

                // Transfer the graphics document into the host document
                if(group.hasChildNodes()){
                    Node graphicsNode = document.importNode(group, true);
                    svgRoot.appendChild(graphicsNode);
                }
                graphics.dispose();
                index++;
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
