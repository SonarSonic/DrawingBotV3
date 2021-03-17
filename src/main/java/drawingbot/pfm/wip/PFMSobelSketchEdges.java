package drawingbot.pfm.wip;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.GrayscaleFilter;
import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.image.ImageTools;
import drawingbot.image.PixelDataLuminance;
import drawingbot.pfm.AbstractSketchPFM;

import java.awt.image.BufferedImage;

public class PFMSobelSketchEdges extends AbstractSketchPFM {

    public IPixelData sobelPixelData;

    @Override
    public BufferedImage preFilter(BufferedImage image) {
        BufferedImage filtered = new GrayscaleFilter().filter(image, null);
        filtered = new EdgeFilter().filter(filtered, null);

        sobelPixelData = ImageTools.copyToPixelData(filtered, new PixelDataLuminance(filtered.getWidth(), filtered.getHeight()));
        return image;
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float delta_angle = 360F / tests;
        int nextLineLength = randomSeed(20, 30);

        resetLuminanceTest();
        for (int d = 0; d < tests; d ++) {
            luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (delta_angle * d) + 45);
        }
    }

    @Override
    protected boolean luminanceTest(IPixelData pixels, int x, int y) {
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return true;
        }
        double sobel = sobelPixelData.getLuminance(x, y);
        double luminance = pixels.getLuminance(x, y);
        sum_luminance += luminance > 200 ? luminance : Math.max(luminance - sobel*4, 0);

        count_pixels++;

        if (test_luminance == -1 || getLuminanceTestAverage() < test_luminance) {
            darkest_x = x;
            darkest_y = y;
            test_luminance = getLuminanceTestAverage();
        }
        return false;
    }

    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust){
        int rgba = adjustLuminanceLine(task, task.getPixelData(), x1, y1, x2, y2, adjust);
        task.addGeometry(new GLine(x1, y1, x2, y2), null, rgba);
        /*
        bresenham.line(x1, y1, x2, y2, (x, y) -> {
            sobelPixelData.adjustLuminance(x, y, -adjust*4);
            return false;
        });

         */
    }
}
