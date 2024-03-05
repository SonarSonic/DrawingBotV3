package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.image.ImageFilteringTask;
import drawingbot.image.format.FilteredImageData;
import drawingbot.render.renderer.JFXRenderer;
import drawingbot.render.renderer.RendererFactory;
import drawingbot.utils.flags.Flags;
import javafx.embed.swing.SwingFXUtils;

//TODO REWRITE IMAGE FILTERING MAKE INSTANCEABLE
public class JFXFilteredImageDisplayMode extends DisplayModeImage implements IJFXDisplayMode {

    @Override
    public RendererFactory getRendererFactory() {
        return JFXRenderer.JFX_RENDERER_FACTORY;
    }

    public ImageFilteringTask filteringTask;

    @Override
    public void preRender(JFXRenderer jfr) {

        //update the filtered image
        FilteredImageData openImage = DrawingBotV3.project().openImage.get();
        if(openImage != null){
            if(openImage.nextUpdate != FilteredImageData.UpdateType.NONE){
                if(filteringTask == null || !filteringTask.updating.get()){
                    DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.imageFilteringService, filteringTask = new ImageFilteringTask(DrawingBotV3.context(), openImage));
                    getViewport().getRenderFlags().markForClear(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED);
                }
            }
        }

        //setup the canvas
        ICanvas canvas = getDefaultCanvas();
        if (openImage != null) {
            canvas = openImage.getDestCanvas();
        }
        setCanvas(canvas);
    }

    @Override
    public void doRender(JFXRenderer jfr) {
        //render the image
        if (getViewport().getRenderFlags().anyMatchAndMarkClear(Flags.FORCE_REDRAW, Flags.OPEN_IMAGE_UPDATED)) {
            jfr.clearCanvas();
            FilteredImageData openImage = DrawingBotV3.project().openImage.get();
            if (openImage != null) {
                jfr.graphicsFX.scale(jfr.getRenderScale(), jfr.getRenderScale());
                jfr.graphicsFX.translate(openImage.getDestCanvas().getScaledDrawingOffsetX(), openImage.getDestCanvas().getScaledDrawingOffsetY());
                jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(openImage.getFilteredImage(), null), 0, 0);
            }
        }
    }

    @Override
    public String getName() {
        return "Image";
    }
}
