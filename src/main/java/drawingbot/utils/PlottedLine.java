package drawingbot.utils;///////////////////////////////////////////////////////////////////////////////////////////////////////
// A class to describe one line segment
//
// Because of a bug in processing.org the MULTIPLY blendMode does not take into account the alpha of
// either source or destination.  If this gets corrected, tweaks to the stroke alpha might be more
// representative of a Copic marker.  Right now it over emphasizes the darkening when overlaps
// of the same pen occur.

public class PlottedLine {

    public int pen_number;
    public boolean pen_down, pen_continuation;
    public float x1, y1, x2, y2;

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

///////////////////////////////////////////////////////////////////////////////////////////////////////