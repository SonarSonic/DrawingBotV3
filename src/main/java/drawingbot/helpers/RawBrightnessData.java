package drawingbot.helpers;

import drawingbot.DrawingBotV3;

import java.awt.image.BufferedImage;

import static processing.core.PApplet.constrain;

/**represents pure raw brightness data, removing bloat to provide fastest possible speeds*/
public class RawBrightnessData {

    public int width;
    public int height;
    public int[] brightness;
    public float totalBrightness;

    public RawBrightnessData(BufferedImage image){
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.brightness = new int[width*height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                setBrightness(x, y, (int)DrawingBotV3.INSTANCE.brightness(image.getRGB(x, y)));
            }
        }
    }

    public float getAverageBrightness(){
        return totalBrightness / (width*height);
    }

    public int getBrightness(int x, int y){
        int loc = x + y*width;
        return brightness[loc];
    }

    public void setBrightness(int x, int y, int value){
        int loc = x + y*width;
        totalBrightness-=brightness[loc]; //remove old brightness value
        brightness[loc] = value;
        totalBrightness+=brightness[loc]; //add new brightness value
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
