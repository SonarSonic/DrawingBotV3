package drawingbot.files;

import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.exporters.*;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.BiFunction;

public enum ExportFormats {
    EXPORT_PDF("Export PDF", PDFExporter::exportPDF, FileUtils.FILTER_PDF),
    EXPORT_SVG("Export SVG", SVGExporter::exportSVG, FileUtils.FILTER_SVG),
    EXPORT_IMAGE("Export Image File", ImageExporter::exportImage, FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA),
    EXPORT_GCODE("Export GCode File", GCodeExporter::exportGCode, FileUtils.FILTER_TXT, FileUtils.FILTER_GCODE),
    EXPORT_GCODE_TEST("Export GCode Test Drawing", GCodeExporter::createGcodeTestFile, FileUtils.FILTER_TXT, FileUtils.FILTER_GCODE);

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

    public static boolean defaultFilter(PlottedLine line, ObservableDrawingPen pen){
        return line.pen_down && pen.isEnabled();
    }

    public interface IExportMethod{
        void export(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation);
    }

}
