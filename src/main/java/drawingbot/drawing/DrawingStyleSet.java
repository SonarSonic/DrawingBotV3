package drawingbot.drawing;

import java.util.ArrayList;
import java.util.List;

public class DrawingStyleSet {

    public List<DrawingStyle> styles = new ArrayList<>();

    public DrawingStyleSet() {}

    public DrawingStyleSet(List<DrawingStyle> styles) {
        this.styles = styles;
    }

    @Override
    public String toString() {
        return styles.toString();
    }
}
