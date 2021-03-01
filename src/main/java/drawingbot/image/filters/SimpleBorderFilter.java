package drawingbot.image.filters;

import com.jhlabs.image.AbstractBufferedImageOp;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.ImageTools;
import drawingbot.image.blend.EnumBlendMode;
import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;

public class SimpleBorderFilter extends AbstractBufferedImageOp{

    public final String prefix = "border/b", suffix = ".png";
    public int borderNumber;

    public SimpleBorderFilter(){
        this.borderNumber = 1;
    }

    public SimpleBorderFilter(int borderNumber){
        this.borderNumber = borderNumber;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (dest == null ){
            dest = createCompatibleDestImage( src, null );
        }
        BufferedImage borderImg = BufferedImageLoader.loadImage(prefix + borderNumber + suffix, true);
        if(borderImg != null){
            ImageTools.drawImage(src, dest);

            borderImg = Scalr.resize(borderImg, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, src.getWidth(), src.getHeight());
            borderImg = ImageTools.lazyRGBFilter(borderImg, ImageTools::invertFilter);

            dest = ImageTools.lazyBlend(dest, borderImg, EnumBlendMode.ADD);
        }
        return dest;
    }
}
