package drawingbot.image;

import java.awt.image.BufferedImage;

public class FilteredBufferedImage {

    public final BufferedImage source;
    public BufferedImage filtered;
    public BufferedImage output;
    public PrintResolution resolution;

    public FilteredBufferedImage(BufferedImage source){
        this.source = source;
        this.resolution = new PrintResolution(source);
    }

    public BufferedImage getSource(){
        return source;
    }

    public BufferedImage getFiltered(){
        return output == null ? source : output;
    }

    public void updateAll(){
        resolution.updateAll();
        filtered = applyFilters(source);
        output = applyCropping(filtered, resolution);
    }

    public void updateCroppingOnly(){
        resolution.updateAll();
        output = applyCropping(filtered, resolution);
        //TODO BORDER FILTERS,NEED UPDATING TOO, TOO FIX WITH OFFSCREEN RENDERING
    }

    public static BufferedImage applyAll(BufferedImage src, PrintResolution resolution){
        src = applyFilters(src);
        src = applyCropping(src, resolution);
        return src;
    }

    public static BufferedImage applyFilters(BufferedImage src){
        return ImageFilterRegistry.applyCurrentFilters(src);
    }

    public static BufferedImage applyCropping(BufferedImage src, PrintResolution resolution){
        return ImageTools.cropToPrintResolution(src, resolution);
    }

    public int getOutputHeight(){
        return output.getHeight();
    }

    public int getOutputWidth(){
        return output.getWidth();
    }

}
