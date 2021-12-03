package drawingbot.files.exporters;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.UnitsLength;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class PDFExporter {

    public static void exportPDF(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        try {
            int width = (int)exportTask.exportResolution.getScaledWidth();
            int height = (int)exportTask.exportResolution.getScaledHeight();

            // Calculate the page size relative to the configured SVG DPI
            float scaledPageWidth = exportTask.exportResolution.printPageWidth / UnitsLength.INCHES.convertToMM * DrawingBotV3.PDF_DPI;
            float scaledPageHeight = exportTask.exportResolution.printPageHeight / UnitsLength.INCHES.convertToMM * DrawingBotV3.PDF_DPI;
            double scale = (double)scaledPageWidth / width;

            Document document = new Document(new Rectangle(scaledPageWidth, scaledPageHeight));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(saveLocation));

            document.open();
            PdfContentByte content = writer.getDirectContent();
            Graphics2D graphics = new PdfGraphics2D(content, scaledPageWidth, scaledPageHeight);
            graphics.transform(AffineTransform.getScaleInstance(scale, scale));

            Graphics2DExporter.drawBackground(exportTask, graphics, width, height);
            Graphics2DExporter.preDraw(exportTask, graphics, width, height, plottingTask);
            Graphics2DExporter.drawGeometryWithDrawingSet(exportTask, graphics, plottingTask.getDrawingSet(), geometries);
            Graphics2DExporter.postDraw(exportTask, graphics, width, height, plottingTask);
            document.close(); //dispose is already called within drawGraphics

        } catch (DocumentException | FileNotFoundException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
