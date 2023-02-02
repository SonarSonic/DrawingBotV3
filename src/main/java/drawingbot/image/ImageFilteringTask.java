package drawingbot.image;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.format.FilteredImageData;
import drawingbot.utils.DBTask;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImageFilteringTask extends DBTask<FilteredImageData> {

    public FilteredImageData image;
    public AtomicBoolean updating = new AtomicBoolean(false);

    public ImageFilteringTask(DBTaskContext context, FilteredImageData image){
        super(context);
        this.image = image;
    }

    @Override
    protected FilteredImageData call() {
        updateTitle("Image Filtering");
        image.updateAll(context.project.imageSettings.get());
        updating.set(true);
        Platform.runLater(() -> {
            context.project.onImageRenderingUpdated();
            updating.set(false);
        });
        return image;
    }
}
