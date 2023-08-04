package drawingbot.api;

public interface ICustomPen extends IDrawingPen{
    /**
     * @param pfmARGB the line's colour as configured by the PFM, won't be null
     * @return returns the custom ARGB value to use when rendering a pen
     */
    default int getCustomARGB(int pfmARGB){
        return getARGB();
    }

    /**
     * @return if this Custom Pen's geometries can be linked regardless of their original sampled colour
     */
    default boolean canOptimisePenPaths(){
        return false;
    }

}
