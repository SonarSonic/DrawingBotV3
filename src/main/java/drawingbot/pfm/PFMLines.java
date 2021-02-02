package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.helpers.AlgorithmHelper;
import drawingbot.helpers.ImageTools;
import drawingbot.helpers.RawLuminanceData;
import drawingbot.plotting.PlottingTask;
import org.imgscalr.Scalr;
import processing.core.PImage;

import java.awt.image.BufferedImage;

public class PFMLines extends AbstractDarkestPFM{

    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    //public float desired_brightness;    // How long to process.  You can always stop early with "s" key

    public int tests;                   // Reasonable values:  13 for development, 720 for final

    protected int maxLines;

    protected float initialProgress;
    protected float progress;
    protected RawLuminanceData redData;
    protected RawLuminanceData greenData;
    protected RawLuminanceData blueData;
    protected RawLuminanceData grayData;
    protected RawLuminanceData[] channels;

    public PFMLines(PlottingTask task) {
        super(task);
        tests = 360;
        adjustbrightness = 30;
        sampleHeight = 1;
        sampleWidth = 1;
        maxLines = 5000;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void preProcess() {
        BufferedImage dst = (BufferedImage) task.getPlottingImage().getNative();

        //ImageTools.imageCrop(task); //TODO USE SCALR
        int targetWidth = (int)(app.getDrawingAreaWidthMM() * 0.5);
        int targetHeight = (int)(app.getDrawingAreaHeightMM() * 0.5);
        dst = Scalr.resize(dst, targetWidth, targetHeight);

        //dst = ImageTools.lazyConvolutionFilter(dst, ImageTools.MATRIX_UNSHARP_MASK, 4, true);
        //dst = ImageTools.lazyConvolutionFilter(dst, ImageTools.MATRIX_UNSHARP_MASK, 3, true);

        dst = ImageTools.lazyImageBorder(dst, "border/b1.png", 0, 0);
        dst = ImageTools.lazyImageBorder(dst, "border/b11.png", 0, 0);
        //dst = ImageTools.lazyRGBFilter(dst, ImageTools::grayscaleFilter);

        task.img_plotting = new PImage(dst);
        grayData = RawLuminanceData.createBrightnessData(dst);
        //blueData = RawLuminanceData.createRedData(dst);
        //greenData = RawLuminanceData.createGreenData(dst);
        //redData = RawLuminanceData.createBlueData(dst);
        channels = new RawLuminanceData[]{grayData};
    }

    @Override
    public float progress() {
        return progress;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void doProcess() {
        for(int i = 0; i < maxLines; i ++){
            for(RawLuminanceData channel : channels){
                findDarkestPixel(channel);
                int startX = darkest_x;
                int startY = darkest_y;
                float darkestLineAvg = 0;
                int[] line = null;

                for (int d = 0; d < tests; d ++) {
                    count_brightness = 0;
                    sum_brightness = 0;
                    int[] testLine = getFullLine(channel, startX, startY, randomSeed(0, 360));
                    AlgorithmHelper.bresenham(testLine[0], testLine[1], testLine[2], testLine[3], (x,y) -> bresenhamTest(channel, x, y));
                    float averageBrightness = (float)sum_brightness/(float)count_brightness;

                    if(line == null || averageBrightness < darkestLineAvg){
                        darkestLineAvg = averageBrightness;
                        line = testLine;
                    }
                }

                task.moveAbs(0, line[0], line[1]);
                task.penDown();
                task.moveAbs(0, line[2], line[3]);
                task.penUp();

                task.plottedDrawing.plottedLines.get(task.plottedDrawing.plottedLines.size()-1).rgba = getRGBAForChannel(channel);
                bresenhamLighten(channel, line[0], line[1], line[2], line[3], adjustbrightness);
                progress = (float)i / maxLines;

                if(task.isCancelled() || finished()){
                    break;
                }
            }
        }
        finish();
    }

    public int getRGBAForChannel(RawLuminanceData data){
        if(data == redData){
            return DrawingBotV3.INSTANCE.color(adjustbrightness, 0, 0, 50);
        }
        if(data == greenData){
            return DrawingBotV3.INSTANCE.color(0, adjustbrightness, 0, 50);
        }
        if(data == blueData){
            return DrawingBotV3.INSTANCE.color(0, 0, adjustbrightness, 50);
        }
        return DrawingBotV3.INSTANCE.color(adjustbrightness, adjustbrightness, adjustbrightness, 50);
    }

    public int[] getFullLine(RawLuminanceData data, int x0, int y0, float degree){
        double minX, minY, maxX, maxY;

        double maxWidth = data.width-1;
        double maxHeight = data.height-1;

        double slope = Math.tan(degree);

        double leftYIntercept = y0 - slope*x0;
        if(leftYIntercept >= maxHeight){
            minY = maxHeight;
            minX = ((maxHeight-leftYIntercept)/slope);
        }else if(leftYIntercept < 0){
            minY = 0;
            minX = ((-leftYIntercept)/slope);
        }else{
            minY = leftYIntercept;
            minX = 0;
        }

        double rightYIntercept = y0 - slope*(x0-maxWidth);

        if(rightYIntercept >= maxHeight){
            maxY = maxHeight-1;
            maxX = ((maxHeight-rightYIntercept)/slope)  + maxWidth+1;
        }else if(rightYIntercept < 0){
            maxY = 0;
            maxX = ((-rightYIntercept)/slope) + maxWidth+1;
        }else{
            maxY = rightYIntercept;
            maxX = maxWidth;
        }

        return new int[]{(int)Math.floor(minX), (int)Math.floor(minY), (int)Math.floor(maxX), (int)Math.floor(maxY)};
    }

    @Override
    public void postProcess() {
        //task.img_plotting = new PImage(red.asBufferedImage());
    }

}
