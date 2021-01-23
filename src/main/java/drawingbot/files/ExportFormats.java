package drawingbot.files;

import drawingbot.plotting.PlottingTask;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.Executors;

public enum ExportFormats {
    PDF_PER_DRAWING("PDF", PDFExporter::exportPDF, FileUtils.EXPORT_PDF),
    PDF_PER_PEN("PDF per PEN", PDFExporter::exportPDFPerPen, FileUtils.EXPORT_PDF),
    SVG_PER_DRAWING("SVG", SVGExporter::exportSVG, FileUtils.EXPORT_SVG),
    SVG_PER_PEN("SVG per PEN", SVGExporter::exportSVGPerPen, FileUtils.EXPORT_SVG),
    JPG_PER_DRAWNG("JPG", ImageExporter::saveImage, FileUtils.FILTER_IMAGES),
    //JPG_PER_PEN("JPG per PEN", ".jpg"),
    //GCODE_PER_DRAWING("GCODE", ".gcode"),
    GCODE_PER_PEN("GCODE per PEN", GCodeExporter::createGcodeFiles, FileUtils.EXPORT_GCODE),
    GCODE_TEST_DRAWING("GCODE Test Drawing", GCodeExporter::createGcodeTestFile, FileUtils.EXPORT_GCODE),
    EXPORT_ALL("Export All", (task, file) -> {
        PDFExporter.exportPDF(task, file);
        SVGExporter.exportSVG(task, file);
    });

    public String displayName;
    public FileChooser.ExtensionFilter[] filters;
    public IExportMethod exportMethod;

    ExportFormats(String displayName, IExportMethod exportMethod, FileChooser.ExtensionFilter... filters){
        this.displayName = displayName;
        this.filters = filters;
        this.exportMethod = exportMethod;
    }

    public String getDialogTitle(){
        return "Save " + displayName;
    }


    public void exportOnWorkerThread(PlottingTask task, File saveLocation){
        Executors.newSingleThreadExecutor().submit(new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                exportMethod.export(task, saveLocation);
                return true;
            }
        });
    }

    public interface IExportMethod{
        void export(PlottingTask task, File saveLocation);
    }

}
