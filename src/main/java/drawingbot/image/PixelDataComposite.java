package drawingbot.image;

import drawingbot.api.IPixelData;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This pixel data is designed as a workaround for the slow speed used by using CompositeContexts in AWT.
 * It allows you to overlay two different images (pixel data) and allow a specialist composite function which will cause minimal overhead.
 *
 * Primarily used to allow an "additive" style of blending for SketchPFMs, to keep in line with older versions.
 * @param <B> the {@link IPixelData} type of the background
 * @param <F> the {@link IPixelData} type of the foreground
 */
public class PixelDataComposite<B extends IPixelData & IPixelListenable, F extends IPixelData & IPixelListenable> extends PixelDataARGBY implements IPixelListenable {

    public B background;
    public F foreground;
    private boolean blending = false;
    private ICompositeFunction compositeFunction;

    private final int[] CACHE_BACKGROUND = new int[4];
    private final int[] CACHE_FOREGROUND = new int[4];
    private final int[] CACHE_RESULT = new int[4];

    public PixelDataComposite(B background, F foreground) {
        super(background.getWidth(), background.getHeight());

        assert background.getWidth() == foreground.getWidth();
        assert background.getHeight() == foreground.getHeight();

        this.background = background;
        this.foreground = foreground;

        this.background.addListener(this::updatePixel);
        this.foreground.addListener(this::updatePixel);
    }

    @Override
    public void loadData(IPixelData source) {
        ImageTools.copy(source, background);
        ImageTools.copy(source, this);
    }

    @Override
    public void loadData(BufferedImage source) {
        ImageTools.copyToPixelData(source, background);
        ImageTools.copyToPixelData(source, this);
    }

    public void updatePixel(int x, int y){
        if(!blending || compositeFunction == null){
            return;
        }

        int argbBackground = background.getARGB(x, y);
        int argbForeground = foreground.getARGB(x, y);

        ImageTools.getColourIntsFromARGB(argbBackground, CACHE_BACKGROUND);
        ImageTools.getColourIntsFromARGB(argbForeground, CACHE_FOREGROUND);

        compositeFunction.composite(CACHE_FOREGROUND, CACHE_BACKGROUND, CACHE_RESULT);

        setARGB(x, y, CACHE_RESULT[0], CACHE_RESULT[1], CACHE_RESULT[2], CACHE_RESULT[3]);
    }

    public void enableBlending(ICompositeFunction compositeFunction){
        this.compositeFunction = compositeFunction;
        blending = true;
    }

    public void disableBlending(){
        blending = false;
    }

    public interface ICompositeFunction{

        void composite(int[] foreground, int[] background, int[] result);

    }

    @Override
    public void setSoftClip(Shape softClip) {
        super.setSoftClip(softClip);
        background.setSoftClip(softClip);
        foreground.setSoftClip(softClip);
    }

    @Override
    public void destroy() {
        super.destroy();
        background.destroy();
        foreground.destroy();
    }

    @Override
    public String getType() {
        return "Composite: B %s F %s".formatted(background.getType(), foreground.getType());
    }
}