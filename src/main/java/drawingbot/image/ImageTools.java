package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.filters.ObservableImageFilter;
import javafx.scene.paint.Color;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ImageTools {

    public static BufferedImage applyCurrentImageFilters(BufferedImage image){
        for(BufferedImageOp filter : ImageTools.createBufferedImageOps(DrawingBotV3.INSTANCE.currentFilters)){
            image = filter.filter(image, null);
        }
        return image;
    }

    public static List<BufferedImageOp> createBufferedImageOps(List<ObservableImageFilter> observableImageFilters){
        List<BufferedImageOp> filters = new ArrayList<>();
        for(ObservableImageFilter filter : observableImageFilters){
            if(filter.enable.get()){
                BufferedImageOp instance = filter.filterFactory.instance();
                filter.filterSettings.forEach(setting -> setting.applySetting(instance));
                filters.add(instance);
            }
        }
        return filters;
    }


    //// BUFFERED IMAGE FILTERS

    public static BufferedImage lazyConvolutionFilter(BufferedImage image, float[][] matrix){
        return lazyConvolutionFilter(image, matrix, 1, false);
    }

    public static BufferedImage lazyConvolutionFilter(BufferedImage image, float[][] matrix, int scale, boolean normalize){
        DrawingBotV3.logger.entering("ImageTools", "lazyConvolutionFilter: " + scale);
        if(scale != 1){
            matrix = MatrixTools.scaleMatrix(matrix, scale);
        }
        if(normalize){
            matrix = MatrixTools.normalizeMatrix(matrix);
        }
        return new ConvolveOp(MatrixTools.matrixToKernal(matrix), ConvolveOp.EDGE_NO_OP, null).filter(image, null);
    }

    /**a lazy/very fast way to filter an image,*/
    public static BufferedImage lazyRGBFilters(BufferedImage image, Function<Integer, Integer> ...filters){
        DrawingBotV3.logger.entering("ImageTools", "lazyRGBFilters");
        lazyRGBFilter(image, integer -> {
            for(Function<Integer, Integer> filter : filters){
                integer = filter.apply(integer);
            }
            return integer;
        });
        return image;
    }

    /**a lazy/very fast way to filter an image,*/
    public static BufferedImage lazyRGBFilter(BufferedImage image, Function<Integer, Integer> filter){
        DrawingBotV3.logger.entering("ImageTools", "lazyRGBFilter");
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                image.setRGB(x, y, filter.apply(image.getRGB(x, y)));
            }
        }
        return image;
    }

    /**a lazy/very fast way to blend too images which are exactly the same size only*/
    public static BufferedImage lazyBlend(BufferedImage image, BufferedImage overlay, EnumBlendMode blendMode){
        DrawingBotV3.logger.entering("ImageTools", "lazyBlend: " + blendMode);

        // create the new image, canvas size is the max. of both image sizes
        int w = Math.max(image.getWidth(), overlay.getWidth());
        int h = Math.max(image.getHeight(), overlay.getHeight());
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // paint both images, preserving the alpha channels
        Graphics2D g = combined.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.setComposite(new BlendComposite(blendMode));
        g.drawImage(overlay, 0, 0, null);

        g.dispose();

        return combined;
    }

    public static BufferedImage cropToPrintResolution(BufferedImage image, PrintResolution resolution){

        //crop the image in it's original resolution
        if(resolution.imageCropX != 0 || resolution.imageCropY != 0 || resolution.imageCropWidth != resolution.sourceWidth || resolution.imageCropHeight != resolution.sourceHeight){
            switch (DrawingBotV3.INSTANCE.scalingMode.get()){
                case CROP_TO_FIT:
                    image = Scalr.crop(image, resolution.imageCropX, resolution.imageCropY, resolution.imageCropWidth, resolution.imageCropHeight);
                    break;
                case STRETCH_TO_FIT:
                    image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, resolution.imageCropWidth, resolution.imageCropHeight);
                    break;
            }
        }

        //rescale the pre-cropped image to the optimised print sizes
        if(resolution.imageWidth != resolution.imageCropWidth || resolution.imageHeight != resolution.imageCropHeight){
            image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, resolution.imageWidth, resolution.imageHeight);
        }

        return image;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// RGB FILTERS


    public static int grayscaleFilter(int argb){
        int lum = getPerceivedLuminanceFromRGB(argb);
        return (argb & 0xff000000) | lum<<16 | lum<<8 | lum;
    }

    public static int invertFilter(int argb){
        int[] values = getColourIntsFromARGB(argb, new int[4]);
        values[1] = 255 - values[1];
        values[2] = 255 - values[2];
        values[3] = 255 - values[3];

        return getARGB(values[0], values[1], values[2], values[3]);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /// IMAGE CONVERSION

    public static IPixelData newPixelData(int width, int height, int colourMode){
        switch (colourMode){
            case 1:
                return new PixelDataHSB(width, height);
            case 2:
                return new PixelDataLuminance(width, height);
            case 3:
                return new PixelDataARGBY(width, height);
            case 4:
                return new PixelDataHybrid(width, height);
            case 5:
                return new PixelDataBufferedImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
            default:
                return new PixelDataARGB(width, height);
        }
    }

    public static BufferedImage deepCopy(BufferedImage copy) {
        ColorModel cm = copy.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = copy.copyData(copy.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static IPixelData copy(IPixelData source, IPixelData dst){
        for(int x = 0; x < source.getWidth(); x ++){
            for(int y = 0; y < source.getHeight(); y ++){
                dst.setARGB(x, y, source.getARGB(x, y));
            }
        }
        return dst;
    }

    public static BufferedImage getBufferedImage(IPixelData data){
        BufferedImage image = new BufferedImage(data.getWidth(), data.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int x = 0; x < data.getWidth(); x ++){
            for(int y = 0; y < data.getHeight(); y ++){
                image.setRGB(x, y, data.getARGB(x, y));
            }
        }
        return image;
    }

    public static IPixelData copyToPixelData(BufferedImage image, IPixelData data){
        for(int x = 0; x < data.getWidth(); x ++){
            for(int y = 0; y < data.getHeight(); y ++){
                int argb = image.getRGB(x, y);
                int alpha = (argb>>24)&0xff;
                data.setARGB(x, y, alpha == 0 ? data.getTransparentARGB() : argb);
            }
        }
        return data;
    }

    public static BufferedImage drawImage(BufferedImage src, BufferedImage dst) {
        Graphics2D g = dst.createGraphics();
        g.drawRenderedImage( src, null );
        g.dispose();
        return dst;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /// COLOURS

    public static int alpha(int argb){
        return (argb>>24)&0xff;
    }

    public static int red(int argb){
        return (argb>>16)&0xff;
    }

    public static int green(int argb){
        return (argb>>8)&0xff;
    }

    public static int blue(int argb){
        return argb&0xff;
    }

    public static int[] getColourIntsFromARGB(int argb, int[] array){
        array[0] = (argb>>24)&0xff; //alpha
        array[1] = (argb>>16)&0xff;//red
        array[2] = (argb>>8)&0xff; //green
        array[3] = argb&0xff; //blue
        return array;
    }

    public static float[] getRGBFloatFromARGB(int argb, float[] array){
        array[0] = ((argb>>16)&0xff) / 255F; //red
        array[1] = ((argb>>8)&0xff) / 255F; //green
        array[2] = (argb&0xff) / 255F; //blue
        return array;
    }

    public static int getBrightnessFromRGB(int r, int g, int b){
        return Math.max(b, Math.max(r, g));
    }

    public static int getPerceivedLuminanceFromRGB(int argb){
        int[] values = getColourIntsFromARGB(argb, new int[4]);
        return (int)(0.2126*values[1] + 0.7152*values[2] + 0.0722*values[3]);
    }

    public static int getPerceivedLuminanceFromRGB(int r, int g, int b){
        return (int) (0.2126*r + 0.7152*g + 0.0722*b);
    }

    public static float getAverageLuminanceFromRGB(float r, float g, float b){
        return 0.2126F*r + 0.7152F*g + 0.0722F*b;
    }

    /**converts processing colors to java fx colors*/
    public static Color getColorFromARGB(int argb){
        int[] values = getColourIntsFromARGB(argb, new int[4]);
        return new Color(values[1] / 255F, values[2] / 255F, values[3] / 255F, values[0] / 255F);
    }

    public static int getARGBFromColor(Color color){
        return getARGB((int)(color.getOpacity() * 255F), (int)(color.getRed() * 255F), (int)(color.getGreen() * 255F), (int)(color.getBlue() * 255F));
    }

    public static int getARGBFromFloat(float a, float r, float g, float b){
        return getARGB((int)(a * 255F), (int)(r * 255F), (int)(g * 255F), (int)(b * 255F));
    }

    public static int getARGB(int a, int r, int g, int b){
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static String toHex(int argb){
        int r = (argb>>16)&0xff;
        int g = (argb>>8)&0xff;
        int b = argb&0xff;
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public static String toHex(int r, int g, int b){
        return String.format("#%02x%02x%02x", r, g, b);
    }


    //// CMYK

    /**
     * Returns the smaller of three color components.
     *
     * @param red    the red component of the color
     * @param green  the green component of the color
     * @param blue   the blue component of the color
     * @return the smaller of {@code red}, {@code green} and {@code blue}
     */
    public static float min(float red, float green, float blue) {
        return Math.min(Math.min(red, green), blue);
    }

    /**
     * Returns the larger of three color components.
     *
     * @param red    the red component of the color
     * @param green  the green component of the color
     * @param blue   the blue component of the color
     * @return the larger of {@code red}, {@code green} and {@code blue}
     */
    public static float max(float red, float green, float blue) {
        return Math.max(Math.max(red, green), blue);
    }

    /**
     * src: javax.swing.colorchooser.ColorModelCMYK
     * Converts CMYK components of a color to a set of RGB components.
     *
     * @param cmyk  a float array with length equal to
     *              the number of CMYK components
     * @param rgb   a float array with length of at least 3
     *              that contains RGB components of a color
     * @return a float array that contains RGB components
     */
    public static float[] CMYKtoRGB(float[] cmyk, float[] rgb) {
        if (rgb == null) {
            rgb = new float[3];
        }
        rgb[0] = 1.0f + cmyk[0] * cmyk[3] - cmyk[3] - cmyk[0];
        rgb[1] = 1.0f + cmyk[1] * cmyk[3] - cmyk[3] - cmyk[1];
        rgb[2] = 1.0f + cmyk[2] * cmyk[3] - cmyk[3] - cmyk[2];
        return rgb;
    }

    /**
     * src: javax.swing.colorchooser.ColorModelCMYK
     * Converts RGB components of a color to a set of CMYK components.
     *
     * @param rgb   a float array with length of at least 3
     *              that contains RGB components of a color
     * @param cmyk  a float array with length equal to
     *              the number of CMYK components
     * @return a float array that contains CMYK components
     */
    public static float[] RGBtoCMYK(float[] rgb, float[] cmyk) {
        if (cmyk == null) {
            cmyk = new float[4];
        }
        float max = max(rgb[0], rgb[1], rgb[2]);
        if (max > 0.0f) {
            cmyk[0] = 1.0f - rgb[0] / max;
            cmyk[1] = 1.0f - rgb[1] / max;
            cmyk[2] = 1.0f - rgb[2] / max;
        }
        else {
            cmyk[0] = 0.0f;
            cmyk[1] = 0.0f;
            cmyk[2] = 0.0f;
        }
        cmyk[3] = 1.0f - max;
        return cmyk;
    }

    //src http://www.deathbysoftware.com/colors/index.html
    public static float[] RGBtoRYB(float[] rgb, float[] ryb){
        float red = rgb[0], green = rgb[1], blue = rgb[2];

        // Remove the white from the color
        float iWhite = min(red, green, blue);

        red -= iWhite;
        green -= iWhite;
        blue -= iWhite;

        float iMaxGreen = max(red, green, blue);

        // Get the yellow out of the red+green
        float yellow = Math.min(red, green);

        red -= yellow;
        green -= yellow;

        // If this unfortunate conversion combines blue and green, then cut each in half to
        // preserve the value's maximum range.
        if (blue > 0 && green > 0){
            blue  /= 2;
            green /= 2;
        }

        // Redistribute the remaining green.
        yellow += green;
        blue   += green;

        // Normalize to values.
        float iMaxYellow = max(red, yellow, blue);

        if (iMaxYellow > 0){
            float iN = iMaxGreen / iMaxYellow;
            red    *= iN;
            yellow *= iN;
            blue   *= iN;
        }

        // Add the white back in.
        red += iWhite;
        yellow += iWhite;
        blue += iWhite;

        ryb[0] = red;
        ryb[1] = yellow;
        ryb[2] = blue;
        return ryb;
    }

    //src http://www.deathbysoftware.com/colors/index.html
    public static float[] RYBtoRGB(float[] ryb, float[] rgb){
        float red = ryb[0], yellow = ryb[1], blue = ryb[2];

        // Remove the whiteness from the color.
        float iWhite = min(red, yellow, blue);

        red -= iWhite;
        yellow -= iWhite;
        blue -= iWhite;

        float iMaxYellow = max(red, yellow, blue);

        // Get the green out of the yellow and blue
        float green = Math.min(yellow, blue);

        yellow -= green;
        blue -= green;

        if (blue > 0 && green > 0){
            blue *= 2.0;
            green *= 2.0;
        }

        // Redistribute the remaining yellow.
        red += yellow;
        green += yellow;

        // Normalize to values.
        float iMaxGreen = max(red, green, blue);

        if (iMaxGreen > 0){
            float iN = iMaxYellow / iMaxGreen;

            red *= iN;
            green *= iN;
            blue *= iN;
        }

        // Add the white back in.
        red += iWhite;
        green += iWhite;
        blue += iWhite;

        // Save the RGB
        rgb[0] = red;
        rgb[1] = green;
        rgb[2] = blue;

        return rgb;
    }
}
