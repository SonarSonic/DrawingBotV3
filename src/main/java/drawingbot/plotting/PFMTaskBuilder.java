package drawingbot.plotting;

import drawingbot.api.Hooks;
import drawingbot.api.ICanvas;
import drawingbot.drawing.DrawingSets;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Handles the construction of the {@link PFMTask}
 * Making it easier for custom implementations of {@link PFMTask} to be used, and allowing easier configuration
 */
public class PFMTaskBuilder {

    public final DBTaskContext context;

    // PFM Configuration \\
    public final PFMFactory<?> pfmFactory;
    public List<GenericSetting<?, ?>> pfmSettings;

    // Drawing Configuration \\\
    public ICanvas canvas;
    public DrawingSets drawingSets;
    public ObservableDrawingSet activeSet;

    public PlottedDrawing drawing;

    // Image Data \\
    public ImageFilterSettings imageSettings;
    public FilteredImageData imageData;
    public boolean useImageCanvas = true;

    /** If the tasks lifecycle is controlled by another task */
    public boolean isSubTask;

    /** Set to true if you wish to bypass any external custom task suppliers / setup, e.g. if using a PFM inside another PFM and not in an external task this should be set to true */
    public boolean useInternalBuilder;

    // Special \\
    public Consumer<PFMTaskBuilder> preSetup;
    public Consumer<PFMTask> postSetup;
    public Function<PFMTaskBuilder, PFMTask> customTaskSupplier;

    public PFMTaskBuilder(DBTaskContext context, PFMFactory<?> factory, List<GenericSetting<?, ?>> settings, ICanvas canvas, DrawingSets drawingSets, ObservableDrawingSet activeSet, ImageFilterSettings imageSettings, FilteredImageData imageData, boolean isSubTask){
        this.context = context;
        this.pfmFactory = factory;
        this.canvas = canvas;
        this.pfmSettings = settings;
        this.drawingSets = drawingSets;
        this.activeSet = activeSet;
        this.imageSettings = imageSettings;
        this.imageData = imageData;
        this.isSubTask = isSubTask;
    }

    //// Helper Methods \\\\

    public static PFMTaskBuilder create(DBTaskContext context){
        return create(context, context.project().getPFMFactory());
    }

    public static PFMTaskBuilder create(DBTaskContext context, PFMFactory<?> factory){
        return new PFMTaskBuilder(context, factory, context.project().getPFMSettings(factory), context.project().getDrawingArea(), context.project().getDrawingSets(), context.project().getActiveDrawingSet(), context.project().getImageSettings(), context.project().getOpenImage(), false);
    }

    public static PFMTaskBuilder create(DBTaskContext context, PFMFactory<?> factory, List<GenericSetting<?, ?>> settings, ICanvas canvas, DrawingSets drawingSets, ObservableDrawingSet activeSet, ImageFilterSettings imageSettings, FilteredImageData imageData, boolean isSubTask){
        return new PFMTaskBuilder(context, factory, settings, canvas, drawingSets, activeSet, imageSettings, imageData, isSubTask);
    }

    //// PFM Task Creation \\\\

    public PFMTask createPFMTask(){
        if(imageData != null && useImageCanvas){
            imageData.updateAll(imageSettings);
            canvas = imageData.getDestCanvas();
        }

        if(pfmSettings == null){
            pfmSettings = context.project().getPFMSettings(pfmFactory);
        }

        if(!useInternalBuilder){
            Hooks.runHook(Hooks.NEW_PFM_TASK_BUILDER, this);
        }

        if(preSetup != null){
            preSetup.accept(this);
        }

        //Create the drawing now all the settings have been applied properly
        drawing = new PlottedDrawing(canvas, drawingSets);

        PFMTask task;
        if(!useInternalBuilder && customTaskSupplier != null){
            task = customTaskSupplier.apply(this);
        }else{
            task = createPFMTaskInternal();
        }

        if(!useInternalBuilder) {
            Object[] hookReturn = Hooks.runHook(Hooks.NEW_PLOTTING_TASK, task);
            task = (PFMTask) hookReturn[0];
        }

        task.isSubTask = isSubTask;

        if(postSetup != null){
            postSetup.accept(task);
        }

        return task;
    }

    public PFMTask createPFMTaskInternal(){
        if(!pfmFactory.isGenerativePFM()){
            return new PFMTaskImage(this);
        }else{
            return new PFMTask(this);
        }
    }

    //// Setters / Getters \\\\

    public DBTaskContext context() {
        return context;
    }

    public PFMTaskBuilder setPFMSettings(List<GenericSetting<?, ?>> pfmSettings) {
        this.pfmSettings = pfmSettings;
        return this;
    }

    public PFMTaskBuilder setCanvas(ICanvas canvas) {
        this.canvas = canvas;
        return this;
    }

    public PFMTaskBuilder setDrawingSets(DrawingSets drawingSets) {
        this.drawingSets = drawingSets;
        return this;
    }

    public PFMTaskBuilder setActiveSet(ObservableDrawingSet activeSet) {
        this.activeSet = activeSet;
        return this;
    }

    public PFMTaskBuilder setImageFilterSettings(ImageFilterSettings imageSettings) {
        this.imageSettings = imageSettings;
        return this;
    }

    public PFMTaskBuilder setImageData(FilteredImageData imageData) {
        this.imageData = imageData;
        return this;
    }

    public PFMTaskBuilder setUseImageCanvas(boolean useImageCanvas) {
        this.useImageCanvas = useImageCanvas;
        return this;
    }

    public PFMTaskBuilder setSubTask(boolean isSubTask) {
        this.isSubTask = isSubTask;
        return this;
    }

    public PFMTaskBuilder setUseInternalBuilder(boolean useInternalBuilder) {
        this.useInternalBuilder = useInternalBuilder;
        return this;
    }

    public PFMTaskBuilder setCustomTaskSupplier(Function<PFMTaskBuilder, PFMTask> customTaskSupplier) {
        this.customTaskSupplier = customTaskSupplier;
        return this;
    }

}
