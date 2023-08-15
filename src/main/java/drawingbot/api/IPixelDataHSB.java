package drawingbot.api;

import drawingbot.api.IPixelData;

/**
 * Implemented on PixelData which support direct HSB modifications
 */
public interface IPixelDataHSB extends IPixelData {


    /**
     * @param type the hsb type 0 = Hue, 1 = Saturation, 2 = Brightness
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels hsb value of the given type 0 - 255
     */
    int getHSB(int type, int x, int y);

    /**
     * Convenience method to get the hue value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels hue value 0 - 255
     */
    default int getHue(int x, int y){
        return getHSB(0, x, y);
    }

    /**
     * Convenience method to get the saturation value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels saturation value 0 - 255
     */
    default int getSaturation(int x, int y){
        return getHSB(1, x, y);
    }

    /**
     * Convenience method to get the brightness value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels brightness value 0 - 255
     */
    default int getBrightness(int x, int y){
        return getHSB(2, x, y);
    }

    /**
     * @param type the hsb type 0 = Hue, 1 = Saturation, 2 = Brightness
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param value the value to set 0-255
     */
    void setHSB(int type, int x, int y, int value);

    /**
     * Convenience method to set the hue value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param hue the hue value to set 0-255
     */
    default void setHue(int x, int y, int hue){
        setHSB(0, x, y, hue);
    }

    /**
     * Convenience method to set the saturation value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param saturation the saturation value to set 0-255
     */
    default void setSaturation(int x, int y, int saturation){
        setHSB(1, x, y, saturation);
    }

    /**
     * Convenience method to set the brightness value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param brightness the brightness value to set 0-255
     */
    default void setBrightness(int x, int y, int brightness){
        setHSB(2, x, y, brightness);
    }

    /**
     * @param type the hsb type 0 = Hue, 1 = Saturation, 2 = Brightness
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param value the value to increase by 0-255
     */
    void adjustHSB(int type, int x, int y, int value);

    /**
     * Convenience method to increase the hue value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param hue the hue value to increase by 0-255
     */
    default void adjustHue(int x, int y, int hue){
        adjustHSB(0, x, y, hue);
    }

    /**
     * Convenience method to increase the saturation value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param saturation the saturation value to increase by 0-255
     */
    default void adjustSaturation(int x, int y, int saturation){
        adjustHSB(1, x, y, saturation);
    }

    /**
     * Convenience method to increase the brightness value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param brightness the brightness value to increase by 0-255
     */
    default void adjustBrightness(int x, int y, int brightness){
        adjustHSB(2, x, y, brightness);
    }

    /**
     * @param type the hsb type 0 = Hue, 1 = Saturation, 2 = Brightness
     * @return the average of the given hsb type for the entire image
     */
    double getAverageHSB(int type);

    /**
     * @return the average hue for the entire image
     */
    default double getAverageHue(){
        return getAverageHSB(0);
    }

    /**
     * @return the average saturation for the entire image
     */
    default double getAverageSaturation(){
        return getAverageHSB(1);
    }

    /**
     * @return the average brightness for the entire image
     */
    default double getAverageBrightness(){
        return getAverageHSB(2);
    }



}

