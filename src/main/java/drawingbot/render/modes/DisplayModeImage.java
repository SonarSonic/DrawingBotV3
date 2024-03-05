package drawingbot.render.modes;

import drawingbot.api.ICanvas;
import drawingbot.render.viewport.Viewport;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.awt.image.BufferedImage;

public abstract class DisplayModeImage extends DisplayModeBase{

    private boolean initialized;

    public DisplayModeImage(){}

    @MustBeInvokedByOverriders
    public void init() {
        initialized = true;
    }

    @Override
    public void activateDisplayMode(Viewport viewport) {
        super.activateDisplayMode(viewport);
        if(!initialized){
            init();
        }
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<BufferedImage> displayedImage = new SimpleObjectProperty<>();

    public BufferedImage getDisplayedImage() {
        return displayedImage.get();
    }

    public ObjectProperty<BufferedImage> displayedImageProperty() {
        return displayedImage;
    }

    public void setDisplayedImage(BufferedImage displayedImage) {
        this.displayedImage.set(displayedImage);
    }

    ////////////////////////////////////////////////////////

    /**
     * If no drawing/image is currently being rendered we will instead render this fallback canvas.
     *
     * Typically this will be the configured project drawing area, though it can be used to set alternative fallbacks
     */
    private final ObjectProperty<ICanvas> fallbackCanvas = new SimpleObjectProperty<>();

    public ICanvas getFallbackCanvas() {
        return fallbackCanvas.get();
    }

    public ObjectProperty<ICanvas> fallbackCanvasProperty() {
        return fallbackCanvas;
    }

    public void setFallbackCanvas(ICanvas fallbackCanvas) {
        this.fallbackCanvas.set(fallbackCanvas);
    }

    ////////////////////////////////////////////////////////

}
