package drawingbot.pfm.wip;

import drawingbot.api.IPixelData;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.pfm.helpers.LuminanceTestLine;

import java.util.function.Function;

public class PFMSketchWaves extends AbstractSketchPFM {

    public float waveOffsetX;
    public float waveOffsetY;
    public float waveDivisorX;
    public float waveDivisorY;

    public WaveType xWave;
    public WaveType yWave;

    public float startAngle;

    public PFMSketchWaves(){
        super();
    }

    @Override
    public boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst){

        double xOffset = xWave.waveFunction.apply(Math.toRadians((point[0]/waveDivisorX)+waveOffsetX));
        double yOffset = yWave.waveFunction.apply(Math.toRadians((point[1]/waveDivisorY)+waveOffsetY));

        float angle = startAngle + (float)Math.toDegrees(xOffset + yOffset);
        float deltaAngle = 360.0F / (float) lineTests;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        LuminanceTestLine luminanceTest = new LuminanceTestLine(darkestDst, minLineLength, maxLineLength, true);
        for (int d = 0; d < lineTests; d ++) {
            luminanceTest.resetSamples();
            bresenham.plotAngledLine(point[0], point[1], nextLineLength, (deltaAngle * d) + angle, (x, y) -> luminanceTest.addSample(pixels, x, y));
        }
        return luminanceTest.getDarkestSample() < pixels.getAverageLuminance();
    }

    public enum WaveType{
        SIN(Math::sin),
        COS(Math::cos),
        TAN(Math::tan),
        TEST1(Math::sinh),
        TEST2(Math::cosh),
        TEST3(Math::tanh);

        public Function<Double, Double> waveFunction;

        WaveType(Function<Double, Double> waveFunction){
            this.waveFunction = waveFunction;
        }
    }
}