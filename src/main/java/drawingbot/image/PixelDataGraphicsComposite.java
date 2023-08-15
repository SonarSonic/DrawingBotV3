package drawingbot.image;

import java.awt.*;

/**
 * A special version of {@link PixelDataComposite} which simplifies the use of AWT rendering onto the pixel data via the Graphics2D object,
 */
public class PixelDataGraphicsComposite extends PixelDataComposite<PixelDataARGBY, PixelDataBufferedImage>{

    private Graphics2D graphics2D;

    private PixelDataGraphicsComposite(PixelDataARGBY background, PixelDataBufferedImage foreground) {
        super(background, foreground);
    }

    public static PixelDataGraphicsComposite create(int width, int height){
        PixelDataARGBY background = new PixelDataARGBY(width, height);

        PixelDataBufferedImage foreground = new PixelDataBufferedImage(width, height);
        Graphics2D graphics2D = foreground.image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        PixelDataGraphicsComposite graphicsComposite = new PixelDataGraphicsComposite(background, foreground);
        graphicsComposite.graphics2D = graphics2D;
        return graphicsComposite;
    }

    public Graphics2D getGraphics2D(){
        return graphics2D;
    }

    @Override
    public void destroy() {
        super.destroy();
        graphics2D.dispose();
    }
}
