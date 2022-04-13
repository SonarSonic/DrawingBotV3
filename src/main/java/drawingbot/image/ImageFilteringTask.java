package drawingbot.image;

import drawingbot.DrawingBotV3;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImageFilteringTask extends Task<FilteredBufferedImage> {

    public FilteredBufferedImage image;
    public AtomicBoolean updating = new AtomicBoolean(false);

    public ImageFilteringTask(FilteredBufferedImage image){
        this.image = image;
    }

    @Override
    protected FilteredBufferedImage call() {
        updateTitle("Image Filtering");
        image.updateAll();
        updating.set(true);
        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.onImageChanged();
            updating.set(false);
        });
        return image;
    }
}
