package drawingbot.helpers;

import drawingbot.DrawingBotV3;
import processing.core.PConstants;
import processing.core.PImage;
import static processing.core.PApplet.*;

public class ImageTools {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_threshold() {
        GCodeHelper.gcode_comment("image_threshold");
        app.img.filter(PConstants.THRESHOLD); //THRESHOLD
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_desaturate() {
        GCodeHelper.gcode_comment("image_desaturate");
        app.img.filter(PConstants.GRAY); //GRAY
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_invert() {
        GCodeHelper.gcode_comment("image_invert");
        app.img.filter(PConstants.INVERT); //INVERT
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_posterize(int amount) {
        GCodeHelper.gcode_comment("image_posterize");
        app.img.filter(PConstants.POSTERIZE, amount); //POSTERIZE
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_blur(int amount) {
        GCodeHelper.gcode_comment("image_blur");
        app.img.filter(PConstants.BLUR, amount); //BLUR
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_erode() {
        GCodeHelper.gcode_comment("image_erode");
        app.img.filter(PConstants.ERODE); //ERODE
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_dilate() {
        GCodeHelper.gcode_comment("image_dilate");
        app.img.filter(PConstants.DILATE); //DILATE
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void save_jpg() {
        // Currently disabled.
        // Must not be called from event handling functions such as keyPressed()
        PImage img_drawing;
        PImage  img_drawing2;

        //app.img_drawing = createImage(app.img.width, app.img.height, RGB);
        //app.img_drawing.copy(0, 0, app.img.width, app.img.height, 0, 0, app.img.width, app.img.height);
        //app.img_drawing.save("what the duce.jpg");

        // Save resuling image
        app.save("tmptif.tif");
        img_drawing = app.loadImage("tmptif.tif");
        img_drawing2 = app.createImage(app.img.width, app.img.height, PConstants.RGB);
        img_drawing2.copy(img_drawing, 0, 0, app.img.width, app.img.height, 0, 0, app.img.width, app.img.height);
        img_drawing2.save("drawingbot.gcode\\gcode_" + app.basefile_selected + ".jpg");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_rotate() {
        //image[y][x]                                     // assuming this is the original orientation
        //image[x][original_width - y]                    // rotated 90 degrees ccw
        //image[original_height - x][y]                   // 90 degrees cw
        //image[original_height - y][original_width - x]  // 180 degrees

        if (app.img.width > app.img.height) {
            PImage img2 = app.createImage(app.img.height, app.img.width, PConstants.RGB);
            app.img.loadPixels();
            for (int x=1; x<app.img.width; x++) {
                for (int y=1; y<app.img.height; y++) {
                    int loc1 = x + y*app.img.width;
                    int loc2 = y + (app.img.width - x) * img2.width;
                    img2.pixels[loc2] = app.img.pixels[loc1];
                }
            }
            app.img = img2;
            app.updatePixels();
            GCodeHelper.gcode_comment("image_rotate: rotated 90 degrees to fit machines sweet spot");
        } else {
            GCodeHelper.gcode_comment("image_rotate: no rotation necessary");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void lighten_one_pixel(int adjustbrightness, int x, int y) {
        int loc = (y)*app.img.width + x;
        float r = app.brightness (app.img.pixels[loc]);
        //r += adjustbrightness;
        r += adjustbrightness + app.random(0, 0.01F);
        r = app.constrain(r,0,255);
        int c = app.color(r);
        app.img.pixels[loc] = c;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_scale(int new_width) {
        if (app.img.width != new_width) {
            GCodeHelper.gcode_comment("image_scale, old size: " + app.img.width + " by " + app.img.height + "     ratio: " + (float)app.img.width / (float)app.img.height);
            app.img.resize(new_width, 0);
            GCodeHelper.gcode_comment("image_scale, new size: " + app.img.width + " by " + app.img.height + "     ratio: " + (float)app.img.width / (float)app.img.height);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static float avg_imgage_brightness() {
        float b = 0.0F;

        for (int p=0; p < app.img.width * app.img.height; p++) {
            b += app.brightness(app.img.pixels[p]);
        }

        return(b / (app.img.width * app.img.height));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_crop() {
        // This will center crop to the desired image size image_size_x and image_size_y

        PImage img2;
        float desired_ratio = app.image_size_x / app.image_size_y;
        float current_ratio = (float)app.img.width / (float)app.img.height;

        GCodeHelper.gcode_comment("image_crop desired ratio of " + desired_ratio);
        GCodeHelper.gcode_comment("image_crop old size: " + app.img.width + " by " + app.img.height + "     ratio: " + current_ratio);

        if (current_ratio < desired_ratio) {
            int desired_x = app.img.width;
            int desired_y = (int)(app.img.width / desired_ratio);
            int half_y = (app.img.height - desired_y) / 2;
            img2 = app.createImage(desired_x, desired_y, 1);
            img2.copy(app.img, 0, half_y, desired_x, desired_y, 0, 0, desired_x, desired_y);
        } else {
            int desired_x = (int)(app.img.height * desired_ratio);
            int desired_y = app.img.height;
            int half_x = (app.img.width - desired_x) / 2;
            img2 = app.createImage(desired_x, desired_y, 1);
            img2.copy(app.img, half_x, 0, desired_x, desired_y, 0, 0, desired_x, desired_y);
        }

        app.img = img2;
        GCodeHelper.gcode_comment("image_crop new size: " + app.img.width + " by " + app.img.height + "     ratio: " + (float)app.img.width / (float)app.img.height);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_boarder(String fname, int shrink, int blur) {
        // A quick and dirty way of softening the edges of your drawing.
        // Look in the boarders directory for some examples.
        // Ideally, the boarder will have similar dimensions as the image to be drawn.
        // For far more control, just edit your input image directly.
        // Most of the examples are pretty heavy handed so you can "shrink" them a few pixels as desired.
        // It does not matter if you use a transparant background or just white.  JPEG or PNG, it's all good.
        //
        // fname:   Name of boarder file.
        // shrink:  Number of pixels to pull the boarder away, 0 for no change.
        // blur:    Guassian blur the boarder, 0 for no blur, 10+ for a lot.

        //PImage boarder = createImage(app.img.width+(shrink*2), app.img.height+(shrink*2), RGB);
        PImage temp_boarder = app.loadImage("boarder/" + fname);
        temp_boarder.resize(app.img.width, app.img.height);
        temp_boarder.filter(PConstants.GRAY);
        temp_boarder.filter(PConstants.INVERT);
        temp_boarder.filter(PConstants.BLUR, blur);

        //boarder.copy(temp_boarder, 0, 0, temp_boarder.width, temp_boarder.height, 0, 0, boarder.width, boarder.height);
        app.img.blend(temp_boarder, shrink, shrink, app.img.width, app.img.height,  0, 0, app.img.width, app.img.height, PConstants.ADD);
        GCodeHelper.gcode_comment("image_boarder: " + fname + "   " + shrink + "   " + blur);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_unsharpen(PImage img, int amount) {
        // Source:  https://www.taylorpetrick.com/blog/post/convolution-part3
        // Subtle unsharp matrix
        float[][] matrix = { { -0.00391F, -0.01563F, -0.02344F, -0.01563F, -0.00391F },
                { -0.01563F, -0.06250F, -0.09375F, -0.06250F, -0.01563F },
                { -0.02344F, -0.09375F,  1.85980F, -0.09375F, -0.02344F },
                { -0.01563F, -0.06250F, -0.09375F, -0.06250F, -0.01563F },
                { -0.00391F, -0.01563F, -0.02344F, -0.01563F, -0.00391F } };


        //print_matrix(matrix);
        matrix = scale_matrix(matrix, amount);
        //print_matrix(matrix);
        matrix = normalize_matrix(matrix);
        //print_matrix(matrix);

        image_convolution(img, matrix, 1.0F, 0.0F);
        GCodeHelper.gcode_comment("image_unsharpen: " + amount);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_blurr(PImage img) {
        // Basic blur matrix

        float[][] matrix = { { 1, 1, 1 },
                { 1, 1, 1 },
                { 1, 1, 1 } };

        matrix = normalize_matrix(matrix);
        image_convolution(img, matrix, 1, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_sharpen(PImage img) {
        // Simple sharpen matrix

        float[][] matrix = { {  0, -1,  0 },
                { -1,  5, -1 },
                {  0, -1,  0 } };

        //print_matrix(matrix);
        image_convolution(img, matrix, 1, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_emboss(PImage img) {
        float[][] matrix = { { -2, -1,  0 },
                { -1,  1,  1 },
                {  0,  1,  2 } };

        image_convolution(img, matrix, 1, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_edge_detect(PImage img) {
        // Edge detect
        float[][] matrix = { {  0,  1,  0 },
                {  1, -4,  1 },
                {  0,  1,  0 } };

        image_convolution(img, matrix, 1, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_motion_blur(PImage img) {
        // Motion Blur
        // http://lodev.org/cgtutor/filtering.html

        float[][] matrix = { {  1, 0, 0, 0, 0, 0, 0, 0, 0 },
                {  0, 1, 0, 0, 0, 0, 0, 0, 0 },
                {  0, 0, 1, 0, 0, 0, 0, 0, 0 },
                {  0, 0, 0, 1, 0, 0, 0, 0, 0 },
                {  0, 0, 0, 0, 1, 0, 0, 0, 0 },
                {  0, 0, 0, 0, 0, 1, 0, 0, 0 },
                {  0, 0, 0, 0, 0, 0, 1, 0, 0 },
                {  0, 0, 0, 0, 0, 0, 0, 1, 0 },
                {  0, 0, 0, 0, 0, 0, 0, 0, 1 } };

        matrix = normalize_matrix(matrix);
        image_convolution(img, matrix, 1, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_outline(PImage img) {
        // Outline (5x5)
        // https://www.jmicrovision.com/help/v125/tools/classicfilterop.htm

        float[][] matrix = { { 1,  1,   1,  1,  1 },
                { 1,  0,   0,  0,  1 },
                { 1,  0, -16,  0,  1 },
                { 1,  0,   0,  0,  1 },
                { 1,  1,   1,  1,  1 } };

        //matrix = normalize_matrix(matrix);
        image_convolution(img, matrix, 1, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_sobel(PImage img, float factor, float bias) {

        // Looks like some kind of inverting edge detection
        //float[][] matrix = { { -1, -1, -1 },
        //                     { -1,  8, -1 },
        //                     { -1, -1, -1 } };

        //float[][] matrix = { {  1,  2,   0,  -2,  -1 },
        //                     {  4,  8,   0,  -8,  -4 },
        //                     {  6, 12,   0, -12,  -6 },
        //                     {  4,  8,   0,  -8,  -4 },
        //                     {  1,  2,   0,  -2,  -1 } };

        // Sobel 3x3 X
        float[][] matrixX = { { -1,  0,  1 },
                { -2,  0,  2 },
                { -1,  0,  1 } };

        // Sobel 3x3 Y
        float[][] matrixY = { { -1, -2, -1 },
                {  0,  0,  0 },
                {  1,  2,  1 } };

        image_convolution(img, matrixX, factor, bias);
        image_convolution(img, matrixY, factor, bias);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void image_convolution(PImage img, float[][] matrix, float factor, float bias) {
        // What about edge pixels?  Ignoring (maxrixsize-1)/2 pixels on the edges?

        int n = matrix.length;      // matrix rows
        int m = matrix[0].length;   // matrix columns

        //print_matrix(matrix);

        PImage simg = app.createImage(img.width, img.height, RGB);
        simg.copy(img, 0, 0, img.width, img.height, 0, 0, simg.width, simg.height);
        int matrixsize = matrix.length;

        for (int x = 0; x < simg.width; x++) {
            for (int y = 0; y < simg.height; y++ ) {
                int c = convolution(x, y, matrix, matrixsize, simg, factor, bias);
                int loc = x + y*simg.width;
                img.pixels[loc] = c;
            }
        }
        app.updatePixels();
    }


///////////////////////////////////////////////////////////////////////////////////////////////////////
// Source:  https://py.processing.org/tutorials/pixels/
// By: Daniel Shiffman
// Factor & bias added by SCC

    public static int convolution(int x, int y, float[][] matrix, int matrixsize, PImage img, float factor, float bias) {
        float rtotal = 0.0F;
        float gtotal = 0.0F;
        float btotal = 0.0F;
        int offset = matrixsize / 2;

        // Loop through convolution matrix
        for (int i = 0; i < matrixsize; i++) {
            for (int j= 0; j < matrixsize; j++) {
                // What pixel are we testing
                int xloc = x+i-offset;
                int yloc = y+j-offset;
                int loc = xloc + img.width*yloc;
                // Make sure we have not walked off the edge of the pixel array
                loc = constrain(loc,0,img.pixels.length-1);
                // Calculate the convolution
                // We sum all the neighboring pixels multiplied by the values in the convolution matrix.
                rtotal += (app.red(img.pixels[loc]) * matrix[i][j]);
                gtotal += (app.green(img.pixels[loc]) * matrix[i][j]);
                btotal += (app.blue(img.pixels[loc]) * matrix[i][j]);
            }
        }

        // Added factor and bias
        rtotal = (rtotal * factor) + bias;
        gtotal = (gtotal * factor) + bias;
        btotal = (btotal * factor) + bias;

        // Make sure RGB is within range
        rtotal = constrain(rtotal,0,255);
        gtotal = constrain(gtotal,0,255);
        btotal = constrain(btotal,0,255);
        // Return the resulting color
        return app.color(rtotal,gtotal,btotal);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static float [][] multiply_matrix (float[][] matrixA, float[][] matrixB) {
        // Source:  https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm
        // Test:    http://www.calcul.com/show/calculator/matrix-multiplication_;2;3;3;5

        int n = matrixA.length;      // matrixA rows
        int m = matrixA[0].length;   // matrixA columns
        int p = matrixB[0].length;

        float[][] matrixC;
        matrixC = new float[n][p];

        for (int i=0; i<n; i++) {
            for (int j=0; j<p; j++) {
                for (int k=0; k<m; k++) {
                    matrixC[i][j] = matrixC[i][j] + matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        //print_matrix(matrix);
        return matrixC;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static float [][] normalize_matrix (float[][] matrix) {
        // Source:  https://www.taylorpetrick.com/blog/post/convolution-part2
        // The resulting matrix is the same size as the original, but the output range will be constrained
        // between 0.0 and 1.0.  Useful for keeping brightness the same.
        // Do not use on a maxtix that sums to zero, such as sobel.

        int n = matrix.length;      // rows
        int m = matrix[0].length;   // columns
        float sum = 0;

        for (int i=0; i<n; i++) {
            for (int j=0; j<m; j++) {
                sum += matrix[i][j];
            }
        }

        for (int i=0; i<n; i++) {
            for (int j=0; j<m; j++) {
                matrix[i][j] = matrix[i][j] / abs(sum);
            }
        }

        return matrix;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static float [][] scale_matrix(float[][] matrix, int scale) {
        int n = matrix.length;      // rows
        int p = matrix[0].length;   // columns
        float sum = 0;

        float [][] nmatrix = new float[n*scale][p*scale];

        for (int i=0; i<n; i++){
            for (int j=0; j<p; j++){
                for (int si=0; si<scale; si++){
                    for (int sj=0; sj<scale; sj++){
                        //println(si, sj);
                        int a1 = (i*scale)+si;
                        int a2 = (j*scale)+sj;
                        float a3 = matrix[i][j];
                        //println( a1 + ", " + a2 + " = " + a3 );
                        //nmatrix[(i*scale)+si][(j*scale)+sj] = matrix[i][j];
                        nmatrix[a1][a2] = a3;
                    }
                }
            }
            //println();
        }
        //println("scale_matrix: " + scale);
        return nmatrix;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void print_matrix(float[][] matrix) {
        int n = matrix.length;      // rows
        int p = matrix[0].length;   // columns
        float sum = 0;

        for (int i=0; i<n; i++){
            for (int j=0; j<p; j++){
                sum += matrix[i][j];
                System.out.printf("%10.5f ", matrix[i][j]);
            }
            println();
        }
        println("Sum: ", sum);
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////

}
