package drawingbot.image;

import drawingbot.api.IPixelData;
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

    public PixelDataAdditiveComposite(int width, int height) {
        super(width, height);

        BufferedImage cacheImage = ObservableWritableRaster.createObservableBufferedImage(width, height, (x, y) -> {
            if(!isDrawing){
                return;
            }
            if(this.cacheRect == null || this.cacheRect.isEmpty()){
                this.cacheRect = new Rectangle2D.Double(x, y, 1, 1);
            }
            this.cacheRect.add(x, y);
        });
        this.cacheImage = cacheImage;
        this.cacheGraphics = cacheImage.createGraphics();
        this.cacheGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.cacheGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

                    doAdditiveBlend(x, y, (int)(cacheRed * (cacheAlpha/255F)), (int)(cacheGreen * (cacheAlpha/255F)), (int)(cacheBlue * (cacheAlpha/255F)));

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

    public void doAdditiveBlend(int x, int y, int addR, int addG, int addB){
        doAdditiveBlend(this, x, y, addR, addG, addB);
    }

    public static void doAdditiveBlend(IPixelData pixelData, int x, int y, int addR, int addG, int addB){
        int red = Utils.clamp(pixelData.getRed(x, y) + addR, 0, 255);
        int green = Utils.clamp(pixelData.getGreen(x, y) + addG, 0, 255);
        int blue = Utils.clamp(pixelData.getBlue(x, y) + addB, 0, 255);
        int alpha = pixelData.getAlpha(x, y);
        pixelData.setARGB(x, y, alpha, red, green, blue);
    }

    @Override
    public void destroy() {
        super.destroy();
        cacheGraphics.dispose();
    }

    @Override
    public String getType() {
        return "ARGBY: Additive Composite";
    }
}