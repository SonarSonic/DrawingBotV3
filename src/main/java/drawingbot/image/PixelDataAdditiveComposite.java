package drawingbot.image;

import drawingbot.api.IPixelData;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.utils.Utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * A special version of {@link PixelDataARGBY} which has the ability to perform additive draw operations with less performance overhead, not intended for general use, used directly by {@link drawingbot.pfm.helpers.PFMRenderPipe}
 */
public class PixelDataAdditiveComposite extends PixelDataARGBY{

    private BufferedImage cacheImage;
    private final Graphics2D cacheGraphics;

    private final Rectangle2D cacheRect = new Rectangle2D.Double(0, 0, 0, 0);
    private final int[] cachePixelData = new int[1];
    private final int[] cacheTransparent = new int[1];
    private boolean isDrawing = false;

    public PixelDataAdditiveComposite(int width, int height) {
        super(width, height);
        BufferedImage cacheImage = ObservableWritableRaster.createObservableBufferedImage(width, height, (x, y) -> {
            if(!isDrawing){
                return;
            }
            if(this.cacheRect.isEmpty()){
                this.cacheRect.setRect(x, y, 1, 1);
            }else{
                this.cacheRect.add(x, y);
            }
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
        cacheRect.setRect(0, 0, 0, 0);
        isDrawing = true;
    }

    public void postDraw(BresenhamHelper.IPixelSetter callback){
        isDrawing = false;

        if(cacheRect.isEmpty()){
            return;
        }
        for(int x = (int) cacheRect.getMinX(); x <= cacheRect.getMaxX() && x < cacheImage.getWidth(); x++){
            for(int y = (int) cacheRect.getMinY(); y <= cacheRect.getMaxY() && y < cacheImage.getHeight(); y++){

                Object data = cacheImage.getRaster().getDataElements(x, y, cachePixelData);
                int argb = cacheImage.getColorModel().getRGB(data);
                int cacheAlpha = ImageTools.alpha(argb);

                if(cacheAlpha != 0){ // Check we've drawn something
                    int cacheRed = ImageTools.red(argb);
                    int cacheGreen = ImageTools.green(argb);
                    int cacheBlue = ImageTools.blue(argb);

                    doAdditiveBlend(x, y, (int)(cacheRed * (cacheAlpha/255F)), (int)(cacheGreen * (cacheAlpha/255F)), (int)(cacheBlue * (cacheAlpha/255F)));

                    if(callback != null){
                        callback.setPixel(x, y);
                    }
                    cacheImage.getRaster().setDataElements(x, y, cacheTransparent);
                }
            }
        }
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
        cacheImage = null;
        cacheGraphics.dispose();
    }

    @Override
    public String getType() {
        return "ARGBY: Additive Composite";
    }
}