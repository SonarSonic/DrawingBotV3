package drawingbot.drawing;

import drawingbot.api.ICustomPen;
import drawingbot.api.IDrawingPen;

public abstract class CustomPen extends DrawingPen implements ICustomPen {

    public CustomPen() {
        super();
    }

    public CustomPen(IDrawingPen source) {
        super(source);
    }

    public CustomPen(String type, String name, int argb) {
        super(type, name, argb);
    }

    public CustomPen(String type, String name, int argb, int distributionWeight, float strokeSize) {
        super(type, name, argb, distributionWeight, strokeSize);
    }

    public abstract int getCustomARGB(int pfmARGB);
}
