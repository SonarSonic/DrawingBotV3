package drawingbot.api;

public interface ISpecialPenHandler {
    /**
     * @param drawingPen
     * @param pfmARGB    the line's colour as configured by the PFM, won't be null
     * @return returns the custom ARGB value to use when rendering a pen
     */
    default int getCustomARGB(IDrawingPen drawingPen, int pfmARGB){
        return drawingPen.getARGB();
    }

    /**
     * @return if this Custom Pen's geometries can be linked regardless of their original sampled colour
     */
    default boolean canOptimisePenPaths(IDrawingPen drawingPen){
        return false;
    }

}
