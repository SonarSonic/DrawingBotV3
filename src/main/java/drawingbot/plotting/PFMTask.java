package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.api.IPFM;
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

    public final ICanvas refCanvas;
    public final ObservableDrawingSet refPenSet;

    public PFMFactory<?> pfmFactory;
    public List<GenericSetting<?, ?>> pfmSettings;
    public PlottedDrawing drawing;

    // STATUS \\
    public EnumTaskStage stage = EnumTaskStage.QUEUED;
    public long startTime;
    public long finishTime = -1;
    public List<String> comments = new ArrayList<>();

    // PATH FINDING \\
    public IPFM pfm;
    public boolean finishEarly = false;
    public PlottingTools tools;

    // RENDERING \\\
    public boolean isSubTask = false;

    // SPECIAL \\
    public boolean useLowQuality = false;
    public int parallelPlots = 3;
    public boolean enablePlottingResolution = true;

    public PFMTask(DBTaskContext context, PlottedDrawing drawing, PFMFactory<?> pfmFactory, ObservableDrawingSet refPenSet, List<GenericSetting<?, ?>> pfmSettings){
        super(context);
        updateTitle("Plotting Image (" + pfmFactory.getRegistryName() + ")");
        this.refCanvas = drawing.getCanvas();
        this.refPenSet = refPenSet;
        this.pfmSettings = pfmSettings;
        this.pfmFactory = pfmFactory;
        this.drawing = drawing;
        this.tools = new PlottingTools(drawing, drawing.newPlottedGroup(refPenSet, pfmFactory));
        this.tools.pfmTask = this;
        this.tools.progressCallback = this;
        this.updateProgressInstantly = false;
    }

    public IPFM pfm(){
        return pfm;
    }

    public boolean doTask(){
        switch (stage){
            case QUEUED:
                startTime = System.currentTimeMillis();
                finishStage();
                break;
            case PRE_PROCESSING:
                updateMessage("Pre-Processing");
                finishEarly = false;

                DrawingBotV3.logger.fine("PFM - Pre-Processing - Started");

                DrawingBotV3.logger.fine("PFM - Create Instance");
                pfm = pfmFactory.instance();
                DrawingBotV3.logger.fine("PFM - Init");
                pfm.init(tools);

                DrawingBotV3.logger.fine("PFM - Apply Settings");
                sendListenerEvent(l -> l.prePFMSettingsApplied(this, pfmSettings, pfm));
                GenericSetting.applySettingsToInstance(pfmSettings, pfm);
                pfm.onSettingsApplied();
                sendListenerEvent(l -> l.postPFMSettingsApplied(this, pfmSettings, pfm));

                //set the plotting transform
                if(pfm.getPlottingResolution() != 1 && enablePlottingResolution){
                    tools.plottingTransform = AffineTransform.getScaleInstance(1D / pfm.getPlottingResolution(), 1D / pfm.getPlottingResolution());
                }

                if(!isSubTask && drawing.getMetadata(Register.INSTANCE.SOFT_CLIP_SHAPE) != null){
                    tools.setSoftClip(drawing.getMetadata(Register.INSTANCE.SOFT_CLIP_SHAPE), pfmFactory);
                }

                if(!isSubTask && drawing.getMetadata(Register.INSTANCE.CLIPPING_SHAPE) != null){
                    tools.setClippingShape(drawing.getMetadata(Register.INSTANCE.CLIPPING_SHAPE));
                }

                if(tools.getClippingShape() == null && tools.getSoftClip() == null){
                    switch (drawing.canvas.getClippingMode()){
                        case NONE:
                            break;
                        case DRAWING:
                            tools.setClippingShape(new Rectangle2D.Float(0, 0, drawing.canvas.getScaledDrawingWidth(), drawing.canvas.getScaledDrawingHeight()));
                            break;
                        case PAGE:
                            tools.setClippingShape(new Rectangle2D.Float(-drawing.canvas.getScaledDrawingOffsetX(), -drawing.canvas.getScaledDrawingOffsetY(), drawing.canvas.getScaledWidth(), drawing.canvas.getScaledHeight()));
                            break;
                    }
                }

                preProcessImages();

                tools.currentGroup.setPFMFactory(pfmFactory);

                DrawingBotV3.logger.fine("PFM - Pre-Process");
                updateMessage("Pre-Processing - PFM");

                sendListenerEvent(l -> l.preSetupPFM(this, pfm));
                pfm.setup();
                sendListenerEvent(l -> l.postSetupPFM(this, pfm));

                finishStage();
                updateMessage("Processing"); //here to avoid excessive task updates
                break;
            case DO_PROCESS:
                if(useLowQuality){
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

                    finishStage();
                }else{
                    pfm.run();
                    finishStage();
                }
                break;
            case POST_PROCESSING:
                updateMessage("Post-Processing - PFM");

                postProcessImages();

                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Started");
                updateMessage("Post-Processing - Distributing Pens");
                drawing.updatePenDistribution();
                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Finished");

                finishStage();
                break;
            case FINISHING:
                finishTime = (System.currentTimeMillis() - startTime);
                updateMessage("Finished - Elapsed Time: " + finishTime/1000 + " s");
                updateProgress(1, 1);
                finishStage();
                break;
            case FINISHED:
                break;
        }
        return true;
    }

    public void preProcessImages(){
        //NOP
    }

    public void postProcessImages(){
        //NOP
    }

    public void comment(String comment){
        comments.add(comment);
        DrawingBotV3.logger.info("Task Comment: " + comment);
    }

    public long getElapsedTime(){
        if(finishTime != -1){
            return finishTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    public void finishStage(){
        if(!isSubTask){
            context.taskManager.onPlottingTaskStageFinished(this, stage);
        }
        stage = EnumTaskStage.values()[stage.ordinal()+1];
    }

    public boolean isTaskFinished(){
        return stage == EnumTaskStage.FINISHED;
    }

    public void stopElegantly(){
        subTasks.forEach(PFMTask::stopElegantly);
        DrawingBotV3.logger.info(stage.toString());
        if(stage.ordinal() < EnumTaskStage.DO_PROCESS.ordinal()){
            cancel();
        }else if(stage == EnumTaskStage.DO_PROCESS){
            finishEarly = true;
        }
        if(pfm != null){ //rare case for mosaic tasks where stopElegantly can be called on already finished pfms
            pfm.onStopped();
        }
    }

    @Override
    public final PlottedDrawing call() {
        if(!isSubTask){
            Platform.runLater(() -> {
                context.taskManager.setActiveTask(this);
                context.taskManager.setRenderedTask(null);
                context.taskManager.setCurrentDrawing(null);

                //only update the distribution type the first time the PFM is changed, also only trigger the update when Start Plotting is hit again, so the current drawing doesn't get re-rendered
                //if(drawingManager instanceof DrawingBotV3){ //TODO FIXME?
                    if(refPenSet.colourSeperator.get().isDefault()){
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
        while(!isTaskFinished() && !isCancelled()){
            if(!doTask()){
                cancel();
            }
        }
        if(!isSubTask){
            Platform.runLater(() -> {
                context.taskManager.setActiveTask(null);
                context.taskManager.setRenderedTask(null);
                context.taskManager.setCurrentDrawing(drawing);
            });
        }
        return drawing;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// IPlottingTask
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isFinished() {
        return isCancelled() || finishEarly;
    }

    public void updatePlottingProgress(double progress, double max) {
        double actual = Math.min(Utils.roundToPrecision(progress / max, 3), 1D);
        updateProgress(actual, 1D);
    }

    public void reset(){
        /*
        subTasks.forEach(PFMTask::reset);
        subTasks = null;
        pfmFactory = null;
        //drawing.reset();

        startTime = 0;
        finishTime = -1;
        comments.clear();

        pfm = null;
        finishEarly = false;

         */
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

    public PFMTask getHostTask() {
        return hostTask;
    }

    public void setHostTask(PFMTask hostTask) {
        this.hostTask = hostTask;
    }

    public boolean isSubTask() {
        return isSubTask;
    }

    //// RENDERING \\\\

    private WrappedGeometryIterator iterator;
    private final List<PFMTask> subTasks = new ArrayList<>();
    private final List<PlottedDrawing> subDrawings = new ArrayList<>();

    /**
     * @return the geometry iterator which should be used when rendering this task, before the drawing is complete
     */
    public WrappedGeometryIterator getTaskGeometryIterator(){
        if(iterator == null){
            iterator = new WrappedGeometryIterator();
            iterator.addIterator(drawing, new AsynchronousGeometryIterator(drawing));
        }
        return iterator;
    }

    public List<PFMTask> getSubTasks() {
        return subTasks;
    }

    public List<PlottedDrawing> getSubDrawings() {
        return subDrawings;
    }

    public void addSubTask(PFMTask task){
        subTasks.add(task);
        addSubDrawing(task.drawing);
    }

    public void removeSubTask(PFMTask task){
        subTasks.remove(task);
        removeSubDrawing(task.drawing);
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

    public boolean isColourMatchTask(){
        return false;
    }
}
