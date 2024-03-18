package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.api.IPFM;
import drawingbot.files.json.presets.PresetPFMSettingsManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.Register;
import drawingbot.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PFMTask extends DBTask<PlottedDrawing> implements ISpecialListenable<PFMTask.Listener> {

    public final PlottedDrawing drawing;
    public final ObservableDrawingSet refPenSet;

    public PFMFactory<?> pfmFactory;
    public List<GenericSetting<?, ?>> pfmSettings;

    // STATUS \\
    public EnumTaskStage stage = EnumTaskStage.START;
    public LazyTimer taskTimer = new LazyTimer();
    public List<String> comments = new ArrayList<>();

    // PATH FINDING \\
    public IPFM pfm;
    public boolean finishEarly = false;
    public PlottingTools tools;
    public boolean isSubTask = false;

    // RENDERING \\\
    public boolean skipReRender = false;

    // SPECIAL \\
    public boolean useLowQuality = false;
    public int parallelPlots = 3;
    public boolean enablePlottingResolution = true;

    /**
     * Helper method to create the PFMTask from a builder rather than individual properties
     * @param builder the {@link PFMTaskBuilder} which generated this PFM
     */
    public PFMTask(PFMTaskBuilder builder){
        this(builder.context, builder.drawing, builder.pfmFactory, builder.activeSet, builder.pfmSettings);
    }

    /**
     * @param context the task context used to generate this {@link PFMTask}
     * @param drawing the {@link PlottedDrawing} that the {@link IPFM} will output into, it should use a {@link drawingbot.drawing.DrawingSets} which contains the 'refPenSet'
     * @param pfmFactory the {@link PFMFactory} which will provide the {@link IPFM} instance for the task to run
     * @param refPenSet the {@link ObservableDrawingSet} which should be considered the initial drawing set from the ones available in the {@link drawingbot.drawing.DrawingSets}
     * @param pfmSettings the list of {@link GenericSetting} which will be applied to the {@link IPFM} instance during the setup process
     */
    public PFMTask(DBTaskContext context, PlottedDrawing drawing, PFMFactory<?> pfmFactory, ObservableDrawingSet refPenSet, List<GenericSetting<?, ?>> pfmSettings){
        super(context);
        updateTitle("Plotting Image (" + pfmFactory.getRegistryName() + ")");
        this.refPenSet = refPenSet;
        this.pfmSettings = pfmSettings;
        this.pfmFactory = pfmFactory;
        this.drawing = drawing;
        this.drawing.setMetadata(Register.INSTANCE.SETTINGS_JSON, PresetPFMSettingsManager.getPFMPresetJson(pfmFactory, pfmSettings));
        this.tools = new PlottingTools(drawing, drawing.newPlottedGroup(refPenSet, pfmFactory));
        this.tools.pfmTask = this;
        this.tools.progressCallback = this;
        this.updateProgressInstantly = false;
    }

    /**
     * @return the instance of the {@link IPFM} which is currently being used, could be null if the stage ({@link EnumTaskStage}) hasn't begun PRE_PROCESSING.
     */
    public IPFM pfm(){
        return pfm;
    }

    /**
     * Handles the actual running of the {@link PFMTask} including the setup of resources required for the {@link IPFM} and the {@link drawingbot.api.IPlottingTools}, it should check the current {@link EnumTaskStage} and only run things required of that stage.
     * @return false if the task failed during the current stage, true if the stage successfully completed
     */
    public boolean doTask(){
        switch (stage){
            case PRE_PROCESSING -> {
                finishEarly = false;

                //Create the instance of the IPFM which will be used
                DrawingBotV3.logger.fine("PFM - Create Instance");
                pfm = pfmFactory.instance();

                //Sets the IPlottingTools for the PFM to use, must be completed before the PFM instance is used
                DrawingBotV3.logger.fine("PFM - Add Plotting Tools");
                pfm.setPlottingTools(tools);

                //Applies the settings to the PFM, this could include configuration of the tools e.g. Random Seeds
                DrawingBotV3.logger.fine("PFM - Apply Settings");
                sendListenerEvent(l -> l.prePFMSettingsApplied(this, pfmSettings, pfm));
                GenericSetting.applySettingsToInstance(pfmSettings, pfm);
                pfm.onSettingsApplied();
                sendListenerEvent(l -> l.postPFMSettingsApplied(this, pfmSettings, pfm));

                //Sets the plotting transform which is used to convert from the PFMs plotting resolution to the resolution of the ICanvas
                if(pfm.getPlottingResolution() != 1 && enablePlottingResolution){
                    tools.plottingTransform = AffineTransform.getScaleInstance(1D / pfm.getPlottingResolution(), 1D / pfm.getPlottingResolution());
                }

                if(!isSubTask && drawing.getMetadata(Register.INSTANCE.SOFT_CLIP_SHAPE) != null){
                    DrawingBotV3.logger.fine("PFM - Using Soft Clipping");
                    tools.setSoftClip(drawing.getMetadata(Register.INSTANCE.SOFT_CLIP_SHAPE), pfmFactory);
                }

                if(!isSubTask && drawing.getMetadata(Register.INSTANCE.CLIPPING_SHAPE) != null){
                    DrawingBotV3.logger.fine( "PFM - Using Clipping Shape");
                    tools.setClippingShape(drawing.getMetadata(Register.INSTANCE.CLIPPING_SHAPE));
                }

                if(tools.getClippingShape() == null && tools.getSoftClip() == null){
                    switch (drawing.canvas.getClippingMode()){
                        case NONE -> {}
                        case DRAWING -> {
                            tools.setClippingShape(new Rectangle2D.Double(0, 0, drawing.canvas.getScaledDrawingWidth(), drawing.canvas.getScaledDrawingHeight()));
                        }
                        case PAGE -> {
                            tools.setClippingShape(new Rectangle2D.Double(-drawing.canvas.getScaledDrawingOffsetX(), -drawing.canvas.getScaledDrawingOffsetY(), drawing.canvas.getScaledWidth(), drawing.canvas.getScaledHeight()));                        }
                    }
                }

                //PFMs which utilise images/vectors as inputs should initialize their images here once the IPlottingTools have been setup but before the final setup of the PFM
                preProcessImages();

                tools.currentGroup.setPFMFactory(pfmFactory);

                //Finally we call the setup method on the PFM, now the IPlottingTools have been setup and the images generated
                updateMessage("Setup");
                DrawingBotV3.logger.fine( "PFM - Setup");
                sendListenerEvent(l -> l.preSetupPFM(this, pfm));
                pfm.setup();
                sendListenerEvent(l -> l.postSetupPFM(this, pfm));
            }
            case DO_PROCESS -> {
                if(!useLowQuality){
                    //Run the PFM, this method will only return once all processing has been completed
                    pfm.run();
                }else{
                    /*
                     * Experimental Feature: to draw with multiple sketch PFMs at the same time, currently broken due to PFMs not being thread-safe
                     * Would need to be re-implemented directly inside the SketchPFM
                     */
                    final CountDownLatch latch = new CountDownLatch(parallelPlots);

                    ExecutorService newService = Executors.newFixedThreadPool(parallelPlots, r -> {
                        Thread t = new Thread(r, "DrawingBotV3 - Parallel Plotting Service v2");
                        t.setDaemon(true);
                        return t;
                    });

                    for(int i = 0; i < parallelPlots; i ++){
                        newService.submit(() -> {
                            pfm.run();
                            latch.countDown();
                        });
                    }

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        return false;
                    } finally {
                        newService.shutdown();
                    }
                }
            }
            case POST_PROCESSING -> {
                pfm.postProcess();
                postProcessImages();

                updateMessage("Updating Pen Distribution");
                drawing.updatePenDistribution();
            }
            case FINISH -> {
                updateMessage("Finished - Elapsed Time: " + taskTimer.getElapsedTimeFormatted());
                updateProgress(1, 1);
            }
        }
        return true;
    }

    @Override
    public final PlottedDrawing call() {

        if(!isSubTask){
            Platform.runLater(() -> {

                //Setup the PFMTask so it is visible in the viewport
                context.taskManager.setActiveTask(this);
                context.taskManager.setRenderedTask(this);
                context.taskManager.setCurrentDrawing(null);

                //only update the distribution type the first time the PFM is changed, also only trigger the update when Start Plotting is hit again, so the current drawing doesn't get re-rendered
                //if(drawingManager instanceof DrawingBotV3){ //TODO FIXME?
                if(refPenSet.colorHandler.get().isDefault()){
                    if(context.project.getPFMSettings().getNextDistributionType() != null){
                        refPenSet.distributionType.set(context.project.getPFMSettings().getNextDistributionType());
                        context.project.getPFMSettings().setNextDistributionType(null);
                    }
                }else{
                    refPenSet.distributionType.set(EnumDistributionType.getRecommendedType(refPenSet, pfmFactory));
                }
                //}
            });
        }


        //Start the task timer
        taskTimer.start();
        logStart();

        //Loop through the various task stages and run them
        for(EnumTaskStage stage : EnumTaskStage.values()){

            //Check the task hasn't been cancelled
            if(isCancelled()){
                break;
            }

            this.stage = stage;
            DrawingBotV3.logger.finer("Stage: " + stage.toString());
            updateMessage(stage.toString());

            sendListenerEvent(listener -> listener.prePFMTaskStage(this, stage));

            if(!doTask()){
                cancel();
                break;
            }

            sendListenerEvent(listener -> listener.postPFMTaskStage(this, stage));


            //TODO
            if(!isSubTask){
                //DrawingBotV3.logger.finer("Finished Stage " + stage.name());
                context.taskManager.onPlottingTaskStageFinished(this, stage);
            }
        }

        if(!isSubTask && !isCancelled()){
            Platform.runLater(() -> {
                //Switch from rendering the task to rendering the generated drawing
                context.taskManager.setActiveTask(null);
                context.taskManager.setRenderedTask(null);
                context.taskManager.setCurrentDrawing(drawing);
            });
        }
        safeDestroy();

        //Finish the task timer
        taskTimer.finish();

        return drawing;
    }

    /**
     * Overridden by tasks which require image data e.g. {@link PFMTaskImage}
     * <br>
     * Typically used to create {@link drawingbot.api.IPixelData} for the {@link IPFM}
     */
    public void preProcessImages(){
        //NOP
    }

    /**
     * Overridden by tasks which require image data e.g. {@link PFMTaskImage}
     * <br>
     * Typically used to destroy / clean up {@link drawingbot.api.IPixelData} created for the {@link IPFM}
     */
    public void postProcessImages(){
        //NOP
    }

    public void comment(String comment){
        comments.add(comment);
        DrawingBotV3.logger.info("Task Comment: " + comment);
    }

    /**
     * @return The elapsed time of the task in milliseconds
     * <br>
     *     If that task is still running this will be the current elapsed time, if the task has finished this will be the time taken to run
     */
    public long getElapsedTime(){
        return taskTimer.getElapsedTime();
    }

    public boolean isTaskFinished(){
        return stage == EnumTaskStage.FINISH;
    }

    /**
     * Called when a user presses the STOP button, rather than cancelling the task, this tries to keep the progress which has already been made.
     */
    public void stopElegantly(){
        subTasks.forEach(PFMTask::stopElegantly);
        DrawingBotV3.logger.info(stage.toString());
        if(stage.ordinal() < EnumTaskStage.DO_PROCESS.ordinal()){
            //If the task hasn't started processing yet we just cancel it and prevent the next stage from occuring
            cancel();
        }else if(stage == EnumTaskStage.DO_PROCESS){
            //If the task is processing, allow it to finish processing and complete the post-processing, finishing stages to create a useable result
            finishEarly = true;
        }
    }

    /**
     * @return true if the task has finished, by a user pressing the "stop" button, or if it has been cancelled.
     */
    public boolean isFinished() {
        return isCancelled() || finishEarly || hostTask != null && hostTask.isFinished();
    }

    /**
     * Attempts to destroy the current task, the primary difference is if this task is required by another task, the case for sub tasks for colour separation this "hostTask" will destroy the sub task only when it no longer requires it's resources
     */
    public void tryDestroy(){
        if(hostTask == null){ //the host task is responsible for destroying it's own sub tasks
            super.tryDestroy();
        }
    }

    /**
     * Releases any objects which are only required while the PFM is running, to allow the objects to be garbage collected earlier if required
     */
    public void safeDestroy(){
        subTasks.forEach(PFMTask::destroy);
        subTasks.clear();

        if(pfm != null){
            pfm.onStopped();
            pfm = null;
        }

        if(tools != null){
            tools.destroy();
        }
    }

    /**
     * Releases all objects relating to the PFMTask including all generated outputs e.g. wipes the generated drawing
     */
    public void destroy(){
        safeDestroy();

        comments.clear();
        finishEarly = false;

        drawing.reset();
    }

    //// CALLBACKS \\\\


    //TODO MOVE CALLBACK INTO TOOLS???
    public Consumer<AbstractSketchPFM> sketchPFMProgressCallback = null;

    /**
     * Allows the PlottingTask to override the SketchPFMs progress.
     */
    public boolean applySketchPFMProgressCallback(AbstractSketchPFM sketchPFM){
        if(sketchPFMProgressCallback != null){
            sketchPFMProgressCallback.accept(sketchPFM);
            return true;
        }
        return false;
    }


    //// GEOMETRY UI HOOKS \\\\

    public int getCurrentGeometryCount(){
        return drawing.getGeometryCount();
    }

    public long getCurrentVertexCount(){
        return drawing.getVertexCount();
    }

    @Override
    public boolean isPlottingTask() {
        return true;
    }

    //// TASK HIERARCHY \\\\

    public PFMTask hostTask;

    /**
     * @return the host task which was responsible for the lifecycle of this {@link PFMTask} if it has one
     */
    public PFMTask getHostTask() {
        return hostTask;
    }

    /**
     * @param hostTask the host task which is responsible for the lifecycle of this {@link PFMTask}
     */
    public void setHostTask(PFMTask hostTask) {
        this.hostTask = hostTask;
    }

    /**
     *
     * @return true if the task either belongs to another host task see {@link #getHostTask()} or if it has been initialised by another task / thread and not by the user, sub tasks will not be rendered in the viewport unless directed to by the host task via {@link ITaskManager#setRenderedTask(PFMTask)}
     */
    public boolean isSubTask() {
        return isSubTask;
    }

    //// RENDERING \\\\

    private WrappedGeometryIterator iterator;
    private final List<PFMTask> subTasks = new ArrayList<>();
    private final List<PlottedDrawing> subDrawings = new ArrayList<>();

    /**
     * @return the geometry iterator which should be used when rendering this task, it can also handle the rendering of multiple sub-iterators / sub-drawings, before the drawing is completed
     */
    public WrappedGeometryIterator getTaskGeometryIterator(){
        if(iterator == null){
            iterator = new WrappedGeometryIterator();
            iterator.addIterator(drawing, new AsynchronousGeometryIterator(drawing));
        }
        return iterator;
    }

    /**
     * @return the list of all  {@link PFMTask}s for which this {@link PFMTask} is the {@link #hostTask}, typically this will be empty, it is primarily used by colour separation tasks e.g. sub tasks for each channel in CMYK processing
     */
    public List<PFMTask> getSubTasks() {
        return subTasks;
    }

    /**
     * @return the list of all the sub drawings of the {@link PFMTask}, typically this consists of all the {@link #getSubTasks()}'s drawings, but can also contain additional {@link PlottedDrawing} as required
     */
    public List<PlottedDrawing> getSubDrawings() {
        return subDrawings;
    }

    public void addSubTask(PFMTask task){
        subTasks.add(task);
        addSubDrawing(task.drawing);
        task.setHostTask(this);
        sendListenerEvent(listener -> listener.onSubTaskAdded(this, task));
    }

    public void removeSubTask(PFMTask task){
        subTasks.remove(task);
        removeSubDrawing(task.drawing);
        task.setHostTask(null);
        sendListenerEvent(listener -> listener.onSubTaskRemoved(this, task));
    }

    public void addSubDrawing(PlottedDrawing drawing){
        if(hostTask != null){
            hostTask.addSubDrawing(drawing);
        }else{
            getTaskGeometryIterator().addIterator(drawing, new AsynchronousGeometryIterator(drawing));
            subDrawings.add(drawing);
        }
    }

    public void removeSubDrawing(PlottedDrawing drawing){
        if(hostTask != null){
            hostTask.removeSubDrawing(drawing);
        }else{
            getTaskGeometryIterator().removeIterator(drawing);
            subDrawings.add(drawing);
        }
    }

    public void resetDrawingIterator(PlottedDrawing drawing){
        if(hostTask != null){
            hostTask.resetDrawingIterator(drawing);
        }else{
            getTaskGeometryIterator().resetIterator(drawing);
            subDrawings.add(drawing);
        }
    }

    public void resetIterator(){
        if(hostTask != null){
            hostTask.resetIterator();
        }else{
            getTaskGeometryIterator().reset();
            subDrawings.add(drawing);
        }
    }

    /**
     * @return If this PFMTask is a special colour match task, this is required in specific situations where custom behaviour might be required to account for colour matching
     */
    public boolean isColourMatchTask(){
        return false;
    }

    //// LOGGING \\\\

    public void logStart(){
        DrawingBotV3.logger.config("STARTING %s: %s, Sub Task: %s".formatted(getTaskName(), pfmFactory.getRegistryName(), isSubTask));
        DrawingBotV3.logger.config("Canvas: %s".formatted(drawing.getCanvas()));
    }

    @Override
    public String getTaskType() {
        return "PFMTask";
    }

    public String getTaskName(){
        return hostTask == null ? super.getTaskName() : "%s: %s".formatted(hostTask.getTaskName(), super.getTaskName());
    }

    /////////////////////////////////


    private ObservableList<Listener> listeners = null;

    public ObservableList<Listener> listeners(){
        if(listeners == null){
            listeners = FXCollections.observableArrayList();
        }
        return listeners;
    }

    public interface Listener{

        default void prePFMSettingsApplied(PFMTask task, List<GenericSetting<?, ?>> src, IPFM pfm){}

        default void postPFMSettingsApplied(PFMTask task, List<GenericSetting<?, ?>> src, IPFM pfm){}

        default void preSetupPFM(PFMTask task, IPFM pfm){}

        default void postSetupPFM(PFMTask task, IPFM pfm){}

        default void prePFMTaskStage(PFMTask task, EnumTaskStage stage){}

        default void postPFMTaskStage(PFMTask task, EnumTaskStage stage){}

        default void onException(PFMTask task, Exception exception){}

        default void onSubTaskAdded(PFMTask task, PFMTask subTask){}

        default void onSubTaskRemoved(PFMTask task, PFMTask subTask){}
    }

}