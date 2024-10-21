package drawingbot.files.loaders;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.format.ImageData;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

public class ImageFileLoader extends AbstractFileLoader {

    public ImageFileLoader(DBTaskContext context, File file, Set<FileLoaderFlags> flags) {
        super(context, file, flags);
    }

    @Override
    public boolean hasImageData() {
        return true;
    }

    @Override
    protected ImageData call() throws Exception {

        updateProgress(-1, 1);
        updateTitle("Importing Image: " + file.toString());

        updateMessage("Loading");
        BufferedImage source = BufferedImageLoader.loadImage(file.toString(), flags.contains(FileLoaderFlags.INTERNAL_FILE));


        updateMessage("Finished");
        updateProgress(1, 1);
        return new ImageData(file, source);
    }

    @Override
    public String getFileTypeDisplayName() {
        return "Image File";
    }
}