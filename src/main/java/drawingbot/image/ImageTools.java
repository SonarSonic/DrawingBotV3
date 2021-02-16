package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import javafx.scene.paint.Color;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.*;
import java.util.function.Function;

public class ImageTools {

    //// MATRIX OPERATIONS

    public static Kernel matrixToKernal(float[][] matrix){
        int height = matrix.length;
        int width = matrix[0].length;
        float[] kernalMatrix = new float[height*width];

        int pos = 0;
        for(float[] row : matrix){
            for(float value : row){
                kernalMatrix[pos] = value;
                pos++;
            }
        }

        return new Kernel(width, height, kernalMatrix);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Source:  https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm
     * Test:    http://www.calcul.com/show/calculator/matrix-multiplication_;2;3;3;5
     * @param matrixA
     * @param matrixB
     * @return
     */
    public static float [][] multiplyMatrix(float[][] matrixA, float[][] matrixB) {

        int n = matrixA.length;      // matrixA rows
        int m = matrixA[0].length;   // matrixA columns
        int p = matrixB[0].length;

        float[][] matrixC;
        matrixC = new float[n][p];

        for (int i=0; i<n; i++) {
            for (int j=0; j<p; j++) {
                for (int k=0; k<m; k++) {
                    matrixC[i][j] = matrixC[i][j] + matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return matrixC;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Source:  https://www.taylorpetrick.com/blog/post/convolution-part2
     * Useful for keeping brightness the same.
     * Do not use on a maxtix that sums to zero, such as sobel.
     * @param matrix
     * @return The resulting matrix is the same size as the original, but the output range will be constrained between 0.0 and 1.0
     */
    public static float [][] normalizeMatrix(float[][] matrix) {
        int n = matrix.length;      // rows
        int m = matrix[0].length;   // columns
        float sum = 0;

        for (int i=0; i<n; i++) {
            for (int j=0; j<m; j++) {
                sum += matrix[i][j];
            }
        }

        for (int i=0; i<n; i++) {
            for (int j=0; j<m; j++) {
                matrix[i][j] = matrix[i][j] / Math.abs(sum);
            }
        }

        return matrix;
    }

    public static float [][] scaleMatrix(float[][] matrix, int scale) {
        int n = matrix.length;      // rows
        int p = matrix[0].length;   // columns

        float [][] nmatrix = new float[n*scale][p*scale];

        for (int i=0; i<n; i++){
            for (int j=0; j<p; j++){
                for (int si=0; si<scale; si++){
                    for (int sj=0; sj<scale; sj++){
                        int a1 = (i*scale)+si;
                        int a2 = (j*scale)+sj;
                        float a3 = matrix[i][j];
                        nmatrix[a1][a2] = a3;
                    }
                }
            }
        }
        return nmatrix;
    }

    public static void printMatrix(float[][] matrix) {
        int n = matrix.length;      // rows
        int p = matrix[0].length;   // columns
        float sum = 0;

        for (int i=0; i<n; i++){
            for (int j=0; j<p; j++){
                sum += matrix[i][j];
                DrawingBotV3.logger.fine("%10.5f " + matrix[i][j]);
            }
        }
        DrawingBotV3.logger.fine("Sum: " + sum);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// BUFFERED IMAGE FILTERS

    /**A quick and dirty way of softening the edges of your drawing.
     * Look in the boarders directory for some examples.
     * Ideally, the boarder will have similar dimensions as the image to be drawn.
     * For far more control, just edit your input image directly.
     * Most of the examples are pretty heavy handed so you can "shrink" them a few pixels as desired.
     * It does not matter if you use a transparant background or just white.  JPEG or PNG, it's all good.
     * @param dst destination buffered image
     * @param fname Name of boarder file.
     * @param shrink Number of pixels to pull the boarder away, 0 for no change.
     * @param blur Guassian blur the boarder, 0 for no blur, 10+ for a lot.
     */
    public static BufferedImage lazyImageBorder(BufferedImage dst, String fname, boolean internal, int shrink, int blur) {
        DrawingBotV3.logger.entering("ImageTools", "lazyImageBorder: " + fname);

        BufferedImage borderImg = BufferedImageLoader.loadImage(fname, internal);
        if(borderImg != null){
            borderImg = Scalr.resize(borderImg, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, dst.getWidth(), dst.getHeight());

            borderImg = ImageTools.lazyRGBFilter(borderImg, ImageTools::invertFilter);
            dst = ImageTools.lazyBlend(dst, borderImg, EnumBlendMode.ADD);
        }
        return dst;
    }

    public static BufferedImage lazyConvolutionFilter(BufferedImage image, float[][] matrix){
        return lazyConvolutionFilter(image, matrix, 1, false);
    }

    public static BufferedImage lazyConvolutionFilter(BufferedImage image, float[][] matrix, int scale, boolean normalize){
        DrawingBotV3.logger.entering("ImageTools", "lazyConvolutionFilter: " + scale);
        if(scale != 1){
            matrix = ImageTools.scaleMatrix(matrix, scale);
        }
        if(normalize){
            matrix = ImageTools.normalizeMatrix(matrix);
        }
        return new ConvolveOp(ImageTools.matrixToKernal(matrix), ConvolveOp.EDGE_NO_OP, null).filter(image, null);
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

    public static BufferedImage cropToAspectRatio(BufferedImage image, float targetRatio){
        DrawingBotV3.logger.entering("ImageTools", "cropToAspectRatio");
        float currentRatio = (float)image.getWidth() / image.getHeight();
        if(targetRatio == currentRatio){
            return image;
        }
        if (currentRatio < targetRatio) {
            int desired_x = image.getWidth();
            int desired_y = (int)(image.getWidth() / targetRatio);

            int half_y = (image.getHeight() - desired_y) / 2;
            image = Scalr.crop(image, 0, half_y, desired_x, desired_y);
        } else {
            int desired_x = (int)(image.getHeight() * targetRatio);
            int desired_y = image.getHeight();

            int half_x = (image.getWidth() - desired_x) / 2;
            image = Scalr.crop(image, half_x, 0, desired_x, desired_y);
        }
        return image;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// RGB FILTERS

    public static int grayscaleFilter(int argb){
        int lum = (77*(argb>>16&0xff) + 151*(argb>>8&0xff) + 28*(argb&0xff))>>8;
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
                return new PixelDataGray(width, height);
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

    /*

    @Deprecated
    public static PImage getPImage(IPixelData data){
        PImage image = new PImage(data.getWidth(), data.getHeight());
        image.loadPixels();
        for(int x = 0; x < data.getWidth(); x ++){
            for(int y = 0; y < data.getHeight(); y ++){
                image.set(x, y, data.getARGB(x, y));
            }
        }
        image.updatePixels();
        return image;
    }

    @Deprecated
    public static IPixelData copyToPixelData(PImage image, IPixelData data){
        for(int x = 0; x < data.getWidth(); x ++){
            for(int y = 0; y < data.getHeight(); y ++){
                data.setARGB(x, y, image.get(x, y));
            }
        }
        return data;
    }

    @Deprecated
    public static WritableImage getWritableImageFromPImage(PImage pImage){
        WritableImage writableImage = new WritableImage(pImage.pixelWidth, pImage.pixelHeight);
        pImage.loadPixels();
        writableImage.getPixelWriter().setPixels(0, 0, pImage.pixelWidth, pImage.pixelHeight, PixelFormat.getIntArgbInstance(), pImage.pixels, 0, 0);
        return writableImage;
    }

     */

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /// COLOURS

    public static int[] getColourIntsFromARGB(int argb, int[] array){
        array[0] = (argb>>24)&0xff; //alpha
        array[1] = (argb>>16)&0xff; //red
        array[2] = (argb>>8)&0xff; //green
        array[3] = argb&0xff; //blue
        return array;
    }

    public static int getBrightness(int argb){
        int[] values = getColourIntsFromARGB(argb, new int[4]);
        return (int)(0.2126*values[1] + 0.7152*values[2] + 0.0722*values[3]);
    }

    public static int getBrightness(int[] rgb){
        return (int)(0.2126*rgb[0] + 0.7152*rgb[0] + 0.0722*rgb[0]);
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
