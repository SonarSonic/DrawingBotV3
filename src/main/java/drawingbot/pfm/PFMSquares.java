package drawingbot.pfm;

import drawingbot.image.ConvolutionMatrices;
import drawingbot.image.ImageTools;
import drawingbot.image.RawLuminanceData;
import drawingbot.plotting.PlottingTask;
import org.imgscalr.Scalr;
import processing.core.PImage;

import java.awt.image.BufferedImage;

import static processing.core.PApplet.*;

public class PFMSquares extends AbstractSketchPFM {

    public PFMSquares(){
        super();
        squiggle_length = 1000;
        adjustbrightness = 9;
        desired_brightness = 250;
        tests = 4;
        minLineLength = 30;
        maxLineLength = 30;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void preProcess() {
        BufferedImage dst = (BufferedImage) task.getPlottingImage().getNative();

        dst = ImageTools.cropToAspectRatio(dst, app.getDrawingAreaWidthMM() / app.getDrawingAreaHeightMM());

        dst = ImageTools.lazyConvolutionFilter(dst, ConvolutionMatrices.MATRIX_UNSHARP_MASK, 3, true);

        dst = ImageTools.lazyImageBorder(dst, "border/b6.png", 0, 0);
        dst = ImageTools.lazyRGBFilter(dst, ImageTools::grayscaleFilter);

        dst = Scalr.resize(dst, Scalr.Method.QUALITY, (int)(dst.getWidth() * plottingResolution), (int)(dst.getHeight()* plottingResolution));

        task.img_plotting = new PImage(dst);
        rawBrightnessData = RawLuminanceData.createBrightnessData(dst);
        initialProgress = rawBrightnessData.getAverageBrightness();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void findDarkestNeighbour(int start_x, int start_y) {
        darkest_neighbor = 257;
        float start_angle;
        float delta_angle;

        start_angle = 36 + degrees((sin(radians(start_x/9F+46F)) + cos(radians(start_y/26F+26F))));
        delta_angle = 360.0F / (float)tests;

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