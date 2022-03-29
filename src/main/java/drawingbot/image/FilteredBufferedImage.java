package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;

import java.awt.image.BufferedImage;

public class FilteredBufferedImage {

    public final BufferedImage source;
    public ImageCanvas refCanvas;

    public BufferedImage cropped;
    public BufferedImage filtered;

    public boolean updateCropping = true;
    public boolean updateAllFilters = true;

    public FilteredBufferedImage(BufferedImage source){
        this.source = source;
        this.refCanvas = new ImageCanvas(new SimpleCanvas(DrawingBotV3.INSTANCE.drawingArea), source, DrawingBotV3.INSTANCE.imageRotation.get().flipAxis);
    }

    public ImageCanvas getCanvas() {
        return refCanvas;
    }

    public BufferedImage getSource(){
        return source;
    }

    public BufferedImage getFiltered(){
        return filtered == null ? source : filtered;
    }

    public void updateAll(){
        ImageCanvas newCanvas = new ImageCanvas(new SimpleCanvas(DrawingBotV3.INSTANCE.drawingArea), source, DrawingBotV3.INSTANCE.imageRotation.get().flipAxis);
        if(cropped == null || updateCropping){
            cropped = applyCropping(source, newCanvas);
        }
        filtered = applyFilters(cropped, updateCropping || updateAllFilters);
        refCanvas = newCanvas;
    }

    public static BufferedImage applyAll(BufferedImage src, ICanvas canvas){
        src = applyCropping(src, canvas);
        src = applyFilters(src, true);
        return src;
    }

    public static BufferedImage applyFilters(BufferedImage src, boolean forceUpdate){
        return ImageTools.applyCurrentImageFilters(src, forceUpdate);
    }

    public static BufferedImage applyCropping(BufferedImage src, ICanvas canvas){
        src = ImageTools.transformImage(src, DrawingBotV3.INSTANCE.imageRotation.get(), DrawingBotV3.INSTANCE.imageFlipHorizontal.get(), DrawingBotV3.INSTANCE.imageFlipVertical.get());
        return ImageTools.cropToCanvas(src, canvas);
    }

}
