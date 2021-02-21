package drawingbot.api;

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
    void updateProgess(float progress, float max);

    /**
     * Notifies the PlottingTask that the {@link IPathFindingModule} has finished
     */
    void finishProcess();

    void openPath();

    void closePath();

    void addToPath(float x1, float y1);

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
     * @param useARGB true if the pfm wishes to set the colours of lines itself, false if drawing pen settings should be used, this can be changed mid plot.
     */
    void useCustomARGB(boolean useARGB);

    /**
     * @param argb the colour all subsequent geometries will use, requires {@link IPlottingTask#useCustomARGB(boolean)} to be true
     */
    void setCustomARGB(int argb);

    /**
     * Sets the active pen from the current drawing set by index
     * @param index the index of the pen
     */
    void setActivePen(int index);

    /**
     * @return the index of the pen in the drawing set
     */
    int getActivePen();

    /**
     * @return the number of pens in the current drawing set
     */
    int getTotalPens();

    /**
     * @return the current drawing pen
     */
    IDrawingPen getDrawingPen();

    /**
     * @return the current drawing set
     */
    IDrawingSet<?> getDrawingSet();

}
