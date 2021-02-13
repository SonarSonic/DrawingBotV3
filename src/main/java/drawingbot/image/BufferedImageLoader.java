package drawingbot.image;

import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public static BufferedImage loadImage(String url, boolean internal) {
        BufferedImage img = null;
        try {
            InputStream stream = null;
            if(internal){
                stream = BufferedImageLoader.class.getClassLoader().getResourceAsStream(url);
            }else{
                stream = new FileInputStream(url);
            }
            if(stream != null){
                img = ImageIO.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
}
