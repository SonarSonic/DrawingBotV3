package drawingbot.image;

public abstract class PixelTargetCache {

    public void updateNextDarkestPixel(int[] dst){
        int[] nextPixel = getNextDarkestPixel(true);
        dst[0] = nextPixel[0];
        dst[1] = nextPixel[1];
    }

    public abstract int[] getNextDarkestPixel(boolean remove);

    public abstract void destroy();
}
