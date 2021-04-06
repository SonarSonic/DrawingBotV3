package drawingbot.image;

import drawingbot.DrawingBotV3;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class BufferedImageLoader extends Task<BufferedImage> {

    public String url;
    public boolean internal; //true if the image should be loaded from within the jar

    public BufferedImageLoader(String url, boolean internal){
        this.url = url;
        this.internal = internal;
    }

    @Override
    protected BufferedImage call() throws Exception {
        return loadImage(url, internal);
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        DrawingBotV3.logger.log(Level.SEVERE, "Buffered Image Loader Failed", t);
    }

    public static FilteredBufferedImage loadFilteredImage(String url, boolean internal) throws IOException {
        BufferedImage source = loadImage(url, internal);
        if(source != null){
            FilteredBufferedImage filtered = new FilteredBufferedImage(source);
            filtered.updateAll();
            return filtered;
        }
        return null;
    }

    public static BufferedImage loadImage(String url, boolean internal) throws IOException {
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

        //convert the image to the default ARGB format.
        if(img != null){
            BufferedImage optimalImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = optimalImg.createGraphics();
            graphics.drawImage(img, 0, 0, null);
            graphics.dispose();
            return optimalImg;
        }
        return null;
    }

    public static class Filtered extends Task<FilteredBufferedImage> {

        public String url;
        public boolean internal; //true if the image should be loaded from within the jar

        public Filtered(String url, boolean internal){
            this.url = url;
            this.internal = internal;
        }

        @Override
        protected FilteredBufferedImage call() throws Exception {

            updateProgress(-1, 1);
            updateTitle("Importing Image: " + url);

            updateMessage("Loading");
            BufferedImage source = loadImage(url, internal);

            updateMessage("Filtering");
            FilteredBufferedImage filtered = new FilteredBufferedImage(source);
            filtered.updateAll();

            updateMessage("Finished");
            updateProgress(1, 1);
            return filtered;
        }
    }
}
