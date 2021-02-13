package drawingbot.files.exporters;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.function.BiFunction;

public class PDFExporter {

    public static void exportPDF(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {

        try {
            int width = plottingTask.getPlottingImage().getWidth();
            int height = plottingTask.getPlottingImage().getHeight();
            Document document = new Document(new Rectangle(width, height));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(saveLocation));

            document.open();
            PdfContentByte content = writer.getDirectContent();
            Graphics2DExporter.exportImage(content.createGraphicsShapes(width, height), exportTask, plottingTask, lineFilter);
            document.close();

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        DrawingBotV3.logger.info("PDF created:  " + saveLocation.getName());
    }
}
