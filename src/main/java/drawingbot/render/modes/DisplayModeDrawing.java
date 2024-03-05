package drawingbot.render.modes;

import drawingbot.api.ICanvas;
import drawingbot.api.IGeometryFilter;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.render.viewport.Viewport;
import drawingbot.utils.flags.Flags;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class DisplayModeDrawing extends DisplayModeBase {

    private boolean initialized;

    public DisplayModeDrawing(){}

    @MustBeInvokedByOverriders
    public void init(){
        initialized = true;
        canvasProperty().bind(Bindings.createObjectBinding(()->{
            if(displayedDrawing.get() != null){
                return displayedDrawing.get().getCanvas();
            }
            return fallbackCanvas.get();
        }, displayedDrawing, fallbackCanvas));

        geometryFilterProperty().addListener((observable, oldValue, newValue) -> {
            if(getViewport() != null){
                getViewport().getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
            }
        });

        displayedDrawingProperty().addListener((observable, oldValue, newValue) -> {
            if(getViewport() != null){
                getViewport().getRenderFlags().setFlag(Flags.CURRENT_DRAWING_CHANGED, true);
            }
        });

        displayedTaskProperty().addListener((observable, oldValue, newValue) -> {
            if(getViewport() != null){
                getViewport().getRenderFlags().setFlag(Flags.ACTIVE_TASK_CHANGED, true);
            }
        });
    }

    @Override
    public void activateDisplayMode(Viewport viewport) {
        super.activateDisplayMode(viewport);
        if(!initialized){
            init();
        }
    }


    ////////////////////////////////////////////////////////

    /**
     * The current status of the drawing renderer
     */
    private final StringProperty renderStatus = new SimpleStringProperty();

    public String getRenderStatus() {
        return renderStatus.get();
    }

    public StringProperty renderStatusProperty() {
        return renderStatus;
    }

    public void setRenderStatus(String renderStatus) {
        this.renderStatus.set(renderStatus);
    }

    ////////////////////////////////////////////////////////

    /**
     * The current progress of the drawing renderer
     */
    private final DoubleProperty renderProgress = new SimpleDoubleProperty();

    public double getRenderProgress() {
        return renderProgress.get();
    }

    public DoubleProperty renderProgressProperty() {
        return renderProgress;
    }

    public void setRenderProgress(double renderProgress) {
        this.renderProgress.set(renderProgress);
    }

    ////////////////////////////////////////////////////////

    /**
     * If no drawing/image is currently being rendered we will instead render this fallback canvas.
     *
     * Typically this will be the configured project drawing area, though it can be used to set alternative fallbacks
     */
    private final ObjectProperty<ICanvas> fallbackCanvas = new SimpleObjectProperty<>();

    public ICanvas getFallbackCanvas() {
        return fallbackCanvas.get();
    }

    public ObjectProperty<ICanvas> fallbackCanvasProperty() {
        return fallbackCanvas;
    }

    public void setFallbackCanvas(ICanvas fallbackCanvas) {
        this.fallbackCanvas.set(fallbackCanvas);
    }

    ////////////////////////////////////////////////////////

    /**
     * The PFMTask which is currently being rendered, if this is null then the displayedDrawing wil be rendered instead
     * When the PFMTask is rendered the tasks own {@link PFMTask#getTaskGeometryIterator()} will be used
     */
    private final ObjectProperty<PFMTask> displayedTask = new SimpleObjectProperty<>();

    public PFMTask getDisplayedTask() {
        return displayedTask.get();
    }

    public ObjectProperty<PFMTask> displayedTaskProperty() {
        return displayedTask;
    }

    public void setDisplayedTask(PFMTask displayedTask) {
        this.displayedTask.set(displayedTask);
    }

    ////////////////////////////////////////////////////////

    /**
     * The drawing to display when {@link #getDisplayedTask()}} is null
     * Typically this will be the most recent DrawingBotV3 drawing
     */
    private final ObjectProperty<PlottedDrawing> displayedDrawing = new SimpleObjectProperty<>();

    public PlottedDrawing getDisplayedDrawing() {
        return displayedDrawing.get();
    }

    public ObjectProperty<PlottedDrawing> displayedDrawingProperty() {
        return displayedDrawing;
    }

    public void setDisplayedDrawing(PlottedDrawing displayedDrawing) {
        this.displayedDrawing.set(displayedDrawing);
    }

    ////////////////////////////////////////////////////////

    /**
     * The geometry filter to apply to the geometries rendered, special Display Modes such as "Selected Pen" may choose to override this
     */
    private final ObjectProperty<IGeometryFilter> geometryFilter = new SimpleObjectProperty<>(IGeometryFilter.DEFAULT_VIEW_FILTER);

    public IGeometryFilter getGeometryFilter(){
        return geometryFilter.get();
    }

    public ObjectProperty<IGeometryFilter> geometryFilterProperty() {
        return geometryFilter;
    }

    public void setGeometryFilter(IGeometryFilter geometryFilter) {
        this.geometryFilter.set(geometryFilter);
    }

    ////////////////////////////////////////////////////////
}
