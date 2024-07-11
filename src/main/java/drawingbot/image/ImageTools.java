package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.api.IPixelData;
import drawingbot.api.IProgressCallback;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.format.ImageCropping;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.UnitsLength;
import javafx.scene.paint.Color;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class ImageTools {

    public static BufferedImage applyCurrentImageFilters(BufferedImage image, ImageFilterSettings settings, boolean forceUpdate, IProgressCallback callback){
        int filterCount = 0;
        for(ObservableImageFilter filter : settings.currentFilters.get()){
            if(filter.enable.get()){
                if(forceUpdate || filter.dirty.get()){
                    BufferedImageOp imageOp = filter.filterFactory.instance();
                    filter.filterSettings.forEach(setting -> setting.applySetting(imageOp));
                    image = imageOp.filter(image, null);
                    filter.dirty.set(false);
                    filter.cached.set(image);
                    forceUpdate = true; //one of the filters has changed, so all the ones after this need to be updated to
                }else{
                    image = filter.cached.get();
                }
            }else if(filter.dirty.get()){
                //the filter has just been disabled, so update downstream filters
                forceUpdate = true;
                filter.dirty.set(false);
            }
            filterCount++;
            callback.updateProgress(filterCount, settings.currentFilters.get().size());
        }
        callback.updateProgress(1, 1);
        return image;
    }

    public static int getArrayIndex(int x, int y, int width){
        return y*width + x;
    }

    public static int getIndexX(int index, int width){
        return index % width;
    }

    public static int getIndexY(int index, int width){
        return index / width;
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
        g.setComposite(blendMode);
        g.drawImage(overlay, 0, 0, null);


        return combined;
    }

    public static BufferedImage lazyBackground(BufferedImage image, java.awt.Color color){
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setBackground(color);
        graphics2D.clearRect(0, 0, image.getWidth(), image.getHeight());
        graphics2D.dispose();
        return image;
    }

    public static BufferedImage lazyBackgroundWithForeground(BufferedImage foreground, java.awt.Color color){
        BufferedImage freshImage = new BufferedImage(foreground.getWidth(), foreground.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = freshImage.createGraphics();
        graphics2D.setBackground(color);
        graphics2D.clearRect(0, 0, freshImage.getWidth(), freshImage.getHeight());
        graphics2D.drawImage(foreground, null, 0, 0);
        graphics2D.dispose();
        return freshImage;
    }

    public static void lazyPNGExport(BufferedImage image, File file){
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * The pre-crop refers to crops made to the image before it is filtered / used.
     * @param image the source image to be cropped
     * @param crop the crop rectangle representing the size of the new image in pixels.
     * @return the cropped image, matching the size of the given crop rectangle.
     */
    public static BufferedImage applyPreCrop(BufferedImage image, Rectangle2D crop){
        if(crop.getX() == 0 && crop.getY() == 0 && crop.getWidth() == image.getWidth() && crop.getHeight() == image.getHeight()){
            return image;
        }
        BufferedImage bufferedImage = new BufferedImage((int)crop.getWidth(), (int)crop.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        //graphics.transform(transform);
        graphics.drawImage(image, null, (int) -crop.getX(),(int) -crop.getY());
        graphics.dispose();
        return bufferedImage;
    }

    public static AffineTransform getCanvasRotationTransform(ICanvas canvas, ImageCropping cropping){
        return getCanvasRotationTransform(canvas, cropping.getImageRotation(), cropping.shouldFlipHorizontal(), cropping.shouldFlipVertical());
    }

    @Deprecated
    public static AffineTransform getCanvasRotationTransform(ICanvas canvas, EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical){
        AffineTransform tx = new AffineTransform();

        if(flipHorizontal){
            tx.translate(canvas.getScaledWidth(), 0);
            tx.scale(-1.0, 1.0);
        }

        if(flipVertical){
            tx.translate(0, canvas.getScaledHeight());
            tx.scale(1.0, -1.0);
        }

        switch (imageRotation){
            case R90 -> {
                tx.translate(canvas.getScaledHeight(), 0);
                tx.rotate(Math.toRadians(90));
            }
            case R180 -> {
                tx.translate(canvas.getScaledWidth(), canvas.getScaledHeight());
                tx.rotate(Math.toRadians(180));
            }
            case R270 -> {
                tx.translate(0, canvas.getScaledWidth());
                tx.rotate(Math.toRadians(-90));
            }
        }
        return tx;
    }

    public static AffineTransform getCanvasScaleTransform(ICanvas sourceCanvas, ICanvas destCanvas){

        AffineTransform tx = new AffineTransform();
        if(destCanvas.useOriginalSizing()){
            return tx;
        }

        final EnumCroppingMode mode = destCanvas.getCroppingMode();
        switch (mode) {
            case CROP_TO_FIT -> {
                int[] crops = CanvasUtils.getCroppedImageSize(destCanvas, (int)sourceCanvas.getScaledWidth(), (int)sourceCanvas.getScaledHeight());
                int width = crops[0];
                int height = crops[1];
                int cropX = crops[2];
                int cropY = crops[3];
                tx.scale(destCanvas.getScaledDrawingWidth() / width, destCanvas.getScaledDrawingHeight() / height);
                tx.translate(-cropX, -cropY);
            }
            case SCALE_TO_FIT -> {
                double widthScale = destCanvas.getScaledDrawingWidth() / sourceCanvas.getScaledDrawingWidth();
                double heightScale = destCanvas.getScaledDrawingHeight() / sourceCanvas.getScaledDrawingHeight();
                double rescale = Math.min(widthScale, heightScale);

                tx.scale(rescale, rescale);
            }
            case STRETCH_TO_FIT -> {
                tx.scale(destCanvas.getScaledDrawingWidth() / sourceCanvas.getScaledDrawingWidth(), destCanvas.getScaledDrawingHeight() / sourceCanvas.getScaledDrawingHeight());
            }
        }
        return tx;
    }

    public static BufferedImage rotateImage(BufferedImage image, EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical){
        if(flipHorizontal){
            image = Scalr.rotate(image, Scalr.Rotation.FLIP_HORZ);
        }

        if(flipVertical){
            image = Scalr.rotate(image, Scalr.Rotation.FLIP_VERT);
        }

        if(imageRotation.scalrRotation != null){
            image = Scalr.rotate(image, imageRotation.scalrRotation);
        }
        return image;
    }

    public static int[] getEffectiveImageSize(ICanvas canvas, int width, int height){
        int[] size = new int[]{width, height};
        switch (canvas.getCroppingMode()){
            case CROP_TO_FIT:
                int[] crops = CanvasUtils.getCroppedImageSize(canvas, width, height);
                size[0] = crops[0];
                size[1] = crops[1];
                break;
            case SCALE_TO_FIT:
                break;
            case STRETCH_TO_FIT:
                break;
        }
        return size;
    }

    public static BufferedImage cropToCanvas(BufferedImage image, ICanvas canvas){

        if(canvas.useOriginalSizing()){
            return image;
        }

        int finalWidth = (int)(canvas.getDrawingWidth(UnitsLength.PIXELS) * canvas.getPlottingScale());
        int finalHeight = (int)(canvas.getDrawingHeight(UnitsLength.PIXELS) * canvas.getPlottingScale());

        //crop the image in it's original resolution
        final EnumCroppingMode mode = canvas.getCroppingMode();
        switch (mode){
            case CROP_TO_FIT:
                int[] crops = CanvasUtils.getCroppedImageSize(canvas, image.getWidth(), image.getHeight());
                int width = crops[0];
                int height = crops[1];
                int cropX = crops[2];
                int cropY = crops[3];

                if(cropX != 0 || cropY != 0 || width != image.getWidth() || height != image.getHeight()) {
                    image = Scalr.crop(image, cropX, cropY, width, height);
                }
                //rescale the pre-cropped image to the optimised print sizes
                if(finalWidth != width || finalHeight != height){
                    image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, finalWidth, finalHeight);
                }
                break;
            case SCALE_TO_FIT:
                double currentRatio = (float) image.getWidth() / image.getHeight();
                double targetRatio = canvas.getDrawingWidth() / canvas.getDrawingHeight();
                int targetWidth = (int)(currentRatio < targetRatio ? Math.round(finalHeight * currentRatio) : finalWidth);
                int targetHeight = (int)(currentRatio < targetRatio ? finalHeight : Math.round(finalWidth / currentRatio));

                image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, targetWidth, targetHeight);
                break;
            case STRETCH_TO_FIT:
                image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, finalWidth, finalHeight);
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

    public static boolean lazyCompare(IPixelData dataA, IPixelData dataB){
        if(dataA == null && dataB == null){
            return true;
        }
        if(dataA == null || dataB == null){
            return false;
        }
        if(dataA.getWidth() != dataB.getWidth() || dataA.getHeight() != dataB.getHeight()){
            return false;
        }
        for(int x = 0; x < dataA.getWidth(); x++){
            for(int y = 0; y < dataA.getHeight(); y++){
                if(dataA.getARGB(x, y) != dataB.getARGB(x, y)){
                    return false;
                }
            }
        }
        return true;
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

    public static int[][] createARGBData(BufferedImage image){
        int[][] data = new int[image.getWidth()][image.getHeight()];
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                data[x][y] = image.getRGB(x, y);
            }
        }
        return data;
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

    public static float[] getARGBFloatFromARGB(int argb, float[] array){
        array[0] = ((argb>>24)&0xff) / 255F; //alpha
        array[1] = ((argb>>16)&0xff) / 255F; //red
        array[2] = ((argb>>8)&0xff) / 255F; //green
        array[3] = (argb&0xff) / 255F; //blue
        return array;
    }

    public static int getBrightnessFromRGB(int argb){
        return Math.max(red(argb), Math.max(green(argb), blue(argb)));
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
        if(color == null){
            return -1;
        }
        return getARGB((int)(color.getOpacity() * 255F), (int)(color.getRed() * 255F), (int)(color.getGreen() * 255F), (int)(color.getBlue() * 255F));
    }
    public static int getARGBFromAWTColor(java.awt.Color color){
        if(color == null){
            return -1;
        }
        return getARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int getARGBFromFloat(float a, float r, float g, float b){
        return getARGB((int)(a * 255F), (int)(r * 255F), (int)(g * 255F), (int)(b * 255F));
    }

    public static int getARGB(int a, int r, int g, int b){
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static java.awt.Color getAWTFromFXColor(Color color){
        return new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
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

}
