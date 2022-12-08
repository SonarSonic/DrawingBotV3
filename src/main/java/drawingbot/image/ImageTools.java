package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.api.IProgressCallback;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.kernels.IKernelFactory;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.api.ICanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.UnitsLength;
import javafx.scene.paint.Color;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.function.Function;

public class ImageTools {

    public static BufferedImage applyCurrentImageFilters(BufferedImage image, ImageFilterSettings settings, boolean forceUpdate, IProgressCallback callback){
        int filterCount = 0;
        for(ObservableImageFilter filter : settings.currentFilters.get()){
            if(filter.enable.get()){
                if(forceUpdate || filter.dirty.get()){
                    BufferedImageOp imageOp = filter.filterFactory.instance();
                    filter.filterSettings.forEach(setting -> setting.applySetting(imageOp));

                    IKernelFactory kernelFactory = MasterRegistry.INSTANCE.getImageFilterKernel(imageOp);
                    if(kernelFactory != null){
                        BufferedImage dstImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        image = kernelFactory.doProcess(imageOp, image, dstImage);
                    }else{
                        image = imageOp.filter(image, null);
                    }
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
            if(callback != null){
                callback.updateProgress(filterCount, settings.currentFilters.get().size());
            }
        }
        if(callback != null) {
            callback.updateProgress(1, 1);
        }
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
        g.setComposite(new BlendComposite(blendMode));
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

    public static BufferedImage applyPreCrop(BufferedImage image, Rectangle2D crop){
        BufferedImage bufferedImage = new BufferedImage((int)crop.getWidth(), (int)crop.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        //graphics.transform(transform);
        graphics.drawImage(image, null, (int) -crop.getX(),(int) -crop.getY());
        graphics.dispose();
        return bufferedImage;
    }

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
                if(destCanvas.getRescaleMode().shouldRescale()) {
                    tx.scale(destCanvas.getScaledDrawingWidth() / width, destCanvas.getScaledDrawingHeight() / height);
                }
                tx.translate(-cropX, -cropY);
            }
            case SCALE_TO_FIT -> {
                if(destCanvas.getRescaleMode().shouldRescale()) {
                    double widthScale = destCanvas.getScaledDrawingWidth() / sourceCanvas.getScaledDrawingWidth();
                    double heightScale = destCanvas.getScaledDrawingHeight() / sourceCanvas.getScaledDrawingHeight();
                    double rescale = Math.min(widthScale, heightScale);

                    tx.scale(rescale, rescale);

                }
            }
            case STRETCH_TO_FIT -> {
                if(destCanvas.getRescaleMode().shouldRescale()) {
                    tx.scale(destCanvas.getScaledDrawingWidth() / sourceCanvas.getScaledDrawingWidth(), destCanvas.getScaledDrawingHeight() / sourceCanvas.getScaledDrawingHeight());
                }else{
                    double currentRatio = sourceCanvas.getScaledWidth() / sourceCanvas.getScaledHeight();
                    double targetRatio = destCanvas.getDrawingWidth() / destCanvas.getDrawingHeight();

                    double scaleX = 1;
                    double scaleY = 1;

                    if(targetRatio < currentRatio){
                        scaleY = currentRatio / targetRatio;
                    }else{
                        scaleX = targetRatio / currentRatio;
                    }
                    tx.scale(scaleX, scaleY);
                }
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

    @Deprecated
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
                return new PixelDataBufferedImage(width, height);
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
