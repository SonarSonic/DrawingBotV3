package drawingbot.plotting;

public class PlottedLine {

    public int pen_number;
    public boolean pen_down, pen_continuation;
    public float x1, y1, x2, y2;
    public int rgba = -1;

    public PlottedLine(boolean pen_down, int pen_number, float x1, float y1, float x2, float y2) {
        this.pen_down = pen_down;
        this.pen_continuation = false;
        this.pen_number = pen_number;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}