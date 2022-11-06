package drawingbot.image;

/**
 * Implement this interface to observe changes to an image
 */
public interface IPixelListener {

    void onPixelChanged(int x, int y);

}
