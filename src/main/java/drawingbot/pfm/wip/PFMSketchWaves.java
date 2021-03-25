package drawingbot.pfm.wip;

import drawingbot.api.IPixelData;
import drawingbot.pfm.AbstractSketchPFM;

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
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {

        double xOffset = xWave.waveFunction.apply(Math.toRadians((start_x/waveDivisorX)+waveOffsetX));
        double yOffset = yWave.waveFunction.apply(Math.toRadians((start_y/waveDivisorY)+waveOffsetY));

        float angle = startAngle + (float)Math.toDegrees(xOffset + yOffset);
        float deltaAngle = 360.0F / (float)tests;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        resetLuminanceTest();
        for (int d = 0; d < tests; d ++) {
            luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (deltaAngle * d) + angle);
        }
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