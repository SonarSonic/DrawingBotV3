package drawingbot.files;

import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottingTask;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DrawingExportHandler implements IExportMethod {

    public final Category category;
    public final String displayName;
    public final boolean isVector;
    public final FileChooser.ExtensionFilter[] filters;
    public final IExportMethod exportMethod;
    public final Function<ExportTask, Dialog<Boolean>> confirmDialog;
    public boolean isPremium = false;

    public DrawingExportHandler(Category category, String displayName, boolean isVector, IExportMethod exportMethod, FileChooser.ExtensionFilter... filters){
        this(category, displayName, isVector, exportMethod, null, filters);
    }

    public DrawingExportHandler(Category category, String displayName, boolean isVector, IExportMethod exportMethod, Function<ExportTask, Dialog<Boolean>> confirmDialog, FileChooser.ExtensionFilter... filters){
        this.category = category;
        this.displayName = displayName;
        this.isVector = isVector;
        this.exportMethod = exportMethod;
        this.confirmDialog = confirmDialog;
        this.filters = filters;
    }

    public DrawingExportHandler setPremium(){
        isPremium = true;
        return this;
    }

    public String getDialogTitle(){
        return "Save " + displayName;
    }

    @Override
    public void export(ExportTask exportTask, File saveLocation) {
        exportMethod.export(exportTask, saveLocation);
    }

    public enum Category {
        SVG,
        IMAGE,
        VECTOR,
        ANIMATION;
    }
}
