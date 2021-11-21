package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.pfm.helpers.ColourSampleTest;
import drawingbot.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDarkestPFM extends AbstractPFM {

    protected int sampleWidth = 10;
    protected int sampleHeight = 10;

    //protected int darkest_x;
    //protected int darkest_y;
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

    /** finds the darkest area of the image, according to sampleWidth/sampleHeight and returns the darkest pixel in that area */
    protected void findDarkestArea(IPixelData pixels, int[] dest) {
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

        dest[0] = darkestSampleX + randomSeed.nextInt(sampleWidth);
        dest[1] = darkestSampleY + randomSeed.nextInt(sampleHeight);
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
    public void findDarkestPixel(IPixelData pixels, int[] dest){
        List<int[]> darkestPixels = findDarkestPixels(pixels);
        int[] darkestPoint = darkestPixels.get(randomSeed.nextInt(darkestPixels.size()));
        dest[0] = darkestPoint[0];
        dest[1] = darkestPoint[1];
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


    protected int clampX(int x, int width){
        return Utils.clamp(x, 0, width-1);
    }

    protected int clampY(int y, int height){
        return Utils.clamp(y, 0, height-1);
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

    protected ColourSampleTest defaultColourTest = new ColourSampleTest();

    public int adjustLuminanceLine(IPlottingTask task, IPixelData pixels, int x0, int y0, int x1, int y1, int adjustLum) {
        defaultColourTest.resetColourSamples(adjustLum);
        bresenham.plotLine(x0, y0, x1, y1, (x, y) -> defaultColourTest.addSample(pixels, x, y));
        return defaultColourTest.getCurrentAverage();
    }

}
