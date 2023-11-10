package drawingbot.image;

import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * A special version of {@link PixelDataARGBY} which has the ability to perform additive draw operations with less performance overhead, not intended for general use, used directly by {@link drawingbot.pfm.helpers.PFMRenderPipe}
 */
public class PixelDataAdditiveComposite extends PixelDataARGBY{

    private BufferedImage cacheImage;
    private Graphics2D cacheGraphics;

    public Rectangle2D cacheRect;
    public boolean isDrawing = false;

    private PixelDataAdditiveComposite(int width, int height) {
        super(width, height);
    }

    public static PixelDataAdditiveComposite create(int width, int height){
        PixelDataAdditiveComposite graphicsComposite = new PixelDataAdditiveComposite(width, height);

        BufferedImage cacheImage = ObservableWritableRaster.createObservableBufferedImage(width, height, (x, y) -> {
            if(!graphicsComposite.isDrawing){
                return;
            }
            if(graphicsComposite.cacheRect == null || graphicsComposite.cacheRect.isEmpty()){
                graphicsComposite.cacheRect = new Rectangle2D.Double(x, y, 1, 1);
            }
            graphicsComposite.cacheRect.add(x, y);
        });
        graphicsComposite.cacheImage = cacheImage;
        graphicsComposite.cacheGraphics = cacheImage.createGraphics();
        graphicsComposite.cacheGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphicsComposite.cacheGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return graphicsComposite;
    }

    public Graphics2D getCacheGraphics(){
        return cacheGraphics;
    }

    public void preDraw(){
        cacheRect = null;
        isDrawing = true;
    }

    public void postDraw(BresenhamHelper.IPixelSetter callback){
        isDrawing = false;

        if(cacheRect == null){
            return;
        }
        for(int x = (int) cacheRect.getMinX(); x <= cacheRect.getMaxX() && x < cacheImage.getWidth(); x++){
            for(int y = (int) cacheRect.getMinY(); y <= cacheRect.getMaxY() && y < cacheImage.getHeight(); y++){

                int argb = cacheImage.getRGB(x, y);
                int cacheAlpha = ImageTools.alpha(argb);
                int cacheRed = ImageTools.red(argb);
                int cacheGreen = ImageTools.green(argb);
                int cacheBlue = ImageTools.blue(argb);

                if(cacheAlpha != 0){ // Check we've drawn something

                    int red = (int) Utils.clamp(getRed(x, y) + (cacheRed * (cacheAlpha/255F)), 0, 255);
                    int green = (int) Utils.clamp(getGreen(x, y) + (cacheGreen * (cacheAlpha/255F)), 0, 255);
                    int blue = (int) Utils.clamp(getBlue(x, y) + (cacheBlue * (cacheAlpha/255F)), 0, 255);
                    int alpha = getAlpha(x, y);
                    setARGB(x, y, alpha, red, green, blue);

                    if(callback != null){
                        callback.setPixel(x, y);
                    }
                }
            }
        }
        //Clean Up - Remove any changes to prepare for the next draw
        cacheGraphics.setBackground(new Color(0, 0, 0, 0));
        cacheGraphics.clearRect((int) cacheRect.getMinX(), (int) cacheRect.getMinY(), (int) cacheRect.getWidth()+1, (int) cacheRect.getHeight()+1);
    }

    @Override
    public void destroy() {
        super.destroy();
        cacheGraphics.dispose();
    }
}