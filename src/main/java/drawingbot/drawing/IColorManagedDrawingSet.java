package drawingbot.drawing;

public interface IColorManagedDrawingSet {

     ColorSeparationHandler getColorSeparationHandler();

     ColorSeparationSettings getColorSeparationSettings();

     void setColorSeparation(ColorSeparationHandler handler, ColorSeparationSettings settings);

}
