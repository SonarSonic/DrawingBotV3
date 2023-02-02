package drawingbot.files.loaders;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.format.FilteredImageData;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageFileLoader extends AbstractFileLoader {

    public ImageFileLoader(DBTaskContext context, File file, boolean internal) {
        super(context, file, internal);
    }

    @Override
    public boolean hasImageData() {
        return true;
    }

    @Override
    protected FilteredImageData call() throws Exception {

        updateProgress(-1, 1);
        updateTitle("Importing Image: " + file.toString());

        updateMessage("Loading");
        BufferedImage source = BufferedImageLoader.loadImage(file.toString(), internal);

        updateMessage("Filtering");
        FilteredImageData filtered = new FilteredImageData(file, context.project.getDrawingArea(), source);
        filtered.updateAll(context.project.getImageSettings());

        updateMessage("Finished");
        updateProgress(1, 1);
        return filtered;
    }

    @Override
    public String getFileType() {
        return "Image File";
    }
}