package drawingbot.utils;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// A class to check the upper and lower limits of a value
public class Limit {
    public float min = Integer.MAX_VALUE;
    public float max = Integer.MIN_VALUE;

    public Limit() { }

    public void update_limit(float value_) {
        if (value_ < min) { min = value_; }
        if (value_ > max) { max = value_; }
    }
}
