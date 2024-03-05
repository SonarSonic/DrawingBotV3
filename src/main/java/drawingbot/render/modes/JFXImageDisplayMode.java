package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.render.renderer.JFXRenderer;
import drawingbot.render.renderer.RendererFactory;
import drawingbot.utils.flags.Flags;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class JFXImageDisplayMode extends DisplayModeImage implements IJFXDisplayMode{

    private WritableImage cacheImage = null;
    private boolean imageChanged = false;

    @Override
    public RendererFactory getRendererFactory() {
        return JFXRenderer.JFX_RENDERER_FACTORY;
    }

    @MustBeInvokedByOverriders
    @Override
    public void init() {
        super.init();
        displayedImageProperty().addListener((observable, oldValue, newValue) -> {
            if(isActive()){
                getViewport().getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
            }
            imageChanged = true;
        });
    }

    @Override
    public void doRender(JFXRenderer jfr) {
        if (getViewport().getRenderFlags().anyMatchAndMarkClear(Flags.FORCE_REDRAW)) {
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
    }

    ////////////////////////////////////////////////////////

    public static class Reference extends JFXImageDisplayMode{

        @Override
        public void init() {
            super.init();
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
            JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.projectCurrentDrawing, (observable, oldValue, newValue) -> {
                if(newValue == null || newValue.getReferenceImage() == null){
                    if(!canvasProperty().isBound()){
                        canvasProperty().bind(fallbackCanvasProperty());
                    }
                    setDisplayedImage(null);
                }else{
                    canvasProperty().unbind();
                    setCanvas(newValue.getCanvas());
                    setDisplayedImage(newValue.getReferenceImage());
                }
            });
        }

        @Override
        public String getName() {
            return "Reference";
        }
    }

    ////////////////////////////////////////////////////////

    public static class Lightened extends JFXImageDisplayMode{

        @Override
        public void init() {
            super.init();
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
            JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.projectCurrentDrawing, (observable, oldValue, newValue) -> {
                if(newValue == null || newValue.getPlottingImage() == null){
                    if(!canvasProperty().isBound()){
                        canvasProperty().bind(fallbackCanvasProperty());
                    }
                    setDisplayedImage(null);
                }else{
                    canvasProperty().unbind();
                    setCanvas(newValue.getCanvas());
                    setDisplayedImage(newValue.getPlottingImage());
                }
            });
        }

        @Override
        public String getName() {
            return "Lightened";
        }
    }

    ////////////////////////////////////////////////////////

    public static class ToneMap extends JFXImageDisplayMode{

        @Override
        public void init() {
            super.init();
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
            JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.projectCurrentDrawing, (observable, oldValue, newValue) -> {
                if(newValue == null || newValue.getToneMap() == null){
                    if(!canvasProperty().isBound()){
                        canvasProperty().bind(fallbackCanvasProperty());
                    }
                    setDisplayedImage(null);
                }else{
                    canvasProperty().unbind();
                    setCanvas(new SimpleCanvas(newValue.getToneMap().getWidth(), newValue.getToneMap().getHeight()));
                    setDisplayedImage(newValue.getToneMap());
                }
            });
        }

        @Override
        public String getName() {
            return "Tone Map";
        }
    }

    ////////////////////////////////////////////////////////

    public static class Original extends JFXImageDisplayMode{

        @Override
        public void init() {
            super.init();
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
            JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.projectCurrentDrawing, (observable, oldValue, newValue) -> {
                if(newValue == null || newValue.getOriginalImage() == null){
                    if(!canvasProperty().isBound()){
                        canvasProperty().bind(fallbackCanvasProperty());
                    }
                    setDisplayedImage(null);
                }else{
                    canvasProperty().unbind();
                    setCanvas(new SimpleCanvas(newValue.getOriginalImage().getWidth(), newValue.getOriginalImage().getHeight()));
                    setDisplayedImage(newValue.getOriginalImage());
                }
            });
        }

        @Override
        public String getName() {
            return "Original";
        }
    }
}
