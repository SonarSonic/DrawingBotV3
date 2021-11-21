package drawingbot.pfm;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.GrayscaleFilter;
import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.image.ImageTools;
import drawingbot.image.PixelDataLuminance;
import drawingbot.pfm.helpers.LuminanceTestLine;

import java.awt.image.BufferedImage;

public class PFMSketchSobel extends AbstractSketchPFM {

    public IPixelData sobelPixelData;
    public float sobelIntensity;
    public int adjustSobel;


    @Override
    public BufferedImage preFilter(BufferedImage image) {
        BufferedImage filtered = new GrayscaleFilter().filter(image, null);
        filtered = new EdgeFilter().filter(filtered, null);

        sobelPixelData = ImageTools.copyToPixelData(filtered, new PixelDataLuminance(filtered.getWidth(), filtered.getHeight()));
        return image;
    }

    @Override
    protected boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {
        float start_angle = randomSeedF(0, 45);
        float delta_angle = 360F / lineTests;
        int nextLineLength = randomSeed(20, 30);

        LuminanceSobelTest luminanceTest = new LuminanceSobelTest(sobelPixelData, sobelIntensity, darkestDst, minLineLength, maxLineLength, true);
        luminanceTest.resetTest();
        for (int d = 0; d < lineTests; d ++) {
            luminanceTest.resetSamples();
            bresenham.plotAngledLine(point[0], point[1], nextLineLength, (delta_angle * d) + start_angle, (x, y) -> luminanceTest.addSample(pixels, x, y));
        }
        return luminanceTest.getDarkestSample() < pixels.getAverageLuminance();
    }

    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust){
        int rgba = adjustLuminanceLine(task, task.getPixelData(), x1, y1, x2, y2, adjust);
        task.addGeometry(new GLine(x1, y1, x2, y2), null, rgba);

        ///adjust sobel data
        adjustLuminanceLine(task, sobelPixelData, x1, y1, x2, y2, -adjustSobel);
    }

    public static class LuminanceSobelTest extends LuminanceTestLine {

        public IPixelData sobelPixelData;
        public float sobelIntensity;

        protected int sum_sobel = 0;

        public LuminanceSobelTest(IPixelData sobelPixelData, float sobelIntensity, int[] darkestDst, int minPixelCount, int maxPixelCount, boolean stopPrematurely){
            super(darkestDst, minPixelCount, maxPixelCount, stopPrematurely);
            this.sobelPixelData = sobelPixelData;
            this.sobelIntensity = sobelIntensity;
        }

        @Override
        public void resetSamples() {
            super.resetSamples();
            sum_sobel = 0;
        }

        @Override
        public void addSample(IPixelData pixels, int x, int y) {
            if(isPixelInvalid(pixels, x, y)){
                return;
            }
            float luminance = pixels.getLuminance(x, y);
            float sobel = sobelPixelData.getLuminance(x, y)*sobelIntensity;
            luminanceSum += luminance-sobel;

            pixelCount++;

            float testResult = ((float) luminanceSum - sum_sobel) / pixelCount;

            if (darkestSample == -1 || testResult < darkestSample) {
                darkestDst[0] = x;
                darkestDst[1] = y;
                darkestSample = testResult;
            }
        }
    }
}
