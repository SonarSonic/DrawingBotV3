package drawingbot.files.loaders;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.format.ImageData;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.DBTask;

import java.io.File;
import java.util.Set;

/**
 * Some file loaders may return null FilteredImageData, e.g. Project Loaders
 */
public abstract class AbstractFileLoader extends DBTask<ImageData> {

    public File file;
    public Set<FileLoaderFlags> flags;

    public AbstractFileLoader(DBTaskContext context, File file, Set<FileLoaderFlags> flags){
        super(context);
        this.file = file;
        this.flags = flags;
    }

    public abstract boolean hasImageData();

    public abstract String getFileTypeDisplayName();

    /**
     * Called after the image data has been loaded into DrawingBotV3, allowing the loader to run additional steps
     */
    public void onFileLoaded(){
        if(!flags.contains(FileLoaderFlags.SUB_TASK)){
            NotificationOverlays.INSTANCE.showWithSubtitle("Loaded " + getFileTypeDisplayName(), file.toString());
        }
    }

}