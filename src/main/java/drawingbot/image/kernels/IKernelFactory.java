package drawingbot.image.kernels;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

public interface IKernelFactory {

    String getFactoryName();

    boolean canProcess(BufferedImageOp imageOp);

    BufferedImage doProcess(BufferedImageOp imageOp, BufferedImage src, BufferedImage dst);

}
