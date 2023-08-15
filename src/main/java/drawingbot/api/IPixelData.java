package drawingbot.api;

import drawingbot.image.ImageTools;
import drawingbot.image.RawData;
import drawingbot.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Represents an image file as raw pixel data, which you can read from / write to.
 * There are lots of convenience methods for providing individual values for alpha, red, green, blue, hue, saturation, luminance
 * It is recommended to change individual values rather than using ARGB methods.
 *
 * It's also specifically optimised to provide averages efficiently, enabling more frequent progress bar updates.
 *
 * For efficiently the given variables will not be checked, this is the responsibility of the {@link IPFM}
 * Pass only x, y coordinates within the bounds of the image
 * Pass only rgb/hsb variables between 0 - 255
 */
public interface IPixelData {

    /**
     * The width of the image this pixel data represents
     */
    int getWidth();

    /**
     * The height of the image this pixel data represents
     */
    int getHeight();

    /**
     * Convenience method to get the value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels ARGB value
     */
    int getARGB(int x, int y);

    /**
     * Convenience method to set the value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param argb the argb value to set the pixel to
     */
    void setARGB(int x, int y, int argb);

    /**
     * Convenience method to set the value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param a the alpha value to set the pixel to
     * @param r the red value to set the pixel to
     * @param g the green value to set the pixel to
     * @param b the blue value to set the pixel to
     */
    void setARGB(int x, int y, int a, int r, int g, int b);

    /**
     * Gets the value of the given channel - 0 = Alpha, 1 = Red, 2 = Green, 3 = Blue
     * @param channel the channel to get
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels value of the given channel 0-255
     */
    int getChannel(int channel, int x, int y);

    /**
     * Convenience method to get the alpha value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels alpha value 0 - 255
     */
    default int getAlpha(int x, int y){
        return getChannel(0, x, y);
    }

    /**
     * Convenience method to get the red value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels red value 0 - 255
     */
    default int getRed(int x, int y){
        return getChannel(1, x, y);
    }

    /**
     * Convenience method to get the green value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels green value 0 - 255
     */
    default int getGreen(int x, int y){
        return getChannel(2, x, y);
    }

    /**
     * Convenience method to get the blue value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels blue value 0 - 255
     */
    default int getBlue(int x, int y){
        return getChannel(3, x, y);
    }

    /**
     * Sets the value of the given channel - 0 = Alpha, 1 = Red, 2 = Green, 3 = Blue
     * @param channel the channel to set
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param value the value to increase by 0-255
     */
    void setChannel(int channel, int x, int y, int value);

    /**
     * Convenience method to set the alpha value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param alpha the alpha value to set 0-255
     */
    default void setAlpha(int x, int y, int alpha){
        setChannel(0, x, y, alpha);
    }

    /**
     * Convenience method to set the red value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param red the red value to set 0-255
     */
    default void setRed(int x, int y, int red){
        setChannel(1, x, y, red);
    }

    /**
     * Convenience method to set the green value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param green the green value to set 0-255
     */
    default void setGreen(int x, int y, int green){
        setChannel(2, x, y, green);
    }

    /**
     * Convenience method to set the blue value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param blue the blue value to set 0-255
     */
    default void setBlue(int x, int y, int blue){
        setChannel(3, x, y, blue);
    }

    /**
     * Adjusts the value of the given channel - 0 = Alpha, 1 = Red, 2 = Green, 3 = Blue
     * @param channel the channel to adjust
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param value the value to increase by 0-255
     */
    void adjustChannel(int channel, int x, int y, int value);

    /**
     * Convenience method to increase the alpha value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param alpha the alpha value to increase by 0-255
     */
    default void adjustAlpha(int x, int y, int alpha){
        adjustChannel(0, x, y, alpha);
    }

    /**
     * Convenience method to increase the red value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param red the red value to increase by 0-255
     */
    default void adjustRed(int x, int y, int red){
        adjustChannel(1, x, y, red);
    }

    /**
     * Convenience method to increase the green value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param green the green value to increase by 0-255
     */
    default void adjustGreen(int x, int y, int green){
        adjustChannel(2, x, y, green);
    }

    /**
     * Convenience method to increase the blue value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param blue the blue value to increase by 0-255
     */
    default void adjustBlue(int x, int y, int blue){
        adjustChannel(3, x, y, blue);
    }

    /**
     * @return the average channel value for the entire image - 0 = Alpha, 1 = Red, 2 = Green, 3 = Blue
     */
    double getAverageChannel(int channel);

    /**
     * @return the average alpha value for the entire image
     */
    default double getAverageAlpha(){
        return getAverageChannel(0);
    }

    /**
     * @return the average red value for the entire image
     */
    default double getAverageRed(){
        return getAverageChannel(1);
    }

    /**
     * @return the average green value for the entire image
     */
    default double getAverageGreen(){
        return getAverageChannel(2);
    }

    /**
     * @return the average blue value for the entire image
     */
    default double getAverageBlue(){
        return getAverageChannel(3);
    }

    /**
     * Convenience method to get the perceived luminance value of a single pixel
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels perceived luminanc value 0 - 255
     */
    int getLuminance(int x, int y);

    /**
     * Allows for the support of more accurate luminance values if the pixel data supports it
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the pixels luminance value from 0 - 1
     */
    double getLuminanceD(int x, int y);

    /**
     * Convenience method to set the perceived luminance value of a single pixel, this will make the pixel grayscale.
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param luminance the brightness value to set 0-255
     */
    void setLuminance(int x, int y, int luminance);

    /**
     * Convenience method to increase the perceived luminance value of a single pixel, this will make the pixel grayscale.
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @param luminance the perceived luminance value to increase by 0-255
     */
    void adjustLuminance(int x, int y, int luminance);

    /**
     * @return the average perceived luminance for the entire image
     */
    double getAverageLuminance();

    /**
     * @return the ARGB value of transparent pixels, default = a=0, r=0, g=0, b=0
     */
    int getTransparentARGB();

    /**
     * sets the transparent ARGB value, this is important for brightness orientated PFMs, not to called directly by a {@link IPFM}
     * instead use {@link IPFMImage#getTransparentARGB()}
     */
    void setTransparentARGB(int argb);

    default void setSoftClip(Shape softClip){

    }

    default Shape getSoftClip(){
        return null;
    }

    default void loadData(IPixelData source){
        ImageTools.copy(source, this);
    }

    default void loadData(BufferedImage source){
        ImageTools.copyToPixelData(source, this);
    }

    default BufferedImage asBufferedImage(){
        return ImageTools.getBufferedImage(this);
    }

    /** Clears all the data in this IPixelData **/
    void destroy();

    /**
     * Should be implemented on all PixelData which should be compatible with the PixelTargetCache
     */
    default RawData getRawLuminanceData(){
        return null;
    }

    default void attachLuminanceDataListener(RawData.IDataListener listener){
        RawData data = getRawLuminanceData();
        assert data != null;
        data.listener = listener;
    }

    default int clampX(int x){
        return Utils.clamp(x, 0, getWidth()-1);
    }

    default int clampY(int y){
        return Utils.clamp(y, 0, getHeight()-1);
    }

    default boolean withinX(int x){
        return Utils.within(x, 0, getWidth()-1);
    }

    default boolean withinY(int y){
        return Utils.within(y, 0, getHeight()-1);
    }

    default boolean withinXY(int x, int y){
        return withinX(x) && withinY(y);
    }

}
