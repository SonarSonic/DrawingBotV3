package drawingbot.api;

import drawingbot.geom.PathBuilder;
import drawingbot.geom.basic.IGeometry;

/**
 * Access editable image data via {@link IPlottingTask#getPixelData()} and unedited reference data {@link IPlottingTask#getReferencePixelData()}
 */
public interface IPlottingTask {

    /**
     * The {@link IPathFindingModule} should check this value during {@link IPathFindingModule#doProcess(IPlottingTask)}.
     * If the plotting task is finished the {@link IPathFindingModule} should end the process prematurely.
     * @return if the task has been finished by the user.
     */
    boolean isFinished();

    /**
     * Updates the progress bar in the user interface
     * @param progress the current progress
     * @param max the max progress
     */
    void updatePlottingProgress(double progress, double max);

    /**
     * Notifies the PlottingTask that the {@link IPathFindingModule} has finished
     */
    void finishProcess();

    IGeometry getLastGeometry();

    void addGeometry(IGeometry geometry);

    void addGeometry(IGeometry geometry, Integer penIndex, Integer rgba);

    PathBuilder getPathBuilder();

    /**
     * The pixel data the {@link IPathFindingModule} can alter while processing
     * This will be set to a copy of {@link #getReferencePixelData()} before the process begins
     * @return the pixels of the image , in ARGB format
     */
    IPixelData getPixelData();

    /**
     * The pixel data of the original image the {@link IPathFindingModule} should draw
     * @return the reference pixels, in ARGB format
     */
    IPixelData getReferencePixelData();

    /**
     * @return the number of pens in the current drawing set
     */
    int getTotalPens();


    /**
     * @return the current drawing set
     */
    IDrawingSet<?> getDrawingSet();

}
