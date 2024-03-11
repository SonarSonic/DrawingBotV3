package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.ImageFilteringService;
import drawingbot.image.format.ImageData;
import drawingbot.render.renderer.JFXRenderer;
import drawingbot.render.renderer.RendererFactory;
import drawingbot.utils.flags.Flags;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

//TODO REWRITE IMAGE FILTERING MAKE INSTANCEABLE
public class JFXFilteredImageDisplayMode extends DisplayModeImage implements IJFXDisplayMode {

    protected WritableImage cacheImage = null;
    protected boolean imageChanged = false;

    public ImageFilteringService imageFilteringService;

    @Override
    public RendererFactory getRendererFactory() {
        return JFXRenderer.JFX_RENDERER_FACTORY;
    }

    @Override
    public void init() {
        super.init();
        this.imageFilteringService = new ImageFilteringService();
        this.imageFilteringService.imageDataProperty().bind(imageDataProperty());
        this.imageFilteringService.imageSettingsProperty().bind(imageSettingsProperty());
        this.imageFilteringService.targetCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
        this.imageFilteringService.setEnabled(true);

        this.canvasProperty().bind(Bindings.createObjectBinding(() -> getDisplayCanvas() == null ? getFallbackCanvas() : getDisplayCanvas(), fallbackCanvasProperty(), displayCanvasProperty()));

        //N.B. These are the only props which bind us to DrawingBotV3
        this.fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
        this.imageDataProperty().bind(DrawingBotV3.INSTANCE.projectOpenImage);
        this.imageSettingsProperty().bind(DrawingBotV3.INSTANCE.projectImageSettings);
        this.imageFilteringService.getImageFilteringService().setOnSucceeded(e -> {
            setDisplayedImage(imageFilteringService.getFilteredImage());
            setDisplayCanvas(imageFilteringService.getFilteredCanvas());
            imageChanged = true;
            getViewport().getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
        });
    }

    @Override
    public void onRenderTick(JFXRenderer jfr) {
        this.imageFilteringService.update();
    }

    @Override
    public void doRender(JFXRenderer jfr) {
        jfr.clearCanvas();

        if(getDisplayedImage() != null){
            if(imageChanged || cacheImage == null){
                cacheImage = SwingFXUtils.toFXImage(getDisplayedImage(), cacheImage);
                imageChanged = false;
            }
            jfr.graphicsFX.scale(jfr.getRenderScale(), jfr.getRenderScale());
            jfr.graphicsFX.translate(getCanvas().getScaledDrawingOffsetX(), getCanvas().getScaledDrawingOffsetY());
            jfr.graphicsFX.drawImage(cacheImage, 0, 0);
        }
    }

    @Override
    public boolean isRenderDirty(JFXRenderer jfr) {
        return getViewport().getRenderFlags().anyMatch(Flags.FORCE_REDRAW);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ImageFilterSettings> imageSettings = new SimpleObjectProperty<>();

    public ImageFilterSettings getImageSettings() {
        return imageSettings.get();
    }

    public ObjectProperty<ImageFilterSettings> imageSettingsProperty() {
        return imageSettings;
    }

    public void setImageSettings(ImageFilterSettings imageSettings) {
        this.imageSettings.set(imageSettings);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ImageData> imageData = new SimpleObjectProperty<>();

    public ImageData getImageData() {
        return imageData.get();
    }

    public ObjectProperty<ImageData> imageDataProperty() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData.set(imageData);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ICanvas> displayCanvas = new SimpleObjectProperty<>();

    public ICanvas getDisplayCanvas() {
        return displayCanvas.get();
    }

    public ObjectProperty<ICanvas> displayCanvasProperty() {
        return displayCanvas;
    }

    public void setDisplayCanvas(ICanvas displayCanvas) {
        this.displayCanvas.set(displayCanvas);
    }

    @Override
    public String getName() {
        return "Image";
    }
}
