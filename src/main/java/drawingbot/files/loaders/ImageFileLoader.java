package drawingbot.files.loaders;

import drawingbot.DrawingBotV3;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.format.FilteredImageData;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageFileLoader extends AbstractFileLoader {

    public ImageFileLoader(File file, boolean internal) {
        super(file, internal);
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
        FilteredImageData filtered = new FilteredImageData(file, DrawingBotV3.INSTANCE.drawingArea, source);
        filtered.updateAll(DrawingBotV3.INSTANCE.imgFilterSettings);

        updateMessage("Finished");
        updateProgress(1, 1);
        return filtered;
    }
}