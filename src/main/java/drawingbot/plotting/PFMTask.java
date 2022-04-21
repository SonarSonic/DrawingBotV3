package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.*;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.utils.DBTask;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.Utils;
import javafx.application.Platform;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PFMTask extends DBTask<PlottedDrawing> {

    public final IDrawingManager drawingManager;
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

    public PFMTask(IDrawingManager drawingManager, PlottedDrawing drawing, PFMFactory<?> pfmFactory, ObservableDrawingSet refPenSet, List<GenericSetting<?, ?>> pfmSettings){
        updateTitle("Plotting Image (" + pfmFactory.getName() + ")");
        this.drawingManager = drawingManager;
        this.refCanvas = drawing.getCanvas();
        this.refPenSet = refPenSet;
        this.pfmSettings = pfmSettings;
        this.pfmFactory = pfmFactory;
        this.drawing = drawing;
        this.tools = new PlottingTools(drawing, drawing.newPlottedGroup(refPenSet, pfmFactory));
        this.tools.pfmTask = this;
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
                GenericSetting.applySettingsToInstance(pfmSettings, pfm);
                onPFMSettingsApplied(pfmSettings, pfm);

                preProcessImages();

                //set the plotting transform
                if(pfm.getPlottingResolution() != 1){
                    tools.plottingTransform = AffineTransform.getScaleInstance(1D / pfm.getPlottingResolution(), 1D / pfm.getPlottingResolution());
                }

                tools.currentGroup.setPFMFactory(pfmFactory);

                DrawingBotV3.logger.fine("PFM - Pre-Process");
                updateMessage("Pre-Processing - PFM");
                pfm.setup();

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
            drawingManager.onPlottingTaskStageFinished(this, stage);
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
                drawingManager.setActiveTask(this);
                drawingManager.setRenderedTask(null);
                drawingManager.setCurrentDrawing(null);
            });
        }
        while(!isTaskFinished() && !isCancelled()){
            if(!doTask()){
                cancel();
            }
        }
        if(!isSubTask){
            Platform.runLater(() -> {
                drawingManager.setActiveTask(null);
                drawingManager.setRenderedTask(null);
                drawingManager.setCurrentDrawing(drawing);

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
        subTasks.forEach(PFMTask::reset);
        subTasks = null;
        pfmFactory = null;
        //drawing.reset();

        startTime = 0;
        finishTime = -1;
        comments.clear();

        pfm = null;
        finishEarly = false;
    }

    //// CALLBACKS
    //TODO MOVE CALLBACK INTO TOOLS???
    public BiConsumer<List<GenericSetting<?, ?>>, IPFM> onPFMSettingsAppliedCallback = null;
    public Consumer<AbstractSketchPFM> sketchPFMProgressCallback = null;

    public void onPFMSettingsApplied(List<GenericSetting<?, ?>> src, IPFM pfm){
        if(onPFMSettingsAppliedCallback != null){
            onPFMSettingsAppliedCallback.accept(src, pfm);
        }
    }

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
    private List<PFMTask> subTasks = new ArrayList<>();
    private List<PlottedDrawing> subDrawings = new ArrayList<>();

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
}
