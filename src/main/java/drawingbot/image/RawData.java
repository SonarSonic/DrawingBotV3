package drawingbot.image;

import drawingbot.utils.Utils;

/**represents raw pixel data, removing bloat to provide fastest possible speeds
 * it also keeps track of the average of all the data stored which allows for progress updates to be more frequent*/
public class RawData {

    public int width;
    public int height;
    public int[][] data;
    public double averageData;

    public int min = 0;
    public int max = 255;

    public RawData(int width, int height){
        this.width = width;
        this.height = height;
        this.data = new int[width][height];
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
        return data[x][y];
    }

    public void setData(int x, int y, int value){
        averageData -= data[x][y]; //remove old value from average
        data[x][y] = value;
        averageData += value; //add new value to average
    }

    public void adjustData(int x, int y, int adjust) {
        int newValue = Utils.clamp(getData(x, y) + adjust,0,255);
        setData(x, y, newValue);
    }
}