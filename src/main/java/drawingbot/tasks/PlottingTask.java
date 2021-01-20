package drawingbot.tasks;

import drawingbot.DrawingBotV3;
import drawingbot.helpers.DrawingTools;
import drawingbot.helpers.GCodeHelper;
import drawingbot.helpers.ImageTools;
import drawingbot.pfm.IPFM;
import drawingbot.pfm.PFMLoaders;
import drawingbot.utils.EnumTaskState;
import drawingbot.utils.Limit;
import drawingbot.utils.PlottedDrawing;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.PrintWriter;

import static processing.core.PApplet.*;

//TODO BATCH PROCESSING
public class PlottingTask {

    public EnumTaskState state = EnumTaskState.QUEUED;

    // IMAGE \\
    public PImage img_original;              // The original image
    public PImage img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public PImage img_plotting;              // Used during drawing for current brightness levels.  Gets damaged during drawing.
    public PImage img_output_cache;              // Used during drawing for current brightness levels.  Gets damaged during drawing.

    public Limit dx, dy;
    public PlottedDrawing plottedDrawing;
    public float[] pen_distribution = new float[DrawingBotV3.pen_count];
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
    public float screen_scale_org;
    public int screen_rotate = 0;

    //PATH FINDING \\
    public IPFM pfm;

    public int display_line_count;

    private PFMLoaders loader;
    private String imageURL;
    private long startTime;
    public boolean finishedRenderingPaths = false;

    public PlottingTask(PFMLoaders loader, String imageURL){
        this.loader = loader;
        this.imageURL = imageURL;
    }

    public boolean doTask(){
        DrawingBotV3 app = DrawingBotV3.INSTANCE;

        switch (state){
            case QUEUED:
                startTime = System.currentTimeMillis();
                nextStage();
                break;
            case LOADING_IMAGE:
                PlottingThread.setThreadStatus("Loading Image: " + imageURL);
                PImage loadedImg = app.loadImage(imageURL);
                PlottingThread.setThreadProgress(0.2);

                if(loadedImg == null){
                    PlottingThread.setThreadStatus("Invalid Image: " + imageURL);
                    return false; //SET THREAD ERROR?? TODO
                }

                pfm = loader.createNewPFM(this);

                PlottingThread.setThreadStatus("Rotating Image");
                img_plotting = loadedImg;//ImageTools.image_rotate(loadedImg);
                PlottingThread.setThreadProgress(0.4);

                img_original = app.createImage(img_plotting.width, img_plotting.height, PConstants.RGB);
                img_original.copy(img_plotting, 0, 0, img_plotting.width, img_plotting.height, 0, 0, img_plotting.width, img_plotting.height);

                nextStage();
                break;
            case PRE_PROCESSING:
                PlottingThread.setThreadStatus("Pre-Processing Image");
                pfm.pre_processing(); //adjust the dimensions / crop of img_plotting
                PlottingThread.setThreadProgress(0.8);

                img_plotting.loadPixels();
                img_reference = app.createImage(img_plotting.width, img_plotting.height, PConstants.RGB);
                img_reference.copy(img_plotting, 0, 0, img_plotting.width, img_plotting.height, 0, 0, img_plotting.width, img_plotting.height);

                plottedDrawing = new PlottedDrawing(this);
                dx = new Limit();
                dy = new Limit();

                float   gcode_scale_x, gcode_scale_y;
                float   screen_scale_x, screen_scale_y;
                gcode_scale_x = DrawingBotV3.image_size_x / img_plotting.width;
                gcode_scale_y = DrawingBotV3.image_size_y / img_plotting.height;
                gcode_scale = min(gcode_scale_x, gcode_scale_y);
                gcode_offset_x = - (img_plotting.width* gcode_scale / 2.0F);
                gcode_offset_y = - (DrawingBotV3.paper_top_to_origin - (DrawingBotV3.paper_size_y - (img_plotting.height * gcode_scale)) / 2.0F);

                screen_scale_x = app.width / (float)img_plotting.width;
                screen_scale_y = app.height / (float)img_plotting.height;
                screen_scale = min(screen_scale_x, screen_scale_y);
                screen_scale_org = screen_scale;

                GCodeHelper.gcodeComment(this, "final dimensions: " + img_plotting.width + " by " + img_plotting.height);
                GCodeHelper.gcodeComment(this,"paper_size: " + nf(DrawingBotV3.paper_size_x,0,2) + " by " + nf(DrawingBotV3.paper_size_y,0,2) + "      " + nf(DrawingBotV3.paper_size_x/25.4F,0,2) + " by " + nf(DrawingBotV3.paper_size_y/25.4F,0,2));
                GCodeHelper.gcodeComment(this,"drawing size max: " + nf(DrawingBotV3.image_size_x,0,2) + " by " + nf(DrawingBotV3.image_size_y,0,2) + "      " + nf(DrawingBotV3.image_size_x/25.4F,0,2) + " by " + nf(DrawingBotV3.image_size_y/25.4F,0,2));
                GCodeHelper.gcodeComment(this,"drawing size calculated " + nf(img_plotting.width * gcode_scale,0,2) + " by " + nf(img_plotting.height * gcode_scale,0,2) + "      " + nf(img_plotting.width * gcode_scale/25.4F,0,2) + " by " + nf(img_plotting.height * gcode_scale/25.4F,0,2));
                GCodeHelper.gcodeComment(this,"gcode_scale X:  " + nf(gcode_scale_x,0,2));
                GCodeHelper.gcodeComment(this,"gcode_scale Y:  " + nf(gcode_scale_y,0,2));
                GCodeHelper.gcodeComment(this,"gcode_scale:    " + nf(gcode_scale,0,2));
                pfm.output_parameters();

                PlottingThread.setThreadProgress(1.0);
                nextStage();
                break;
            case PATH_FINDING:

                if(pfm.finished()){
                    if(finishedRenderingPaths){ //PAUSE FOR THE DRAW THREAD TO FINISH.
                        nextStage();
                    }
                    break;
                }

                pfm.find_path();
                display_line_count = plottedDrawing.getPlottedLineCount();
                PlottingThread.setThreadProgress(pfm.progress());
                PlottingThread.setThreadStatus("Plotting Image: Lines: " + plottedDrawing.getPlottedLineCount());
                break;
            case POST_PROCESSING:
                pfm.post_processing();

                DrawingTools.set_even_distribution(this);
                DrawingTools.normalize_distribution(this);


                long start = System.currentTimeMillis();
                plottedDrawing.evenly_distribute_pen_changes(plottedDrawing.getPlottedLineCount()-1, DrawingBotV3.pen_count);
                long evenly_distribute_pen_changes = (System.currentTimeMillis() - start);
                println("evenly_distribute_pen_changes: " + evenly_distribute_pen_changes + " ms");

                start = System.currentTimeMillis();
                plottedDrawing.distribute_pen_changes_according_to_percentages(display_line_count, DrawingBotV3.pen_count);
                long distribute_pen_changes_according_to_percentages = (System.currentTimeMillis() - start);
                println("distribute_pen_changes_according_to_percentages: " + evenly_distribute_pen_changes + " ms");


                //println("elapsed time: " + (millis() - startTime) / 1000.0 + " seconds");
                display_line_count = plottedDrawing.getPlottedLineCount();

                GCodeHelper.gcodeComment(this,"extremes of X: " + dx.min + " to " + dx.max);
                GCodeHelper.gcodeComment(this, "extremes of Y: " + dy.min + " to " + dy.max);

                nextStage();
                break;
            case LOGGING:
                long endTime = System.currentTimeMillis();
                long lastDrawTick = (endTime - startTime);
                //controller.progressBarLabel.setText("Draw: " + lastDrawTick + " milliseconds");
                PlottingThread.setThreadStatus("Plotting Image: Lines: " + plottedDrawing.getPlottedLineCount() + " FINISHED");
                nextStage();
                break;
            case FINISHED:
                break;
        }
        return true;
    }

    public void nextStage(){
        state = EnumTaskState.values()[state.ordinal()+1];
    }

    public boolean isTaskFinished(){
        return state == EnumTaskState.FINISHED;
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

}
