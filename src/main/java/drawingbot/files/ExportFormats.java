package drawingbot.files;

import drawingbot.files.exporters.*;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;

public enum ExportFormats {
    EXPORT_SVG("Export SVG", true, SVGExporter::exportBasicSVG, FileUtils.FILTER_SVG),
    EXPORT_INKSCAPE_SVG("Export Inkscape SVG", true, SVGExporter::exportInkscapeSVG, FileUtils.FILTER_SVG),
    EXPORT_IMAGE("Export Image File", false, ImageExporter::exportImage, FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA),
    EXPORT_PDF("Export PDF", true, PDFExporter::exportPDF, FileUtils.FILTER_PDF),
    EXPORT_GCODE("Export GCode File", true, GCodeExporter::exportGCode, FileUtils.FILTER_GCODE, FileUtils.FILTER_TXT),
    EXPORT_GCODE_TEST("Export GCode Test Drawing", true, GCodeExporter::exportGCodeTest, FileUtils.FILTER_GCODE, FileUtils.FILTER_TXT);

    public final String displayName;
    public final boolean isVector;
    public final FileChooser.ExtensionFilter[] filters;
    public final IExportMethod exportMethod;

    ExportFormats(String displayName, boolean isVector, IExportMethod exportMethod, FileChooser.ExtensionFilter... filters){
        this.displayName = displayName;
        this.isVector = isVector;
        this.filters = filters;
        this.exportMethod = exportMethod;
    }

    public String getDialogTitle(){
        return "Save " + displayName;
    }

    public interface IExportMethod{
        void export(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation);
    }

}
