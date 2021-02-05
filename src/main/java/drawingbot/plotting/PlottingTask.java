package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.pfm.IPFM;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.utils.GenericFactory;
import drawingbot.utils.EnumTaskStage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.*;

public class PlottingTask extends Task<PlottingTask> {

    public GenericFactory<IPFM> loader;
    public PlottedDrawing plottedDrawing;

    // STATUS \\
    public EnumTaskStage stage = EnumTaskStage.QUEUED;
    public long startTime;
    public long finishTime = -1;
    public boolean finishedRenderingPaths = false;
    public List<String> comments = new ArrayList<>();

    // IMAGES \\
    public PImage img_original;              // The original image
    public PImage img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public PImage img_plotting;              // Used during drawing for current brightness levels.  Gets damaged during drawing.

    // PATH FINDING \\
    public IPFM pfm;
    public float old_x = 0;
    public float old_y = 0;
    public boolean is_pen_down;

    // GCODE \\
    private float gcode_offset_x;
    private float gcode_offset_y;
    private float gcode_scale;

    public PlottingTask(GenericFactory loader, ObservableDrawingSet drawingPenSet, PImage image){
        updateTitle("Processing Image");
        this.loader = loader;
        this.plottedDrawing = new PlottedDrawing(drawingPenSet);
        this.img_original = image;
    }

    public boolean doTask(){
        DrawingBotV3 app = DrawingBotV3.INSTANCE;

        switch (stage){

            case QUEUED:
                startTime = System.currentTimeMillis();
                finishStage();
                break;

            case LOADING_IMAGE:
                DrawingBotV3.logger.fine("Creating PFM Instance");
                pfm = loader.instance();
                PFMMasterRegistry.applySettings(pfm);
                pfm.init(this);

                DrawingBotV3.logger.fine("Creating Plotting Image");
                img_plotting = app.createImage(img_original.width, img_original.height, PConstants.RGB);
                img_plotting.copy(img_original, 0, 0, img_original.width, img_original.height, 0, 0, img_original.width, img_original.height);

                finishStage();
                break;

            case PRE_PROCESSING:
                updateMessage("Pre-Processing Image");

                DrawingBotV3.logger.fine("PFM - Pre-Processing - Started");
                pfm.preProcess();
                DrawingBotV3.logger.fine("PFM - Pre-Processing - Finished");

                img_plotting.loadPixels();
                img_reference = app.createImage(img_plotting.width, img_plotting.height, PConstants.RGB);
                img_reference.copy(img_plotting, 0, 0, img_plotting.width, img_plotting.height, 0, 0, img_plotting.width, img_plotting.height);

                float   gcode_scale_x, gcode_scale_y;
                gcode_scale_x = DrawingBotV3.INSTANCE.getDrawingAreaWidthMM() / img_plotting.width;
                gcode_scale_y = DrawingBotV3.INSTANCE.getDrawingAreaHeightMM() / img_plotting.height;
                gcode_scale = min(gcode_scale_x, gcode_scale_y);

                finishStage();
                updateMessage("Plotting Image: " + loader.getName()); //here to avoid excessive task updates
                break;

            case DO_PROCESS:

                if(pfm.finished()){
                    if(finishedRenderingPaths){ //PAUSE FOR THE DRAW THREAD TO FINISH.
                        finishStage();
                    }
                    break;
                }

                pfm.doProcess();
                break;

            case POST_PROCESSING:

                DrawingBotV3.logger.fine("PFM - Post-Processing - Started");
                pfm.postProcess();
                DrawingBotV3.logger.fine("PFM - Post-Processing - Finished");

                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Started");
                plottedDrawing.updateWeightedDistribution();
                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Finished");

                finishStage();
                break;

            case FINISHING:
                finishTime = (System.currentTimeMillis() - startTime);
                updateMessage("Finished: " + finishTime/1000 + " s");
                finishStage();
                break;

            case FINISHED:
                finishStage();
                break;
        }
        return true;
    }

    public void comment(String comment){
        comments.add(comment);
        DrawingBotV3.logger.info("Task Comment: " + comment);
    }

    public void penUp() {
        is_pen_down = false;
    }

    public void penDown() {
        is_pen_down = true;
    }

    public void moveAbs(int pen_number, float x, float y) {
        plottedDrawing.addline(pen_number, is_pen_down, old_x, old_y, x, y);
        old_x = x;
        old_y = y;
    }

    public long getElapsedTime(){
        if(finishTime != -1){
            return finishTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    public void finishStage(){
        DrawingBotV3.INSTANCE.onTaskStageFinished(this, stage);
        stage = EnumTaskStage.values()[stage.ordinal()+1];
    }

    public boolean isTaskFinished(){
        return stage == EnumTaskStage.FINISHED;
    }

    public PImage getOriginalImage() {
        return img_original;
    }

    public PImage getReferenceImage() {
        return img_reference;
    }

    public PImage getPlottingImage() {
        return img_plotting;
    }

    public int width(){
        return getOriginalImage().width;
    }

    public int height(){
        return getOriginalImage().height;
    }

    public void stopElegantly(){
        if(stage.ordinal() < EnumTaskStage.DO_PROCESS.ordinal()){
            cancel();
        }else if(stage == EnumTaskStage.DO_PROCESS){
            pfm.finish();
            finishStage();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        DrawingBotV3.INSTANCE.onTaskCancelled();
        return super.cancel(mayInterruptIfRunning);
    }

    public float getGCodeScale(){
        return gcode_scale;
    }

    public float getGCodeXOffset(){
        return gcode_offset_x + DrawingBotV3.gcodeOffsetX.get();
    }

    public float getGCodeYOffset(){
        return gcode_offset_y + DrawingBotV3.gcodeOffsetY.get();
    }

    @Override
    protected PlottingTask call() throws Exception {
        Platform.runLater(() -> DrawingBotV3.INSTANCE.setActivePlottingTask(this));
        while(!isTaskFinished() && !isCancelled()){
            if(!doTask()){
                cancel();
            }
        }
        return this;
    }

    public void reset(){
        img_original = null;
        img_reference = null;
        img_plotting = null;
        plottedDrawing.reset();
        plottedDrawing = null;
        comments.clear();
        old_x = 0;
        old_y = 0;
        is_pen_down = false;
        gcode_offset_x = 0;
        gcode_offset_y = 0;
        gcode_scale = 0;
        pfm = null;
        loader = null;
        startTime = 0;
        finishTime = -1;
        finishedRenderingPaths = false;
    }
}
