package drawingbot.drawing;

import java.util.List;

public interface IDrawingSet<P extends IDrawingPen> {

    String getName();

    List<P> getPens();

}
