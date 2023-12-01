package drawingbot.files;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.json.adapters.JsonAdapterDrawingExportHandler;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.Function;

@JsonAdapter(JsonAdapterDrawingExportHandler.class)
public class DrawingExportHandler {

    public final Category category;
    public final String registryName;
    public final String description;
    public final boolean isVector;
    public final FileChooser.ExtensionFilter[] filters;
    public FileChooser.ExtensionFilter selectedFilter;

    public final IExportMethod exportMethod;
    public final Function<ExportTask, Dialog<Boolean>> confirmDialog;
    public boolean isPremium = false;

    public DrawingExportHandler(Category category, String registryName, String description, boolean isVector, IExportMethod exportMethod, FileChooser.ExtensionFilter... filters){
        this(category, registryName, description, isVector, exportMethod, null, filters);
    }

    public DrawingExportHandler(Category category, String registryName, String description, boolean isVector, IExportMethod exportMethod, Function<ExportTask, Dialog<Boolean>> confirmDialog, FileChooser.ExtensionFilter... filters){
        this.category = category;
        this.registryName = registryName;
        this.description = description;
        this.isVector = isVector;
        this.exportMethod = exportMethod;
        this.confirmDialog = confirmDialog;
        this.filters = filters;
        this.selectedFilter = filters[0];
    }

    public DrawingExportHandler setPremium(){
        isPremium = true;
        return this;
    }

    public void setupExport(ExportTask task){

    }

    /**
     * Currently only used by the vpype exporter, as sometimes now output is specified by the command
     * @param exportMode
     */
    public boolean requiresSaveLocation(ExportTask.Mode exportMode){
        return true;
    }

    public String getDefaultExtension(){
        return selectedFilter.getExtensions().get(0).substring(1);
    }

    public String getDialogTitle(){
        return "Save " + description;
    }

    public String getRegistryName(){
        return registryName;
    }

    @Override
    public String toString() {
        return description;
    }

    public enum Category {
        SVG,
        IMAGE,
        VECTOR,
        ANIMATION,
        SPECIAL
    }

    @FunctionalInterface
    public interface IExportMethod {

        void export(ExportTask exportTask, File saveLocation);

    }
}
