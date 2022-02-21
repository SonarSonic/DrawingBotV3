package drawingbot.files;

import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.Function;

public class DrawingExportHandler {

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

    public enum Category {
        SVG,
        IMAGE,
        VECTOR,
        ANIMATION
    }

    @FunctionalInterface
    public interface IExportMethod {

        void export(ExportTask exportTask, File saveLocation);

    }
}
