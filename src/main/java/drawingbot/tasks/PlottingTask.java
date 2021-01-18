package drawingbot.tasks;

import drawingbot.DrawingBotV3;
import drawingbot.helpers.DrawingTools;
import drawingbot.helpers.GCodeHelper;
import drawingbot.helpers.ImageTools;
import drawingbot.pfm.IPFM;
import drawingbot.pfm.PFMLoaders;
import drawingbot.utils.Limit;
import drawingbot.utils.PlottedDrawing;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.PrintWriter;

import static processing.core.PApplet.*;

public class PlottingTask {

    public int state = 0;

    // IMAGE \\
    public PImage img_original;              // The original image
    public PImage img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public PImage img_plotting;              // Used during drawing for current brightness levels.  Gets damaged during drawing.

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

    //PATH FINDING \\
    public IPFM pfm;

    public int display_line_count;

    public PlottingTask(DrawingBotV3 app, PFMLoaders loader, PImage loadedImg){
        pfm = loader.createNewPFM(this);
        img_plotting = ImageTools.image_rotate(loadedImg);

        img_original = app.createImage(img_plotting.width, img_plotting.height, PConstants.RGB);
        img_original.copy(img_plotting, 0, 0, img_plotting.width, img_plotting.height, 0, 0, img_plotting.width, img_plotting.height);

        pfm.pre_processing(); //adjust the dimensions / crop of img_plotting
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
        app.screen_scale = min(screen_scale_x, screen_scale_y);
        app.screen_scale_org = app.screen_scale;

        GCodeHelper.gcode_comment(this, "final dimensions: " + img_plotting.width + " by " + img_plotting.height);
        GCodeHelper.gcode_comment(this,"paper_size: " + nf(DrawingBotV3.paper_size_x,0,2) + " by " + nf(DrawingBotV3.paper_size_y,0,2) + "      " + nf(DrawingBotV3.paper_size_x/25.4F,0,2) + " by " + nf(DrawingBotV3.paper_size_y/25.4F,0,2));
        GCodeHelper.gcode_comment(this,"drawing size max: " + nf(DrawingBotV3.image_size_x,0,2) + " by " + nf(DrawingBotV3.image_size_y,0,2) + "      " + nf(DrawingBotV3.image_size_x/25.4F,0,2) + " by " + nf(DrawingBotV3.image_size_y/25.4F,0,2));
        GCodeHelper.gcode_comment(this,"drawing size calculated " + nf(img_plotting.width * gcode_scale,0,2) + " by " + nf(img_plotting.height * gcode_scale,0,2) + "      " + nf(img_plotting.width * gcode_scale/25.4F,0,2) + " by " + nf(img_plotting.height * gcode_scale/25.4F,0,2));
        GCodeHelper.gcode_comment(this,"gcode_scale X:  " + nf(gcode_scale_x,0,2));
        GCodeHelper.gcode_comment(this,"gcode_scale Y:  " + nf(gcode_scale_y,0,2));
        GCodeHelper.gcode_comment(this,"gcode_scale:    " + nf(gcode_scale,0,2));
        pfm.output_parameters();
    }

    public void doTask(){
        switch (state){
            case 0:
                //startTime = millis();
                state++;
                break;
            case 1:
                pfm.find_path();
                display_line_count = plottedDrawing.line_count;
                if(pfm.finished()){
                    state++;
                }
                break;
            case 2:
                pfm.post_processing();

                DrawingTools.set_even_distribution(this);
                DrawingTools.normalize_distribution(this);
                plottedDrawing.evenly_distribute_pen_changes(plottedDrawing.get_line_count(), DrawingBotV3.pen_count);
                plottedDrawing.distribute_pen_changes_according_to_percentages(display_line_count, DrawingBotV3.pen_count);

                //println("elapsed time: " + (millis() - startTime) / 1000.0 + " seconds");
                display_line_count = plottedDrawing.line_count;

                GCodeHelper.gcode_comment(this,"extreams of X: " + dx.min + " thru " + dx.max);
                GCodeHelper.gcode_comment(this, "extreams of Y: " + dy.min + " thru " + dy.max);
                finishTask();
                state++;
                break;
        }
    }

    public boolean taskFinished = false;

    public void finishTask(){
        taskFinished = true;
    }

    public boolean isTaskFinished(){
        return taskFinished;
    }

    //TODO ALLOW OTHER EXTENSIONS

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
