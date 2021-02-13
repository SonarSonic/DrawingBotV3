package drawingbot.utils;

import drawingbot.DrawingBotV3;

//TODO FIXME
public class GridOverlay {

    ///grid rendering
    public static final float INCHES_TO_MILLIMETRES = 25.4F;
    public static final float paper_top_to_origin = 285; //mm, make smaller to move drawing down on paper
    public static final float paper_size_x = 32 * INCHES_TO_MILLIMETRES; //mm, papers width
    public static final float paper_size_y = 40 * INCHES_TO_MILLIMETRES; //mm, papers height
    public static final float grid_scale = 25.4F; // Use 10.0 for centimeters, 25.4 for inches, and between 444 and 529.2 for cubits.

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void grid() {
        // This will give you a rough idea of the size of the printed image, in "grid_scale" units.
        // Some screen scales smaller than 1.0 will sometimes display every other line
        // It looks like a big logic bug, but it just can't display a one pixel line scaled down well.

        //TODO FIX GRID RENDERER!!!
        if (app.displayGrid.getValue() && app.getActiveTask() != null) {
            //app.hint(DISABLE_DEPTH_TEST);      // Allow fills to be shown on top.
            int image_center_x = (int)(app.getActiveTask().width() / 2);
            int image_center_y = (int)(app.getActiveTask().height() / 2);
            int gridlines = 100;

            // Give everything outside the paper area a light grey color
            app.noStroke();
            app.fill(0, 0, 0, 32);
            float border_x = (paper_size_x - app.getDrawingAreaWidthMM()) / 2;
            float border_y = (paper_size_y - app.getDrawingAreaHeightMM()) / 2;
            app.rect(-border_x/app.getActiveTask().getGCodeScale(), -border_y/app.getActiveTask().getGCodeScale(), 999999, -999999);
            app.rect((app.getDrawingAreaWidthMM()+border_x)/app.getActiveTask().getGCodeScale(), -border_y/app.getActiveTask().getGCodeScale(), 999999, 999999);
            app.rect((app.getDrawingAreaWidthMM()+border_x)/app.getActiveTask().getGCodeScale(), (app.getDrawingAreaHeightMM()+border_y)/app.getActiveTask().getGCodeScale(), -999999, 999999);
            app.rect(-border_x/app.getActiveTask().getGCodeScale(), (app.getDrawingAreaHeightMM()+border_y)/app.getActiveTask().getGCodeScale(), -999999, -999999);

            // Vertical lines
            app.strokeWeight(1);
            app.stroke(255, 64, 64, 80);
            app.noFill();
            for (int x = -gridlines; x <= gridlines; x++) {
                int x0 = (int)(x * grid_scale / app.getActiveTask().getGCodeScale());
                app.line(x0 + image_center_x, -999999, x0 + image_center_x, 999999);
            }

            // Horizontal lines
            for (int y = -gridlines; y <= gridlines; y++) {
                int y0 = (int)(y * grid_scale / app.getActiveTask().getGCodeScale());
                app.line(-999999, y0 + image_center_y, 999999, y0 + image_center_y);
            }

            // Screen center line
            app.stroke(255, 64, 64, 80);
            app.strokeWeight(4);
            app.line(image_center_x, -999999, image_center_x, 999999);
            app.line(-999999, image_center_y, 999999, image_center_y);
            app.strokeWeight(1);

            // Mark the edge of the drawing/image area in blue
            app.stroke(64, 64, 255, 92);
            app.noFill();
            app.strokeWeight(2);
            app.rect(0, 0, app.getActiveTask().width(), app.getActiveTask().height());

            // Green pen origin (home position) dot.
            app.stroke(0, 255, 0, 255);
            app.fill(0, 255, 0, 255);
            app.ellipse(-app.getActiveTask().getGCodeXOffset() / app.getActiveTask().getGCodeScale(), -app.getActiveTask().getGCodeYOffset() / app.getActiveTask().getGCodeScale(), 10, 10);

            // Red center of image dot
            app.stroke(255, 0, 0, 255);
            app.fill(255, 0, 0, 255);
            app.ellipse(image_center_x, image_center_y, 10, 10);

            // Blue dot at image 0,0
            app.stroke(0, 0, 255, 255);
            app.fill(0, 0, 255, 255);
            app.ellipse(0, 0, 10, 10);

            //app.hint(ENABLE_DEPTH_TEST);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
// Experimental, mark coordinates of mouse locations to console.
// Useful for locating vanishing points etc.
// Currently works correctly with screen_scale, translation and rotation.
    public static void mouse_point() {

        DrawingBotV3.logger.info("Mouse point: ");
        /* TODO FIXME ADD REPLACEMENT!!!!
        switch(app.screen_rotate) {
            case 0:
                DrawingBotV3.logger.info(  (app.mouseX/app.screen_scale - app.mx) + ", " +  (app.mouseY/app.screen_scale - app.my) );
                break;
            case 1:
                DrawingBotV3.logger.info(  (app.mouseY/app.screen_scale - app.my) + ", " + -(app.mouseX/app.screen_scale - app.mx) );
                break;
            case 2:
                DrawingBotV3.logger.info( -(app.mouseX/app.screen_scale - app.mx) + ", " + -(app.mouseY/app.screen_scale - app.my) );
                break;
            case 3:
                DrawingBotV3.logger.info( -(app.mouseY/app.screen_scale - app.my) + ", " +  (app.mouseX/app.screen_scale - app.mx) );
                break;
        }

         */
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////
}