package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.GCodeExporter;
import drawingbot.pfm.IPFM;
import drawingbot.pfm.PFMLoaders;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.Limit;
import javafx.concurrent.Task;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.PrintWriter;

import static processing.core.PApplet.*;

//TODO BATCH PROCESSING TODO REORGANISE VARIABLES
public class PlottingTask extends Task<PlottingTask> {

    public EnumTaskStage stage = EnumTaskStage.QUEUED;

    // IMAGE \\
    public PImage img_original;              // The original image
    public PImage img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public PImage img_plotting;              // Used during drawing for current brightness levels.  Gets damaged during drawing.

    public Limit dx, dy;
    public PlottedDrawing plottedDrawing;
    public PrintWriter output;

    public String gcode_comments = "";

    // PLOTTING \\
    public float old_x = 0;
    public float old_y = 0;
    public boolean is_pen_down;

    // GCODE \\
    public float gcode_offset_x;
    public float gcode_offset_y;
    public float gcode_scale;

    // RENDERING \\
    public float screen_scale;

    //PATH FINDING \\
    public IPFM pfm;

    private PFMLoaders loader;
    private String imageURL;
    private long startTime;
    public long finishTime = -1;
    public boolean finishedRenderingPaths = false;

    public PlottingTask(PFMLoaders loader, ObservableDrawingSet drawingPenSet, String imageURL){
        updateTitle("Processing Image");
        this.loader = loader;
        this.plottedDrawing = new PlottedDrawing(this, drawingPenSet);
        this.imageURL = imageURL;
    }

    public boolean doTask(){
        DrawingBotV3 app = DrawingBotV3.INSTANCE;

        switch (stage){
            case QUEUED:
                startTime = System.currentTimeMillis();
                finishStage();
                break;
            case LOADING_IMAGE:
                updateMessage("Loading Image: " + imageURL);
                PImage loadedImg = app.loadImage(imageURL);

                if(loadedImg == null){
                    updateMessage("Invalid Image: " + imageURL);
                    return false; //SET THREAD ERROR?? TODO
                }

                pfm = loader.createNewPFM(this);

                updateMessage("Rotating Image");
                img_plotting = loadedImg;//ImageTools.image_rotate(loadedImg);

                img_original = app.createImage(img_plotting.width, img_plotting.height, PConstants.RGB);
                img_original.copy(img_plotting, 0, 0, img_plotting.width, img_plotting.height, 0, 0, img_plotting.width, img_plotting.height);

                finishStage();
                break;
            case PRE_PROCESSING:
                updateMessage("Pre-Processing Image");
                pfm.pre_processing(); //adjust the dimensions / crop of img_plotting

                img_plotting.loadPixels();
                img_reference = app.createImage(img_plotting.width, img_plotting.height, PConstants.RGB);
                img_reference.copy(img_plotting, 0, 0, img_plotting.width, img_plotting.height, 0, 0, img_plotting.width, img_plotting.height);

                dx = new Limit();
                dy = new Limit();

                float   gcode_scale_x, gcode_scale_y;
                float   screen_scale_x, screen_scale_y;
                gcode_scale_x = DrawingBotV3.INSTANCE.getDrawingAreaWidthMM() / img_plotting.width;
                gcode_scale_y = DrawingBotV3.INSTANCE.getDrawingAreaHeightMM() / img_plotting.height;
                gcode_scale = min(gcode_scale_x, gcode_scale_y);
                gcode_offset_x = - (img_plotting.width* gcode_scale / 2.0F);
                gcode_offset_y = - (DrawingBotV3.paper_top_to_origin - (DrawingBotV3.paper_size_y - (img_plotting.height * gcode_scale)) / 2.0F);

                screen_scale_x = app.width / (float)img_plotting.width;
                screen_scale_y = app.height / (float)img_plotting.height;
                screen_scale = min(screen_scale_x, screen_scale_y);

                GCodeExporter.gcodeComment(this, "final dimensions: " + img_plotting.width + " by " + img_plotting.height);
                GCodeExporter.gcodeComment(this,"paper_size: " + nf(DrawingBotV3.paper_size_x,0,2) + " by " + nf(DrawingBotV3.paper_size_y,0,2) + "      " + nf(DrawingBotV3.paper_size_x/25.4F,0,2) + " by " + nf(DrawingBotV3.paper_size_y/25.4F,0,2));
                GCodeExporter.gcodeComment(this,"drawing size max: " + nf(DrawingBotV3.INSTANCE.getDrawingAreaWidthMM(),0,2) + " by " + nf(DrawingBotV3.INSTANCE.getDrawingAreaHeightMM(),0,2) + "      " + nf(DrawingBotV3.INSTANCE.getDrawingAreaWidthMM()/25.4F,0,2) + " by " + nf(DrawingBotV3.INSTANCE.getDrawingAreaHeightMM()/25.4F,0,2));
                GCodeExporter.gcodeComment(this,"drawing size calculated " + nf(img_plotting.width * gcode_scale,0,2) + " by " + nf(img_plotting.height * gcode_scale,0,2) + "      " + nf(img_plotting.width * gcode_scale/25.4F,0,2) + " by " + nf(img_plotting.height * gcode_scale/25.4F,0,2));
                GCodeExporter.gcodeComment(this,"gcode_scale X:  " + nf(gcode_scale_x,0,2));
                GCodeExporter.gcodeComment(this,"gcode_scale Y:  " + nf(gcode_scale_y,0,2));
                GCodeExporter.gcodeComment(this,"gcode_scale:    " + nf(gcode_scale,0,2));
                pfm.output_parameters();

                finishStage();
                break;
            case PATH_FINDING:

                if(pfm.finished()){
                    if(finishedRenderingPaths){ //PAUSE FOR THE DRAW THREAD TO FINISH.
                        finishStage();
                    }
                    break;
                }

                pfm.find_path();
                updateProgress(pfm.progress(), 1.0);
                updateMessage("Plotting Image: " + loader.getName());
                break;
            case POST_PROCESSING:
                pfm.post_processing();

                plottedDrawing.setEvenDistribution();
                plottedDrawing.normalizeDistribution();


                plottedDrawing.evenlyDistributePenChanges();
                plottedDrawing.distributePenChangesAccordingToPercentages();

                GCodeExporter.gcodeComment(this,"extremes of X: " + dx.min + " to " + dx.max);
                GCodeExporter.gcodeComment(this, "extremes of Y: " + dy.min + " to " + dy.max);

                finishStage();
                break;
            case LOGGING:
                finishTime = (System.currentTimeMillis() - startTime);
                //controller.progressBarLabel.setText("Draw: " + lastDrawTick + " milliseconds");
                updateMessage("Finished: " + finishTime/1000 + " s");
                finishStage();
                break;
            case FINISHED:
                break;
        }
        return true;
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

    @Override
    protected PlottingTask call() throws Exception {
        DrawingBotV3.INSTANCE.setActivePlottingTask(this);
        while(!isTaskFinished() && !isCancelled()){
            if(!doTask()){
                cancel();
            }
        }
        return this;
    }
}
