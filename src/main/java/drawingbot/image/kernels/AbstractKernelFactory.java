package drawingbot.image.kernels;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.WritableRaster;

public abstract class AbstractKernelFactory<C extends BufferedImageOp> implements IKernelFactory {

    public final Class<C> clazz;

    public AbstractKernelFactory(Class<C> clazz){
        this.clazz = clazz;
    }

    @Override
    public String getFactoryName() {
        return clazz.getSimpleName();
    }

    @Override
    public boolean canProcess(BufferedImageOp imageOp) {
        return clazz.isInstance(imageOp);
    }

    @Override
    public BufferedImage doProcess(BufferedImageOp imageOp, BufferedImage src, BufferedImage dst) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        WritableRaster srcRaster = src.getRaster();
        WritableRaster dstRaster = dst.getRaster();

        final int[] inPixels = new int[width * height];
        final int[] outPixels = new int[width * height];

        srcRaster.getDataElements(0, 0, width, height, inPixels);

        Kernel kernel = createKernel((C)imageOp, inPixels, outPixels, width, height);
        Range range = Range.create(outPixels.length);
        kernel.execute(range);

        dstRaster.setDataElements(0, 0, width, height, outPixels);
        return dst;
    }

    public abstract Kernel createKernel(C imageOp, int[] inPixels, int[] outPixels, int width, int height);

}
