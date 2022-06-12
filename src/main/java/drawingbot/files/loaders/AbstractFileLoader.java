package drawingbot.files.loaders;

import drawingbot.image.format.FilteredImageData;
import drawingbot.utils.DBTask;

import java.io.File;

/**
 * Some file loaders may return null FilteredImageData, e.g. Project Loaders
 */
public abstract class AbstractFileLoader extends DBTask<FilteredImageData> {

    public File file;
    public boolean internal;

    public AbstractFileLoader(File file, boolean internal){
        this.file = file;
        this.internal = internal;
    }

    public abstract boolean hasImageData();

    /**
     * Called after the imagedata has been loaded into DrawingBotV3, allowing the loader to run additional steps
     */
    public void onImageDataLoaded(){}

}