package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractDarkestPFM extends AbstractPFM {

    protected int sampleWidth = 10;
    protected int sampleHeight = 10;

    protected int darkest_x;
    protected int darkest_y;
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

    /** finds the darkest area of the image, according to sampleWidth/sampleHeight and returns a random pixel in that area */
    protected void findDarkestArea(IPixelData pixels) {
        int totalSamplesX = pixels.getWidth()/sampleWidth;
        int totalSamplesY = pixels.getHeight()/sampleHeight;

        int darkestSampleX = 0;
        int darkestSampleY = 0;

        float darkest_value = 1000;

        for(int sampleX = 0; sampleX < totalSamplesX; sampleX++){
            for(int sampleY = 0; sampleY < totalSamplesY; sampleY++){
                int startX = sampleX*sampleWidth;
                int endX = startX + sampleWidth;

                int startY = sampleY*sampleHeight;
                int endY = startY + sampleHeight;
                float sampleLuminance = 0;
                for(int x = startX; x < endX; x++){
                    for(int y = startY; y < endY; y++){
                        sampleLuminance += pixels.getLuminance(x, y);
                    }
                }
                float avgLuminance = sampleLuminance / (sampleWidth*sampleHeight);
                if (avgLuminance < darkest_value) {
                    darkest_value = avgLuminance;
                    darkestSampleX = startX;
                    darkestSampleY = startY;
                }
            }
        }

        darkest_x = darkestSampleX + randomSeed.nextInt(sampleWidth);
        darkest_y = darkestSampleY + randomSeed.nextInt(sampleHeight);
    }

    /**
     * @param pixels
     * @return a collection of all the darkest pixels
     */
    public List<int[]> findDarkestPixels(IPixelData pixels){
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
    public void findDarkestPixel(IPixelData pixels){
        List<int[]> darkestPixels = findDarkestPixels(pixels);
        int[] point = darkestPixels.get(randomSeed.nextInt(darkestPixels.size()));
        darkest_x = point[0];
        darkest_y = point[1];
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

    ///luminance calculations
    protected int sum_luminance = 0;
    protected int count_pixels = 0;
    protected float test_luminance = 0;

    protected int sum_red = 0;
    protected int sum_green = 0;
    protected int sum_blue = 0;
    protected int sum_alpha = 0;
    protected int total_pixels = 0;

    protected void resetLuminanceTest(){
        test_luminance = -1;
    }

    protected void resetLuminanceSamples(){
        sum_luminance = 0;
        count_pixels = 0;
    }

    protected float getLuminanceTestAverage(){
        return (float) sum_luminance / (float)count_pixels;
    }

    public boolean testAndSetLuminanceTest(){
        float average = getLuminanceTestAverage();
        boolean test = test_luminance == -1 || average < test_luminance;
        test_luminance = average;
        return test;
    }

    protected int clampX(int x, int width){
        return Utils.clamp(x, 0, width-1);
    }

    protected int clampY(int y, int height){
        return Utils.clamp(y, 0, height-1);
    }

    /**
     * luminance test for angled lines
     */
    protected void luminanceTestAngledLine(IPixelData pixels, int originX, int originY, float distance, float degree){
        int x1 = (int)(Math.cos(Math.toRadians(degree))*distance) + originX;
        int y1 = (int)(Math.sin(Math.toRadians(degree))*distance) + originY;
        luminanceTestLine(pixels, originX, originY, x1, y1);
    }

    /**
     * luminance test for straight lines
     */
    protected void luminanceTestLine(IPixelData pixels, int x0, int y0, int x1, int y1) {
        resetLuminanceSamples();

        //x0 = clampX(x0, pixels.getWidth());
        //y0 = clampY(y0, pixels.getHeight());

        bresenham.plotLine(x0, y0, x1, y1, (x, y) -> luminanceTest(pixels, x, y));
    }

    /**
     * luminance test for individual pixels and STOPS prematurely
     */
    protected boolean luminanceTest(IPixelData pixels, int x, int y){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            //if we're off the image we'll take the last result and stop there
            if(test_luminance == -1 && count_pixels > minLineLength()){
                onLuminanceTestSuccess(pixels, x, y);
            }
            return true;
        }
        sum_luminance += pixels.getLuminance(x, y);
        count_pixels++;
        if ((test_luminance == -1 || getLuminanceTestAverage() < test_luminance) && count_pixels > minLineLength()) {
            onLuminanceTestSuccess(pixels, x, y);
        }
        return count_pixels > maxLineLength();
    }

    public void onLuminanceTestSuccess(IPixelData pixels, int x, int y){
        darkest_x = x;
        darkest_y = y;
        test_luminance = getLuminanceTestAverage();
    }

    protected void luminanceTally(IPixelData pixels, int x, int y){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return;
        }
        sum_luminance += pixels.getLuminance(x, y);
        count_pixels++;
    }


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

    public void resetColourSamples(){
        sum_red = 0;
        sum_green = 0;
        sum_blue = 0;
        sum_alpha = 0;
        total_pixels = 0;
    }

    public int getColourTestAverage(){
        if(total_pixels == 0){
            return 0;
        }
        return ImageTools.getARGB(sum_alpha / total_pixels, sum_red /total_pixels, sum_green / total_pixels, sum_blue / total_pixels);
    }

    public int adjustLuminanceLine(IPlottingTask task, IPixelData pixels, int x0, int y0, int x1, int y1, int adjustLum) {
        resetColourSamples();
        bresenham.plotLine(x0, y0, x1, y1, (x, y) -> adjustLuminanceColour(pixels, x, y, adjustLum));
        return getColourTestAverage();
    }

    public boolean adjustLuminanceColour(IPixelData pixels, int x, int y, int adjustLum){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return false;
        }
        total_pixels++;
        sum_alpha += pixels.getAlpha(x, y);
        sum_red += pixels.getRed(x, y);
        sum_green += pixels.getGreen(x, y);
        sum_blue += pixels.getBlue(x, y);
        pixels.adjustRed(x, y, adjustLum);
        pixels.adjustGreen(x, y, adjustLum);
        pixels.adjustBlue(x, y, adjustLum);
        return false;
    }

}
