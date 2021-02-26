package drawingbot.image;

import java.awt.image.BufferedImage;

public class FilteredBufferedImage {

    public final BufferedImage source;
    public BufferedImage filtered;

    public FilteredBufferedImage(BufferedImage source){
        this.source = source;
    }

    public BufferedImage getSource(){
        return source;
    }

    public BufferedImage getFiltered(){
        return filtered == null ? source : filtered;
    }

    public BufferedImage applyCurrentFilters(){
        return filtered = applyCurrentFilters(getSource());
    }

    public BufferedImage applyCurrentFilters(BufferedImage src){
        return ImageFilterRegistry.applyCurrentFilters(src);
    }

    public int getHeight(){
        return source.getHeight();
    }

    public int getWidth(){
        return source.getWidth();
    }

}
