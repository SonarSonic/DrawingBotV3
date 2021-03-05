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
        if(!resolution.hasCropping()){
            return image;
        }
        switch (DrawingBotV3.INSTANCE.scalingMode.get()){
            case CROP_TO_FIT:
                image = Scalr.crop(image, resolution.imageCropX, resolution.imageCropY, resolution.imageWidth, resolution.imageHeight);
                break;
            case SCALE_TO_FIT:
                break;
            case STRETCH_TO_FIT:
                image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, resolution.imageWidth, resolution.imageHeight);
                break;
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

    public static int[] getColourIntsFromARGB(int argb, int[] array){
        array[0] = (argb>>24)&0xff; //alpha
        array[1] = (argb>>16)&0xff; //red
        array[2] = (argb>>8)&0xff; //green
        array[3] = argb&0xff; //blue
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
}
