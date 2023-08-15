package drawingbot.image;

import drawingbot.utils.Utils;

import java.awt.*;

/**represents raw pixel data, removing bloat to provide fastest possible speeds
 * it also keeps track of the average of all the data stored which allows for progress updates to be more frequent*/
public class RawData {

    public int width;
    public int height;
    public byte[][] data;
    public double averageData;
    public int pixelCount;

    public int min = 0;
    public int max = 255;

    private Shape softClip = null;

    public IDataListener listener;

    public RawData(int width, int height){
        this.width = width;
        this.height = height;
        this.data = new byte[width][height];
        this.pixelCount = width*height;
    }

    public void setBounds(int min, int max){
        this.min = min;
        this.max = max;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getAverage(){
        return averageData / pixelCount;
    }

    public int getData(int x, int y){
        return Byte.toUnsignedInt(data[x][y]);
    }

    public void setData(int x, int y, int value){
        int oldValue = getData(x, y);

        value = Utils.clamp(value, 0, max);


        data[x][y] = (byte)value;

        if(isWithinObservableRange(x, y)){
            averageData -= oldValue; //remove old value from average
            averageData += value; //add new value to average
        }

        if(listener != null){
            listener.onChange(x, y, oldValue, value);
        }
    }

    public void adjustData(int x, int y, int adjust) {
        int newValue = Utils.clamp(getData(x, y) + adjust,0, max);
        setData(x, y, newValue);
    }

    public void setData(int[][] data){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                setData(x, y, data[x][y]);
            }
        }
    }

    public void setData(byte[][] data){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                setData(x, y, Byte.toUnsignedInt(data[x][y]));
            }
        }
    }

    public void recalculateAverageData(){
        averageData = 0;
        pixelCount = 0;
        for(int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(isWithinObservableRange(x, y)){
                    averageData += getData(x, y);
                    pixelCount++;
                }
            }
        }
    }

    public boolean isWithinObservableRange(int x, int y){
        return softClip == null || softClip.contains(x, y);
    }

    public void setSoftClip(Shape softClip){
        this.softClip = softClip;
        this.recalculateAverageData();
    }

    public Shape getSoftClip(){
        return softClip;
    }

    public void destroy() {
        this.softClip = null;
        this.data = null;
    }

    public interface IDataListener{

        void onChange(int x, int y, int oldValue, int newValue);

    }
}