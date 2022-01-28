package drawingbot.image;

import org.joml.Vector4d;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to both ARGB + Distance (D) values, and also provides cached averages for each
 */
public class PixelDataARGB4D extends PixelDataARGB {

    public RawData luminance;
    private int targetA;
    private int targetR;
    private int targetG;
    private int targetB;
    
    public PixelDataARGB4D(int width, int height) {
        super(width, height);
        this.luminance = new RawData(width, height);
        this.targetA = 255;
        this.targetR = 0;
        this.targetG = 0;
        this.targetB = 0;
    }

    public void setTargetColor(int target){
        targetA = ImageTools.alpha(target);
        targetR = ImageTools.red(target);
        targetG = ImageTools.green(target);
        targetB = ImageTools.blue(target);
    }

    public void updateColorDistance(){
        for(int x = 0; x < getWidth(); x ++) {
            for (int y = 0; y < getHeight(); y++) {
                int argb = getARGB(x, y);
                updateColorDistance(x, y, ImageTools.alpha(argb), ImageTools.red(argb), ImageTools.green(argb), ImageTools.blue(argb));
            }
        }
    }
    
    @Override
    public void setChannel(int channel, int x, int y, int value) {
        super.setChannel(channel, x, y, value);
        updateColorDistance(x, y, alpha.getData(x, y), red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        super.adjustChannel(channel, x, y, value);
        updateColorDistance(x, y, alpha.getData(x, y), red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }
    
    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        super.setARGB(x, y, a, r, g, b);
        updateColorDistance(x, y, a, r, g, b);
    }

    @Override
    public int getLuminance(int x, int y) {
        return luminance.getData(x, y);
    }

    @Override
    public double getAverageLuminance() {
        return luminance.getAverage();
    }

    protected void updateColorDistance(int x, int y, int a, int r, int g, int b){
        int d = colorDistance(targetA, targetR, targetG, targetB, a, r, g, b);
        luminance.setData(x, y, d);
    }

    public static int colorDistance(int a1, int r1, int g1, int b1, int a2, int r2, int g2, int b2){
        Vector4d v = new Vector4d(6.0*a1, 2.2*r1, 4.3*g1, 3.5*b1);
        v.sub(6.0*a2, 2.2*r2, 4.3*g2, 3.5*b2);
        return (int)(v.length()/10/3); // weighted max is 2157.42659 -> 10/3 is a magic voodoo sweet spot
    }
}
