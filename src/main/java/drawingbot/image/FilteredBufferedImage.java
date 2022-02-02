package drawingbot.image;

import drawingbot.DrawingBotV3;

import java.awt.image.BufferedImage;

public class FilteredBufferedImage {

    public final BufferedImage source;
    public PrintResolution resolution;

    public BufferedImage cropped;
    public BufferedImage filtered;

    public boolean updateCropping = true;
    public boolean updateAllFilters = true;

    public FilteredBufferedImage(BufferedImage source){
        this.source = source;
        this.resolution = new PrintResolution(DrawingBotV3.INSTANCE.drawingArea, source);
        this.resolution.updateAll();
    }

    public BufferedImage getSource(){
        return source;
    }

    public BufferedImage getFiltered(){
        return filtered == null ? source : filtered;
    }

    public void updateAll(){
        if(cropped == null || updateCropping){
            resolution = new PrintResolution(DrawingBotV3.INSTANCE.drawingArea, source);
            resolution.updateAll();
            cropped = ImageTools.cropToPrintResolution(source, resolution);
        }
        filtered = ImageTools.applyCurrentImageFilters(cropped, updateCropping || updateAllFilters);
    }

    public static BufferedImage applyAll(BufferedImage src, PrintResolution resolution){
        src = applyCropping(src, resolution);
        src = applyFilters(src);
        return src;
    }

    public static BufferedImage applyFilters(BufferedImage src){
        return ImageTools.applyCurrentImageFilters(src, true);
    }

    public static BufferedImage applyCropping(BufferedImage src, PrintResolution resolution){
        return ImageTools.cropToPrintResolution(src, resolution);
    }

}
