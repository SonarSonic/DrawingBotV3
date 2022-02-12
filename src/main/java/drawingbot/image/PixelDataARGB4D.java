package drawingbot.image;

import drawingbot.utils.Utils;
import org.joml.Vector4d;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to both ARGB + Distance (D) values, and also provides cached averages for each
 */
public class PixelDataARGB4D extends PixelDataARGB {

    public RawData distance;
    private int targetA;
    private int targetR;
    private int targetG;
    private int targetB;
    private double maxDistance;
    
    public PixelDataARGB4D(int width, int height) {
        super(width, height);
        this.distance = new RawData(width, height);
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
        maxDistance = getMaxColourDistance();
    }
    
    @Override
    public void setChannel(int channel, int x, int y, int value) {
        super.setChannel(channel, x, y, value);
        updateColourDistance(x, y, alpha.getData(x, y), red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        super.adjustChannel(channel, x, y, value);
        updateColourDistance(x, y, alpha.getData(x, y), red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }
    
    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        super.setARGB(x, y, a, r, g, b);
        updateColourDistance(x, y, a, r, g, b);
    }

    @Override
    public void setLuminance(int x, int y, int luminance) {
        //super.setLuminance(x, y, luminance);
        alpha.setData(x, y, luminance);
        updateColourDistance(x, y);
    }

    @Override
    public void adjustLuminance(int x, int y, int luminance) {
        //super.adjustLuminance(x, y, luminance);
        alpha.adjustData(x, y, -luminance);
        updateColourDistance(x, y);
    }

    @Override
    public int getLuminance(int x, int y) {
        return distance.getData(x, y);
    }

    @Override
    public double getAverageLuminance() {
        return distance.getAverage();
    }

    public void updateColourDistance(){
        for(int x = 0; x < getWidth(); x ++) {
            for (int y = 0; y < getHeight(); y++) {
                updateColourDistance(x, y);
            }
        }
    }

    public void updateColourDistance(int x, int y){
        int argb = getARGB(x, y);
        updateColourDistance(x, y, ImageTools.alpha(argb), ImageTools.red(argb), ImageTools.green(argb), ImageTools.blue(argb));
    }

    public void updateColourDistance(int x, int y, int a, int r, int g, int b){
        int d = weightedColourDistance(targetA, targetR, targetG, targetB, a, r, g, b);
        distance.setData(x, y, d);
    }

    public static double getMaxColourDistance(){
        return rawColourDistance(0, 0, 0, 0, 255, 255, 255, 255);
    }

    public static double rawColourDistance(int a1, int r1, int g1, int b1, int a2, int r2, int g2, int b2){
        Vector4d v = new Vector4d(6.0*a1, 2.2*r1, 4.3*g1, 3.5*b1);
        v.sub(6.0*a2, 2.2*r2, 4.3*g2, 3.5*b2);
        return v.length();
    }

    public int weightedColourDistance(int targetA, int targetR, int targetG, int targetB, int a2, int r2, int g2, int b2){
        double rawDistance = rawColourDistance(targetA, targetR, targetG, targetB, a2, r2, g2, b2);
        return Math.min((int) Utils.mapDouble(rawDistance, 0, maxDistance, 0, 255*2), 255);
    }
}