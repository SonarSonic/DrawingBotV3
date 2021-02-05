package drawingbot.image;

public class PixelData {

    public int width;
    public int height;

    public PixelData(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int asIndex(int x, int y){
        return x + y*width;
    }

    public int[] asPoint(int index){
        int x = index % width;
        int y = (index - x) / width;
        return new int[]{x, y};
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
