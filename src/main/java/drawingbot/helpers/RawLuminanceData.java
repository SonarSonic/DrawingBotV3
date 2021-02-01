package drawingbot.helpers;

import drawingbot.DrawingBotV3;

import java.awt.image.BufferedImage;
import java.util.function.Function;

import static processing.core.PApplet.constrain;

/**represents pure raw brightness data, removing bloat to provide fastest possible speeds
 * it also keeps track of the average of all the data stored which allows for progress updates to be more frequent*/
public class RawLuminanceData extends PixelData {

    public int[] data;
    public float averageData;

    public RawLuminanceData(int width, int height){
        super(width, height);
        this.data = new int[width*height];
    }

    public static RawLuminanceData createBrightnessData(BufferedImage image){
        return createData(image, rgba -> (int)DrawingBotV3.INSTANCE.brightness(rgba));
    }

    public static RawLuminanceData createRedData(BufferedImage image){
        return createData(image, rgba -> (rgba >> 16) & 0xFF);
    }

    public static RawLuminanceData createGreenData(BufferedImage image){
        return createData(image, rgba -> (rgba >> 8) & 0xFF);
    }

    public static RawLuminanceData createBlueData(BufferedImage image){
        return createData(image, rgba -> rgba & 0xFF);
    }

    public static RawLuminanceData createData(BufferedImage image, Function<Integer, Integer> func){
        RawLuminanceData data = new RawLuminanceData(image.getWidth(), image.getHeight());
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                data.setBrightness(x, y, func.apply(image.getRGB(x, y)));
            }
        }
        return data;
    }

    public float getAverageBrightness(){
        return averageData / (width*height);
    }

    public int getBrightness(int x, int y){
        int loc = x + y*width;
        return data[loc];
    }

    public void setBrightness(int x, int y, int value){
        int loc = x + y*width;
        averageData -= data[loc]; //remove old brightness value
        data[loc] = value;
        averageData += data[loc]; //add new brightness value
    }

    /////

    public void brightenPixel(int x, int y, int adjust) {
        int newBrightness = constrain(getBrightness(x, y) + adjust,0,255);
        setBrightness(x, y, newBrightness);
    }

    public BufferedImage asBufferedImage(){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                image.setRGB(x, y, DrawingBotV3.INSTANCE.color(getBrightness(x, y)));
            }
        }
        return image;
    }
}
