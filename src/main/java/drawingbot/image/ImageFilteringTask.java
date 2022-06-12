package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.image.format.FilteredImageData;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImageFilteringTask extends Task<FilteredImageData> {

    public FilteredImageData image;
    public AtomicBoolean updating = new AtomicBoolean(false);

    public ImageFilteringTask(FilteredImageData image){
        this.image = image;
    }

    @Override
    protected FilteredImageData call() {
        updateTitle("Image Filtering");
        image.updateAll(DrawingBotV3.INSTANCE.imgFilterSettings);
        updating.set(true);
        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.onImageChanged();
            updating.set(false);
        });
        return image;
    }
}
