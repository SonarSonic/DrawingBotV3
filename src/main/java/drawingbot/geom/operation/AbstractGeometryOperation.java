package drawingbot.geom.operation;

import drawingbot.api.IProgressCallback;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.utils.ProgressCallback;

/**
 * Geometry operations should be non-destructive, new geometries should be created if needed
 */
public abstract class AbstractGeometryOperation implements IProgressCallback {

    public boolean forExport = true;
    public String message = "";
    public String title = "";
    public float progress;
    public IProgressCallback progressCallback = this;


    public AbstractGeometryOperation(){}

    /**
     * This method will return the original plotted drawing if the operation is destructive, or return a new one if it's non-destructive
     */
    public abstract PlottedDrawing run(PlottedDrawing originalDrawing);

    public abstract boolean isDestructive();

    @Override
    public void updateTitle(String title) {
        this.title = title;
    }

    @Override
    public void updateProgress(float progress, float max) {
        this.progress = progress/max;
    }

    @Override
    public void updateMessage(String message) {
        this.message = message;

    }

    public PlottedDrawing createPlottedDrawing(PlottedDrawing reference){
        PlottedDrawing newDrawing = new PlottedDrawing(reference.getDefaultGroup().drawingSet, reference.getDefaultGroup().pfmFactory);
        newDrawing.copyBase(reference);
        return newDrawing;
    }
}
