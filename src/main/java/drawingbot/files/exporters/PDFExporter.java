package drawingbot.files.exporters;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
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
            int width = plottingTask.getPixelWidth();
            int height = plottingTask.getPixelHeight();

            Document document = new Document(new Rectangle(width, height));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(saveLocation));

            document.open();
            PdfContentByte content = writer.getDirectContent();
            Graphics2DExporter.drawGraphics(new PdfGraphics2D(content, width, height), width, height, exportTask, plottingTask, lineFilter);
            document.close(); //dispose is already called within drawGraphics

        } catch (DocumentException | FileNotFoundException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }
}
