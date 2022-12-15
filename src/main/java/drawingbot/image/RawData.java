package drawingbot.image;

import drawingbot.utils.Utils;

/**represents raw pixel data, removing bloat to provide fastest possible speeds
 * it also keeps track of the average of all the data stored which allows for progress updates to be more frequent*/
public class RawData {

    public int width;
    public int height;
    public byte[][] data;
    public double averageData;

    public int min = 0;
    public int max = 255;

    public IDataListener listener;

    public RawData(int width, int height){
        this.width = width;
        this.height = height;
        this.data = new byte[width][height];
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
        return averageData / (width*height);
    }

    public int getData(int x, int y){
        return Byte.toUnsignedInt(data[x][y]);
    }

    public void setData(int x, int y, int value){
        int oldValue = getData(x, y);

        value = Utils.clamp(value, 0, max);
        averageData -= oldValue; //remove old value from average
        data[x][y] = (byte)value;
        averageData += value; //add new value to average

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

    public interface IDataListener{

        void onChange(int x, int y, int oldValue, int newValue);

    }
}