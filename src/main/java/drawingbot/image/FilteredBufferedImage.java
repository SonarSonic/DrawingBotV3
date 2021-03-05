package drawingbot.image;

import java.awt.image.BufferedImage;

public class FilteredBufferedImage {

    public final BufferedImage source;
    public BufferedImage filtered;
    public PrintResolution resolution;

    public FilteredBufferedImage(BufferedImage source){
        this.source = source;
        this.resolution = new PrintResolution(source);
        this.resolution.updateAll();
    }

    public BufferedImage getSource(){
        return source;
    }

    public BufferedImage getFiltered(){
        return filtered == null ? source : filtered;
    }

    public void updateAll(){
        resolution.updateAll();
        filtered = applyAll(source, resolution);
    }

    public static BufferedImage applyAll(BufferedImage src, PrintResolution resolution){
        src = applyCropping(src, resolution);
        src = applyFilters(src);
        return src;
    }

    public static BufferedImage applyFilters(BufferedImage src){
        return ImageTools.applyCurrentImageFilters(src);
    }

    public static BufferedImage applyCropping(BufferedImage src, PrintResolution resolution){
        return ImageTools.cropToPrintResolution(src, resolution);
    }

}
