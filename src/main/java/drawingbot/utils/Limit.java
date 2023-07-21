package drawingbot.utils;

/**A class to check the upper and lower limits of a value */
public class Limit {

    public float min = Integer.MAX_VALUE;
    public float max = Integer.MIN_VALUE;

    public Limit() { }

    public void updateLimit(float value) {
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
    }
}
