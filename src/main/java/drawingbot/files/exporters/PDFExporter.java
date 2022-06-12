package drawingbot.files.exporters;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.plotting.canvas.CanvasUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PDFExporter {

    public static void exportPDF(ExportTask exportTask, File saveLocation){
        try {
            int width = (int)exportTask.exportDrawing.getCanvas().getScaledWidth();
            int height = (int)exportTask.exportDrawing.getCanvas().getScaledHeight();

            // Calculate the page size relative to the configured PDF DPI
            int scaledPageWidth = (int) CanvasUtils.getExportWidth(exportTask.exportDrawing.getCanvas(), DrawingBotV3.PDF_DPI);
            int scaledPageHeight = (int)CanvasUtils.getExportHeight(exportTask.exportDrawing.getCanvas(), DrawingBotV3.PDF_DPI);
            double scale = (double)scaledPageWidth / width;

            Document document = new Document(new Rectangle(scaledPageWidth, scaledPageHeight));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(saveLocation));
            
            PdfGState gstate = new PdfGState();
            gstate.setBlendMode(getPDFBlendMode());
            
            document.open();
            PdfContentByte content = writer.getDirectContent();
            
            content.setGState(gstate);
            Graphics2D graphics = new PdfGraphics2D(content, scaledPageWidth, scaledPageHeight);
            graphics.transform(AffineTransform.getScaleInstance(scale, scale));
            
            Graphics2DExporter.drawBackground(graphics, width, height);
            Graphics2DExporter.preDraw(exportTask, graphics);
            Graphics2DExporter.drawGeometries(exportTask, graphics, IGeometryFilter.BYPASS_FILTER);
            Graphics2DExporter.postDraw(exportTask, graphics);
            document.close(); //dispose is already called within drawGraphics

        } catch (DocumentException | FileNotFoundException e) {
            exportTask.setError(e.getMessage());
            e.printStackTrace();
        }
    }

    public static PdfName getPDFBlendMode(){
        switch (DrawingBotV3.INSTANCE.blendMode.get()) {
            case MULTIPLY:
                return PdfGState.BM_MULTIPLY;
            case SCREEN:
                return PdfGState.BM_SCREEN;
            case DARKEN:
                return PdfGState.BM_DARKEN;
            case LIGHTEN:
                return PdfGState.BM_LIGHTEN;
            case HARD_LIGHT:
                return PdfGState.BM_HARDLIGHT;
            case DIFFERENCE:
                return PdfGState.BM_DIFFERENCE;
            case EXCLUSION:
                return PdfGState.BM_EXCLUSION;                
            default:
                return PdfGState.BM_NORMAL; // RED, GREEN, BLUE, and ADD blend modes are not supported by iText
        }
    }    
}
