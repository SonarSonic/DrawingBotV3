package drawingbot.files.loaders;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.format.FilteredImageData;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.DBTask;

import java.io.File;

/**
 * Some file loaders may return null FilteredImageData, e.g. Project Loaders
 */
public abstract class AbstractFileLoader extends DBTask<FilteredImageData> {

    public File file;
    public boolean internal;
    public boolean isSubTask;

    public AbstractFileLoader(DBTaskContext context, File file, boolean internal, boolean isSubTask){
        super(context);
        this.file = file;
        this.internal = internal;
        this.isSubTask = isSubTask;
    }

    public abstract boolean hasImageData();

    public abstract String getFileTypeDisplayName();

    /**
     * Called after the imagedata has been loaded into DrawingBotV3, allowing the loader to run additional steps
     */
    public void onFileLoaded(){
        if(!isSubTask){
            NotificationOverlays.INSTANCE.showWithSubtitle("Loaded " + getFileTypeDisplayName(), file.toString());
        }
    }

}