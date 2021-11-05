package drawingbot.image.kernels;

import com.aparapi.Kernel;

public abstract class AbstractKernel extends Kernel {

    public final int[] inPixels;
    public final int[] outPixels;

    public AbstractKernel(int[] inPixels, int[] outPixels){
        this.inPixels = inPixels;
        this.outPixels = outPixels;
    }
}
