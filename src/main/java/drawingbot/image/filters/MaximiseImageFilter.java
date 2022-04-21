package drawingbot.image.filters;

import com.jhlabs.image.AbstractBufferedImageOp;
import drawingbot.image.ImageTools;
import drawingbot.utils.Utils;

import java.awt.image.BufferedImage;

public class MaximiseImageFilter extends AbstractBufferedImageOp {

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (dest == null ){
            dest = createCompatibleDestImage( src, null );
        }

        int min = 255;
        int max = 0;
        for(int x = 0; x < src.getWidth(); x++){
            for(int y = 0; y < src.getHeight(); y++){
                int argb = src.getRGB(x, y);
                int alpha = ImageTools.alpha(argb);
                if(alpha > 0){
                    int luminance = ImageTools.getPerceivedLuminanceFromRGB(argb);
                    min = Math.min(luminance, min);
                    max = Math.max(luminance, max);
                }
            }
        }

        for(int x = 0; x < src.getWidth(); x++){
            for(int y = 0; y < src.getHeight(); y++){
                int argb = src.getRGB(x, y);
                int alpha = ImageTools.alpha(argb);
                int luminance = ImageTools.getPerceivedLuminanceFromRGB(argb);
                int mapped = Utils.mapInt(luminance, min, max, 0, 255);
                dest.setRGB(x, y, ImageTools.getARGB(alpha, mapped, mapped, mapped));
            }
        }
        return dest;
    }
}
