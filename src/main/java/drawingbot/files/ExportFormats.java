package drawingbot.files;

import drawingbot.files.exporters.*;
import drawingbot.geom.basic.IGeometry;
import drawingbot.javafx.controls.DialogExportGCodeBegin;
import drawingbot.javafx.controls.DialogExportHPGLBegin;
import drawingbot.javafx.controls.DialogExportSequenceBegin;
import drawingbot.plotting.PlottingTask;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public enum ExportFormats {
    EXPORT_SVG("Export SVG (.svg)", true, SVGExporter::exportBasicSVG, FileUtils.FILTER_SVG),
    EXPORT_INKSCAPE_SVG("Export Inkscape SVG (.svg)", true, SVGExporter::exportInkscapeSVG, FileUtils.FILTER_SVG),
    EXPORT_IMAGE("Export Image File (.png, .jpg, etc.)", false, ImageExporter::exportImage, FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA),
    EXPORT_HPGL("Export HPGL File (.hpgl)", true, HPGLExporter::exportHPGL, e -> new DialogExportHPGLBegin(), FileUtils.FILTER_HPGL, FileUtils.FILTER_TXT),
    EXPORT_PDF("Export PDF (.pdf)", true, PDFExporter::exportPDF, FileUtils.FILTER_PDF),
    EXPORT_GCODE("Export GCode File (.gcode, .txt)", true, GCodeExporter::exportGCode, e -> new DialogExportGCodeBegin(), FileUtils.FILTER_GCODE, FileUtils.FILTER_TXT),
    EXPORT_GCODE_TEST("Export GCode Test Drawing (.gcode, .txt)", true, GCodeExporter::exportGCodeTest, e -> new DialogExportGCodeBegin(), FileUtils.FILTER_GCODE, FileUtils.FILTER_TXT),
    EXPORT_IMAGE_SEQUENCE("Export Animation - (Image Sequence, .png, .jpg)", false, AnimationExporter::exportAnimation, e -> new DialogExportSequenceBegin(), FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA),
    EXPORT_MP4("Export Animation - (H.264, .mp4)", false, VideoExporter::exportMP4, e -> new DialogExportSequenceBegin(), FileUtils.FILTER_MP4),
    EXPORT_PRORES("Export Animation - (ProRes 422, .mov)", false, VideoExporter::exportProRes, e -> new DialogExportSequenceBegin(), FileUtils.FILTER_MOV);

    public final String displayName;
    public final boolean isVector;
    public final FileChooser.ExtensionFilter[] filters;
    public final IExportMethod exportMethod;
    public final Function<ExportTask, Dialog<Boolean>> confirmDialog;

    ExportFormats(String displayName, boolean isVector, IExportMethod exportMethod, FileChooser.ExtensionFilter... filters){
        this(displayName, isVector, exportMethod, null, filters);
    }

    ExportFormats(String displayName, boolean isVector, IExportMethod exportMethod, Function<ExportTask, Dialog<Boolean>> confirmDialog, FileChooser.ExtensionFilter... filters){
        this.displayName = displayName;
        this.isVector = isVector;
        this.exportMethod = exportMethod;
        this.confirmDialog = confirmDialog;
        this.filters = filters;
    }

    public String getDialogTitle(){
        return "Save " + displayName;
    }

    public interface IExportMethod{
        void export(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation);
    }

}