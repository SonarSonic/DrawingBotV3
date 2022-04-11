package drawingbot.geom.operation;

import drawingbot.api.IProgressCallback;
import drawingbot.plotting.PlottedDrawing;

/**
 * Geometry operations should be non-destructive, new geometries should be created if needed
 */
public abstract class AbstractGeometryOperation implements IProgressCallback {

    public boolean forExport = true;
    public String message = "";
    public String title = "";
    public double progress;
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
    public void updateProgress(double progress, double max) {
        this.progress = progress/max;
    }

    @Override
    public void updateMessage(String message) {
        this.message = message;

    }

    public PlottedDrawing createPlottedDrawing(PlottedDrawing reference){
        return reference.copyBase();
    }
}
