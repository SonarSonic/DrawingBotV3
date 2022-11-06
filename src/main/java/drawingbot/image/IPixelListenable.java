package drawingbot.image;

public interface IPixelListenable {

    void addListener(IPixelListener listener);

    void removeListener(IPixelListener listener);

}
