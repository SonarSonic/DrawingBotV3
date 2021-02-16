package drawingbot.api;

/**
 * Access editable image data via {@link IPlottingTask#getPixelData()} and unedited reference data {@link IPlottingTask#getReferencePixelData()}
 * Lines can be added via the {@link IPlottingTask#movePenDown()}, {@link IPlottingTask#movePenUp()}, {@link IPlottingTask#moveAbsolute(float, float)}, {@link IPlottingTask#addLine(float, float, float, float)}
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

    /**
     * @return if the pen is currently down
     */
    boolean isPenDown();

    /**
     * Moves the pen up, making any subsequent moves invisible, has no effect if the pen is already up
     */
    void movePenUp();

    /**
     * Moves the pen down, making any subsequent moves visible, has no effect if the pen is already down
     */
    void movePenDown();

    /**
     * Moves the pen to the given pixel coordinates, sub-pixel positioning can be used.
     * If the pen is down a line will be drawn if not the pen will simple move to the given position.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    void moveAbsolute(float x, float y);

    /**
     * A convenience method which moves the pen up, moves it to the first point, lowers the pen and draws a line
     * Useful for converting other scripts to {@link IPathFindingModule} format which specify lines in this way
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     */
    void addLine(float x1, float y1, float x2, float y2);

    void beginShape(); //TODO MAKE WORK FOR ALL TYPES!

    void endShape();

    void addCurveVertex(float x1, float y1);

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
