package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.ImageTools;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.pfm.helpers.ColourSampleTest;
import drawingbot.pfm.helpers.LuminanceTestLine;
import drawingbot.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractDarkestPFM extends AbstractPFM {

    protected static int sampleWidth = 10;
    protected static int sampleHeight = 10;

    public BresenhamHelper bresenham = new BresenhamHelper();

    @Override
    public int getColourMode() {
        return 3;
    }

    @Override
    public int getTransparentARGB() {
        return ImageTools.getARGB(0, 255, 255, 255);
    }

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * finds the darkest area of the image, according to sampleWidth/sampleHeight and returns the first darkest pixel in that area
     * @param pixels the pixel data to search
     * @param dest must have length >= 2 will set the following values 0 = Darkest Pixel X, 1 = Darkest Pixel Y, 2 = Darkest Pixel Luminance, 3 = Darkest Sample Luminance
     */
    public static void findDarkestArea(IPixelData pixels, int[] dest) {
        int totalSamplesX = pixels.getWidth()/sampleWidth;
        int totalSamplesY = pixels.getHeight()/sampleHeight;

        float darkestSampleLuminance = 1000;

        int finalDarkestPixelX = 0;
        int finalDarkestPixelY = 0;
        int finalDarkestPixelLuminance = 0;

        for(int sampleX = 0; sampleX < totalSamplesX; sampleX++){
            for(int sampleY = 0; sampleY < totalSamplesY; sampleY++){
                int startX = sampleX*sampleWidth;
                int endX = startX + sampleWidth;

                int startY = sampleY*sampleHeight;
                int endY = startY + sampleHeight;
                float sampleLuminance = 0;

                int darkestPixelX = 0;
                int darkestPixelY = 0;
                int darkestPixel = 1000;

                for(int x = startX; x < endX; x++){
                    for(int y = startY; y < endY; y++){

                        int luminance = pixels.getLuminance(x, y);
                        sampleLuminance += luminance;
                        if(luminance < darkestPixel){
                            darkestPixelX = x;
                            darkestPixelY = y;
                            darkestPixel = luminance;
                        }
                    }
                }
                float avgLuminance = sampleLuminance / (sampleWidth*sampleHeight);
                if (avgLuminance < darkestSampleLuminance) {
                    darkestSampleLuminance = avgLuminance;
                    finalDarkestPixelX = darkestPixelX;
                    finalDarkestPixelY = darkestPixelY;
                    finalDarkestPixelLuminance = darkestPixel;
                }
            }
        }

        dest[0] = finalDarkestPixelX;
        dest[1] = finalDarkestPixelY;

        if(dest.length >= 3){
            dest[2] = finalDarkestPixelLuminance;
        }
        if(dest.length >= 4){
            dest[3] = (int) darkestSampleLuminance;
        }
    }

    /**
     * @param pixels
     * @return a collection of all the darkest pixels
     */
    public static List<int[]> findDarkestPixels(IPixelData pixels){
        List<int[]> points = new ArrayList<>();
        int luminance = pixels.getLuminance(0,0);


        for(int x = 0; x < pixels.getWidth(); x ++){
            for(int y = 0; y < pixels.getHeight(); y ++){
                int c = pixels.getLuminance(x, y);
                if(c == luminance) {
                    points.add(new int[]{x, y});
                }
                if(c < luminance) {
                    points.clear();
                    points.add(new int[]{x, y});
                    luminance = c;
                }
            }
        }
        return points;
    }

    /** returns a random pixel of the darkest pixels found*/
    public void findDarkestPixel(IPixelData pixels, int[] dest){
        List<int[]> darkestPixels = findDarkestPixels(pixels);
        int[] darkestPoint = darkestPixels.get(randomSeed.nextInt(darkestPixels.size()));
        dest[0] = darkestPoint[0];
        dest[1] = darkestPoint[1];
    }


    /**
     * Finds the darkest line from the given start point, choosing between a naive approach or bresenham circle tests for faster results
     */
    public float findDarkestLine(IPixelData pixels, int startX, int startY, int minLength, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, int[] darkestDst){
        LuminanceTestLine luminanceTest = new LuminanceTestLine(darkestDst, minLength, maxLength, true);
        runDarkestTest(pixels, startX, startY, maxLength, maxTests, startAngle, drawingDeltaAngle, shading, false, (x, y) -> {
            luminanceTest.resetSamples();
            bresenham.plotLine(startX, startY, x, y, (xT, yT) -> luminanceTest.addSample(pixels, xT, yT));
        });
        return luminanceTest.getDarkestSample();
    }


    /**
     * Returns the end coordinates of each line test
     */
    public void runDarkestTest(IPixelData pixels, int startX, int startY, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, boolean safe, BiConsumer<Integer, Integer> consumer){
        if(drawingDeltaAngle == 360 && !shading && (maxTests == -1 || bresenham.getBresenhamCircleSize(maxLength) <= maxTests)){
            bresenham.plotCircle(startX, startY, maxLength, (x1, y1) -> processSafePixels(pixels, startX, startY, x1, y1, safe, consumer));
        }else{
            float deltaAngle = shading ? drawingDeltaAngle : drawingDeltaAngle / (float) maxTests;
            for (int d = 0; d < (shading ? 2 : maxTests); d ++) {
                int x1 = (int)Math.ceil((Math.cos(Math.toRadians((deltaAngle * d) + startAngle))*maxLength) + startX);
                int y1 = (int)Math.ceil((Math.sin(Math.toRadians((deltaAngle * d) + startAngle))*maxLength) + startY);
                processSafePixels(pixels, startX, startY, x1, y1, safe, consumer);
            }
        }
    }

    /**
     * Takes the consumer of a darkness test and the next pixel position, and checks it is "safe" (within the bounds of the image)
     * If the pixels are "unsafe" and the safe boolean is enabled, the first edge pixel from the line is returned.
     * Safe should be disabled for tests which check if the pixels are within the bounds of the image themselves.
     */
    public void processSafePixels(IPixelData pixels, int startX, int startY, int endX, int endY, boolean safe, BiConsumer<Integer, Integer> consumer){
        if(!safe || (Utils.within(endX, 0, pixels.getWidth()) && Utils.within(endY, 0, pixels.getHeight()))){
            consumer.accept(endX, endY);
        }else{
            int[] edgePixel = bresenham.findEdge(startX, startY, endX, endY, pixels.getWidth(), pixels.getHeight());
            consumer.accept(edgePixel[0], edgePixel[1]);
        }
    }

    public double getAngle(int startX, int startY, int targetX, int targetY) {
        double angle = Math.toDegrees(Math.atan2(targetY - startY, targetX - startX));
        angle %= 360;
        if(angle < 0){
            angle += 360;
        }
        return angle;
    }

    /** returns a line which intersects through the entire image going through the specified point */
    public int[] getIntersectingLine(IPixelData pixels, int pointX, int pointY, float degree){
        double slope = Math.tan(degree);
        int[] left = getLeftIntersection(pixels, pointX, pointY, slope);
        int[] right = getRightIntersection(pixels, pointX, pointY, slope);
        return new int[]{left[0], left[1], right[0], right[1]};
    }

    private final int[] leftPointCache = new int[2];

    /** finds the point of intersection with the image to the left of the given point */
    public int[] getLeftIntersection(IPixelData pixels, int originX, int originY, double slope){
        double maxHeight = pixels.getHeight()-1;
        double leftYIntercept = originY - slope*originX;
        if(leftYIntercept >= maxHeight){
            leftPointCache[0] = (int)((maxHeight-leftYIntercept)/slope);
            leftPointCache[1] = (int)maxHeight;
        }else if(leftYIntercept < 0){
            leftPointCache[0] = (int)((-leftYIntercept)/slope);
            leftPointCache[1] = 0;
        }else{
            leftPointCache[0] = 0;
            leftPointCache[1] = (int)leftYIntercept;
        }
        return leftPointCache;
    }

    private final int[] rightPointCache = new int[2];

    /** finds the point of intersection with the image to the right of the given point */
    public int[] getRightIntersection(IPixelData pixels, int originX, int originY, double slope){
        double maxWidth = pixels.getWidth()-1;
        double maxHeight = pixels.getHeight()-1;

        double rightYIntercept = originY - slope*(originX-maxWidth);
        if(rightYIntercept >= maxHeight){
            rightPointCache[0] = (int)(((maxHeight-rightYIntercept)/slope)  + maxWidth);
            rightPointCache[1] = (int)maxHeight-1;
        }else if(rightYIntercept < 0){
            rightPointCache[0] = (int)(((-rightYIntercept)/slope) + maxWidth);
            rightPointCache[1] = 0;
        }else{
            rightPointCache[0] = (int)maxWidth;
            rightPointCache[1] = (int)rightYIntercept;
        }
        return rightPointCache;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Overriding this value will force lines to reach the minimum length provided
     * Doing this will result in less accurate plots for lines but is generally better for curve PFMS
     * TODO MAKE THIS AN OPTION ON ALL PFMS???
     * @return
     */
    public int minLineLength(){
        return 0;
    }

    public int maxLineLength(){
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public ColourSampleTest defaultColourTest = new ColourSampleTest();

    public void addGeometryWithColourSamples(IPlottingTask task, IPixelData pixelData, IGeometry geometry, int adjust){
        int colourSamples = adjustGeometryLuminance(pixelData, geometry, adjust);
        task.addGeometry(geometry, -1, colourSamples);
    }

    public int adjustGeometryLuminance(IPixelData pixelData, IGeometry geometry, int adjust){
        defaultColourTest.resetColourSamples(adjust);
        geometry.renderBresenham(bresenham, (x,y) -> defaultColourTest.addSample(pixelData, x, y));
        return defaultColourTest.getCurrentAverage();
    }

}
