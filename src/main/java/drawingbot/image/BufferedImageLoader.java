package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.format.ImageData;
import drawingbot.utils.DBTask;
import drawingbot.utils.EnumRotation;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;

public class BufferedImageLoader extends DBTask<BufferedImage> {

    public final String url;
    public final boolean internal; //true if the image should be loaded from within the jar

    public BufferedImageLoader(DBTaskContext context, String url, boolean internal){
        super(context);
        this.url = url;
        this.internal = internal;
    }

    @Override
    protected BufferedImage call() throws Exception {
        return loadImage(url, internal);
    }

    @Override
    public void setException(Throwable t) {
        super.setException(t);
        DrawingBotV3.logger.log(Level.SEVERE, "Buffered Image Loader Failed", t);
    }

    public static ImageData loadImageData(DBTaskContext context, String url, boolean internal) throws IOException {
        BufferedImage source = loadImage(url, internal);
        if(source != null){
            return new ImageData(new File(url), source);
        }
        return null;
    }

    public static BufferedImage loadImage(String url, boolean internal) throws IOException {

        boolean isVideo = FileUtils.matchesExtensionFilter(FileUtils.getExtension(url), FileUtils.IMPORT_VIDEOS);
        if(isVideo){
            try {
                return loadImageFromVideo(url);
            } catch (JCodecException e) {
                e.printStackTrace();
            }
            return null;
        }

        InputStream stream;
        if(internal){
            stream = DrawingBotV3.class.getClassLoader().getResourceAsStream(url);
        }else{
            stream = new FileInputStream(url);
        }

        BufferedImage img = null;
        if(stream != null){
            img = ImageIO.read(stream);
        }

        return convertToARGB(img);
    }

    /**
     * Loads the first image from a video
     */
    public static BufferedImage loadImageFromVideo(String url) throws IOException, JCodecException {
        Picture picture = FrameGrab.getFrameFromFile(new File(url), 0);
        return convertToARGB(AWTUtil.toBufferedImage(picture));
    }

    public static BufferedImage convertToARGB(BufferedImage img){
        if(img == null){
            return null;
        }
        if(img.getType() == BufferedImage.TYPE_INT_ARGB){
            return img;
        }
        BufferedImage optimalImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = optimalImg.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();
        return optimalImg;
    }

    public static class Transformed extends BufferedImageLoader {

        public EnumRotation imageRotation;
        public boolean flipHorizontal;
        public boolean flipVertical;

        public Transformed(DBTaskContext context, String url, boolean internal, EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical) {
            super(context, url, internal);
            this.imageRotation = imageRotation;
            this.flipHorizontal = flipHorizontal;
            this.flipVertical = flipVertical;
        }

        @Override
        protected BufferedImage call() throws Exception {
            BufferedImage image = super.call();
            image = ImageTools.rotateImage(image, imageRotation, flipHorizontal, flipVertical);
            return image;
        }
    }
}
