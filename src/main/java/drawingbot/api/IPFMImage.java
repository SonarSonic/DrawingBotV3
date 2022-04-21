package drawingbot.api;

import java.awt.image.BufferedImage;

/**
 * Defines a {@link IPFM} which requires an Image as an input
 */
public interface IPFMImage extends IPFM {


    default BufferedImage preFilter(BufferedImage image){
        return image;
    }

    /**
     * This setting is very important to allow for an efficient {@link IPFM} as the selected setting dictates if values are cached or calculated
     * If you primarily need red/green/blue/alpha calculations go with ARGB
     * If you primarily need hue/saturation/brightness calculations go with HSB
     * If you only need luminance calculations go with Luminance
     * If you primarily need red/green/blue/alpha calculations & luminance go with ARGBY
     * If you need fast access to every value in ARGB & HSB & Luminance (very often) use Hybrid
     * @return the default colour mode is ARGB, but this is often the least efficient, especially for luminance orientated PFMs using bresenham calculations
     */
    IPixelData createPixelData(int width, int height);

    /**
     * the transparent ARGB value of the {@link IPixelData}, this is important for brightness orientated PFMs
     * @return the current transparent ARGB value
     */
    default int getTransparentARGB(){
        return -1;
    }

}
