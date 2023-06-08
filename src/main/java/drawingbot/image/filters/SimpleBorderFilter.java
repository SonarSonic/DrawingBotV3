package drawingbot.image.filters;

import com.jhlabs.image.AbstractBufferedImageOp;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.ImageTools;
import drawingbot.image.blend.EnumBlendMode;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**A quick and dirty way of softening the edges of your drawing.
 * Look in the boarders directory for some examples.
 * Ideally, the boarder will have similar dimensions as the image to be drawn.
 * For far more control, just edit your input image directly.
 * Most of the examples are pretty heavy handed so you can "shrink" them a few pixels as desired.
 * It does not matter if you use a transparant background or just white.  JPEG or PNG, it's all good.
 */
public class SimpleBorderFilter extends AbstractBufferedImageOp{

    public final static String prefix = "border/b", suffix = ".png";
    public int borderNumber;
    public String borderFileName = "";

    public SimpleBorderFilter(){
        this.borderNumber = 1;
    }

    public SimpleBorderFilter(int borderNumber){
        this.borderNumber = borderNumber;
    }

    public SimpleBorderFilter(String borderFileName){
        this.borderFileName = borderFileName;
    }

    public static BufferedImage getBorderImage(int borderNumber) {
        try {
            return BufferedImageLoader.loadImage(prefix + borderNumber + suffix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage getBorderImage(String fileName) {
        try {
            return BufferedImageLoader.loadImage(fileName, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BufferedImage getBorderImage() {
        BufferedImage borderImage = null;
        if(!borderFileName.isEmpty()){
            borderImage = getBorderImage(borderFileName);
        }
        if(borderImage == null){
            borderImage = getBorderImage(borderNumber);
        }
        return borderImage;
    }


    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (dest == null ){
            dest = createCompatibleDestImage( src, null );
        }

        BufferedImage borderImg  = getBorderImage();
        if(borderImg != null){

            ///starting with white allows us to use images with an alpha layer
            dest = ImageTools.lazyBackground(dest, Color.WHITE);
            dest = ImageTools.drawImage(src, dest);

            borderImg = Scalr.resize(borderImg, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, src.getWidth(), src.getHeight());
            borderImg = ImageTools.lazyRGBFilter(borderImg, ImageTools::invertFilter);

            dest = ImageTools.lazyBlend(dest, borderImg, EnumBlendMode.ADD);
        }
        return dest;
    }

    public static class DirtyBorder extends SimpleBorderFilter{

        public DirtyBorder() {
            super();
        }

        public DirtyBorder(int borderNumber) {
            super(borderNumber);
        }

        @Override
        public BufferedImage getBorderImage() {
            return getBorderImage(borderNumber);
        }
    }

    public static class CustomBorder extends SimpleBorderFilter{

        public CustomBorder() {
            super();
        }

        public CustomBorder(String borderFileName) {
            super(borderFileName);
        }

        @Override
        public BufferedImage getBorderImage() {
            return getBorderImage(borderFileName);
        }
    }

}