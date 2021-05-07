package drawingbot.image;

import drawingbot.DrawingBotV3;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class ImageFilteringTask extends Task<FilteredBufferedImage> {

    public FilteredBufferedImage image;

    public ImageFilteringTask(FilteredBufferedImage image){
        this.image = image;
    }

    @Override
    protected FilteredBufferedImage call() {
        image.updateAll();
        Platform.runLater(() -> {
            DrawingBotV3.RENDERER.forceCanvasUpdate(); //force update canvas
            DrawingBotV3.INSTANCE.isUpdatingFilters = false;
            DrawingBotV3.INSTANCE.reRender();
        });
        return image;
    }
}
