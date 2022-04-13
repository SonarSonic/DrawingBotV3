package drawingbot.image;

import drawingbot.api.ICanvas;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;

import java.awt.image.BufferedImage;

public class FilteredBufferedImage {

    public final BufferedImage source;
    public final ImageFilterSettings imgFilterSettings;
    public final ObservableCanvas canvas;
    public ImageCanvas refCanvas;
    public ImageCanvas destCanvas;

    public BufferedImage cropped;
    public BufferedImage filtered;

    public boolean updateCropping = true;
    public boolean updateAllFilters = true;

    public FilteredBufferedImage(BufferedImage source, ImageFilterSettings imgFilterSettings, ObservableCanvas canvas){
        this.source = source;
        this.imgFilterSettings = imgFilterSettings;
        this.canvas = canvas;
        this.refCanvas = new ImageCanvas(new SimpleCanvas(canvas), source, imgFilterSettings.imageRotation.get().flipAxis);
        this.destCanvas = new ImageCanvas(canvas, source, imgFilterSettings.imageRotation.get().flipAxis){
            @Override
            public boolean flipAxis() {
                return imgFilterSettings.imageRotation.get().flipAxis;
            }
        };
    }

    public ImageCanvas getCurrentCanvas() {
        return refCanvas;
    }

    public ImageCanvas getDestCanvas() {
        return destCanvas;
    }

    public BufferedImage getSource(){
        return source;
    }

    public BufferedImage getFiltered(){
        return filtered == null ? source : filtered;
    }

    public void updateAll(){
        ImageCanvas newCanvas = new ImageCanvas(new SimpleCanvas(canvas), source, imgFilterSettings.imageRotation.get().flipAxis);
        if(cropped == null || updateCropping){
            cropped = applyCropping(source, newCanvas, imgFilterSettings);
        }
        filtered = applyFilters(cropped, updateCropping || updateAllFilters, imgFilterSettings);
        refCanvas = newCanvas;
    }

    public static BufferedImage applyAll(BufferedImage src, ICanvas canvas, ImageFilterSettings imgFilterSettings){
        src = applyCropping(src, canvas, imgFilterSettings);
        src = applyFilters(src, true, imgFilterSettings);
        return src;
    }

    public static BufferedImage applyFilters(BufferedImage src, boolean forceUpdate, ImageFilterSettings imgFilterSettings){
        return ImageTools.applyCurrentImageFilters(src, imgFilterSettings, forceUpdate, null);
    }

    public static BufferedImage applyCropping(BufferedImage src, ICanvas canvas, ImageFilterSettings imgFilterSettings){
        src = ImageTools.transformImage(src, imgFilterSettings.imageRotation.get(), imgFilterSettings.imageFlipHorizontal.get(), imgFilterSettings.imageFlipVertical.get());
        return ImageTools.cropToCanvas(src, canvas);
    }

}
