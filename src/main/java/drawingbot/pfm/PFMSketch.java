package drawingbot.pfm;

import drawingbot.image.ConvolutionMatrices;
import drawingbot.image.ImageTools;
import drawingbot.image.RawLuminanceData;
import drawingbot.plotting.PlottingTask;
import org.imgscalr.Scalr;
import processing.core.PImage;

import java.awt.image.*;

public class PFMSketch extends AbstractSketchPFM {

    public boolean enableShading;
    public int squigglesTillShading;
    public int startAngleMin;
    public int startAngleMax;
    public float drawingDeltaAngle;
    public float shadingDeltaAngle;

    @Override
    public void init(PlottingTask task) {
        super.init(task);
        if(startAngleMax < startAngleMin){
            int value = startAngleMin;
            startAngleMin = startAngleMax;
            startAngleMax = value;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void preProcess() {

        BufferedImage dst = (BufferedImage) task.getPlottingImage().getNative();

        dst = ImageTools.cropToAspectRatio(dst, app.getDrawingAreaWidthMM() / app.getDrawingAreaHeightMM());

        dst = ImageTools.lazyConvolutionFilter(dst, ConvolutionMatrices.MATRIX_UNSHARP_MASK, 4, true);
        dst = ImageTools.lazyConvolutionFilter(dst, ConvolutionMatrices.MATRIX_UNSHARP_MASK, 3, true);

        dst = ImageTools.lazyImageBorder(dst, "border/b1.png", 0, 0);
        dst = ImageTools.lazyImageBorder(dst, "border/b11.png", 0, 0);
        dst = ImageTools.lazyRGBFilter(dst, ImageTools::grayscaleFilter);

        dst = Scalr.resize(dst, Scalr.Method.QUALITY, (int)(dst.getWidth() * plottingResolution), (int)(dst.getHeight()* plottingResolution));

        task.img_plotting = new PImage(dst);
        rawBrightnessData = RawLuminanceData.createBrightnessData(dst);
        initialProgress = rawBrightnessData.getAverageBrightness();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void findDarkestNeighbour(int start_x, int start_y) {
        darkest_neighbor = 1000;
        float delta_angle;
        float start_angle = randomSeed(startAngleMin, startAngleMax);    // Spitfire;

        if (!enableShading || squiggle_count < squigglesTillShading) {
            delta_angle = drawingDeltaAngle / (float)tests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        for (int d = 0; d < tests; d ++) {
            bresenhamAvgBrightness(rawBrightnessData, start_x, start_y, nextLineLength, (delta_angle * d) + start_angle);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void postProcess() {
        task.img_plotting = new PImage(rawBrightnessData.asBufferedImage());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

}