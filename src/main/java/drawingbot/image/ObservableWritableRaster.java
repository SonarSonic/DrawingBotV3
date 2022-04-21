package drawingbot.image;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Observes changes to pixels in a {@link BufferedImage}
 */
public class ObservableWritableRaster extends WritableRaster {

    public List<IPixelListener> listenerList = new ArrayList<>();

    /**
     * Implement this interface to observe changes to BufferedImages
     */
    public interface IPixelListener {
        void onPixelChanged(int x, int y);
    }

    public static ObservableWritableRaster createDefaultWritableRaster(int width, int height){
        ColorModel colormodel = ColorModel.getRGBdefault();
        WritableRaster tempRaster = colormodel.createCompatibleWritableRaster(width, height);
        return new ObservableWritableRaster(tempRaster.getSampleModel(), tempRaster.getDataBuffer(), new Point(0,0));
    }

    public static BufferedImage createObservableBufferedImage(int width, int height, IPixelListener listener){
        ColorModel colormodel = ColorModel.getRGBdefault();
        WritableRaster tempRaster = colormodel.createCompatibleWritableRaster(width, height);
        ObservableWritableRaster observableRaster = new ObservableWritableRaster(tempRaster.getSampleModel(), tempRaster.getDataBuffer(), new Point(0,0));
        BufferedImage bufferedImage = new BufferedImage(colormodel, observableRaster, true, null);
        observableRaster.listenerList.add(listener);
        return bufferedImage;
    }

    public ObservableWritableRaster(SampleModel sampleModel, Point origin) {
        super(sampleModel, origin);
    }

    public ObservableWritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        super(sampleModel, dataBuffer, origin);
    }

    public ObservableWritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate, WritableRaster parent) {
        super(sampleModel, dataBuffer, aRegion, sampleModelTranslate, parent);
    }

    private void onPixelsChanged(int x, int y, int w, int h){
        int x1 = x + w;
        int y1 = y + h;

        for (int i = y; i< y1; i++) {
            for (int j = x; j < x1; j++) {
                onPixelChanged(j, i);
            }
        }
    }

    private void onPixelChanged(int x, int y){
        x -= sampleModelTranslateX;
        y -= sampleModelTranslateY;

        for(IPixelListener listener : listenerList){
            listener.onPixelChanged(x, y);
        }
    }

    @Override
    public void setDataElements(int x, int y, Object inData) {
        super.setDataElements(x, y, inData);
        onPixelChanged(x, y);
    }

    /**
     * Called by {@link #setDataElements(int, int, Object)}
     */
    @Override
    public void setDataElements(int x, int y, Raster inRaster) {
        super.setDataElements(x, y, inRaster);
        //not needed
    }

    /**
     * Not currently implemented, more complex operation
     */
    @Override
    public void setDataElements(int x, int y, int w, int h, Object inData) {
        super.setDataElements(x, y, w, h, inData);
        onPixelsChanged(x, y, w, h);
    }

    @Override
    public void setPixel(int x, int y, int[] iArray) {
        super.setPixel(x, y, iArray);
        onPixelChanged(x, y);
    }

    @Override
    public void setPixel(int x, int y, float[] fArray) {
        super.setPixel(x, y, fArray);
        onPixelChanged(x, y);
    }

    @Override
    public void setPixel(int x, int y, double[] dArray) {
        super.setPixel(x, y, dArray);
        onPixelChanged(x, y);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, int[] iArray) {
        super.setPixels(x, y, w, h, iArray);
        onPixelsChanged(x, y, w, h);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, float[] fArray) {
        super.setPixels(x, y, w, h, fArray);
        onPixelsChanged(x, y, w, h);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, double[] dArray) {
        super.setPixels(x, y, w, h, dArray);
        onPixelsChanged(x, y, w, h);
    }

    @Override
    public void setSample(int x, int y, int b, int s) {
        super.setSample(x, y, b, s);
        onPixelChanged(x, y);
    }

    @Override
    public void setSample(int x, int y, int b, float s) {
        super.setSample(x, y, b, s);
        onPixelChanged(x, y);
    }

    @Override
    public void setSample(int x, int y, int b, double s) {
        super.setSample(x, y, b, s);
        onPixelChanged(x, y);
    }

    @Override
    public void setSamples(int x, int y, int w, int h, int b, int[] iArray) {
        super.setSamples(x, y, w, h, b, iArray);
        onPixelsChanged(x, y, w, h);
    }

    @Override
    public void setSamples(int x, int y, int w, int h, int b, float[] fArray) {
        super.setSamples(x, y, w, h, b, fArray);
        onPixelsChanged(x, y, w, h);
    }

    @Override
    public void setSamples(int x, int y, int w, int h, int b, double[] dArray) {
        super.setSamples(x, y, w, h, b, dArray);
        onPixelsChanged(x, y, w, h);
    }
}
