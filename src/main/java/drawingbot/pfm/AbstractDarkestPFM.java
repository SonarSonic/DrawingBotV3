package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.utils.AlgorithmHelper;
import drawingbot.utils.Utils;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDarkestPFM extends AbstractPFM {

    protected int sampleWidth = 10;
    protected int sampleHeight = 10;

    protected int darkest_x;
    protected int darkest_y;
    protected float darkest_value;
    protected float darkest_neighbor;

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
        task.useCustomARGB(true);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** finds the darkest area of the image, according to sampleWidth/sampleHeight and returns a random pixel in that area */
    protected void findDarkestArea(IPixelData pixels) {
        int totalSamplesX = pixels.getWidth()/sampleWidth;
        int totalSamplesY = pixels.getHeight()/sampleHeight;

        int darkestSampleX = 0;
        int darkestSampleY = 0;

        darkest_value = 1000;

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

    /** returns a random pixel of the darkest pixels found*/
    public void findDarkestPixel(IPixelData pixels){
        List<Pair<Integer, Integer>> points = new ArrayList<>();
        int luminance = pixels.getLuminance(0,0);


        for(int x = 0; x < pixels.getWidth(); x ++){
            for(int y = 0; y < pixels.getHeight(); y ++){
                int c = pixels.getLuminance(x, y);
                if(c == luminance) {
                    points.add(new Pair<>(x, y));
                }
                if(c < luminance) {
                    points.clear();
                    points.add(new Pair<>(x, y));
                    luminance = c;
                }
            }
        }

        Pair<Integer, Integer> point = points.get(randomSeed.nextInt(points.size()));
        darkest_x = point.getKey();
        darkest_y = point.getValue();
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

    protected void bresenhamAvgLuminance(IPixelData pixels, int x0, int y0, float distance, float degree) {
        sum_luminance = 0;
        count_pixels = 0;

        int x1, y1;
        x1 = (int)(Math.cos(Math.toRadians(degree))*distance) + x0;
        y1 = (int)(Math.sin(Math.toRadians(degree))*distance) + y0;
        x0 = Utils.clamp(x0, 0, pixels.getWidth()-1);
        y0 = Utils.clamp(y0, 0, pixels.getHeight()-1);
        x1 = Utils.clamp(x1, 0, pixels.getWidth()-1);
        y1 = Utils.clamp(y1, 0, pixels.getHeight()-1);

        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> bresenhamTest(pixels, x, y));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    ///bresenham calculations
    protected int sum_luminance = 0;
    protected int count_pixels = 0;

    protected int sum_red = 0;
    protected int sum_green = 0;
    protected int sum_blue = 0;
    protected int sum_alpha = 0;
    protected int total_pixels = 0;

    protected void bresenhamTest(IPixelData pixels, int x, int y){
        sum_luminance += pixels.getLuminance(x, y);
        count_pixels++;
        if ((float) sum_luminance / (float) count_pixels < darkest_neighbor) {
            darkest_x = x;
            darkest_y = y;
            darkest_neighbor = (float) sum_luminance / (float) count_pixels;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void bresenhamLighten(IPlottingTask task, IPixelData pixels, int x0, int y0, int x1, int y1, int adjustLum) {
        sum_red = 0;
        sum_green = 0;
        sum_blue = 0;
        sum_alpha = 0;
        total_pixels = 0;
        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> adjustLuminanceColour(pixels, x, y, adjustLum));
        task.setCustomARGB(ImageTools.getARGB(sum_alpha / total_pixels, sum_red /total_pixels, sum_green / total_pixels, sum_blue / total_pixels));
    }

    public void adjustLuminanceColour(IPixelData pixels, int x, int y, int adjustLum){
        total_pixels++;
        sum_alpha += pixels.getAlpha(x, y);
        sum_red += pixels.getRed(x, y);
        sum_green += pixels.getGreen(x, y);
        sum_blue += pixels.getBlue(x, y);
        pixels.adjustRed(x, y, adjustLum);
        pixels.adjustGreen(x, y, adjustLum);
        pixels.adjustBlue(x, y, adjustLum);
    }

}
