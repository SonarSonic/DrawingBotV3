///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_threshold() {
  gcode_comment("Thresholed");
  img.filter(THRESHOLD);
}
  
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_desaturate() {
  gcode_comment("Desaturated");
  img.filter(GRAY);
}
  
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_invert() {
  gcode_comment("Inverted");
  img.filter(INVERT);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_posterize(int amount) {
  gcode_comment("Posterized");
  img.filter(POSTERIZE, amount);
}
  
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_blur(int amount) {
  gcode_comment("Blured");
  img.filter(BLUR, amount);
}
 
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_erode() {
  gcode_comment("Eroded");
  img.filter(ERODE);
}
  
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_dilate() {
  gcode_comment("Dilated");
  img.filter(DILATE);
}
  
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_rotate() {
  //image[y][x]                                     // assuming this is the original orientation
  //image[x][original_width - y]                    // rotated 90 degrees ccw
  //image[original_height - x][y]                   // 90 degrees cw
  //image[original_height - y][original_width - x]  // 180 degrees

  if (img.width > img.height) {
    gcode_comment("Rotated");
    PImage img2 = createImage(img.height, img.width, RGB);
    img.loadPixels();
    for (int x=1; x<img.width; x++) {
      for (int y=1; y<img.height; y++) {
        int loc1  = x + y*img.width;
        int loc2 = y + (img.width - x) * img2.width;
        img2.pixels[loc2] = img.pixels[loc1];
      }
    }
    img = img2;
    updatePixels();
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void lighten_one_pixel(int adjustbrightness, int x, int y) {
  int loc = (y)*img.width + x;
  float r = brightness (img.pixels[loc]);
  //r += adjustbrightness;
  r += adjustbrightness + random(0, 0.01);
  r = constrain(r,0,255);
  color c = color(r);
  img.pixels[loc] = c;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_scale(int new_width) {
  if (img.width != new_width) {
    gcode_comment("Resizing, dimensions were " + img.width + " by " + img.height);
    img.resize(new_width, 0);
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
float avg_imgage_brightness() {
  float b = 0.0;

  for (int p=0; p < img.width * img.height; p++) {
    b += brightness(img.pixels[p]);
  }
  
  return(b / (img.width * img.height));
}
  
///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_crop() {
  // This will center crop to the desired image size image_size_x and image_size_y
  
  PImage img2;
  float desired_ratio = image_size_x / image_size_y;
  float current_ratio = (float)img.width / (float)img.height;
  
  gcode_comment("Cropping image to desired ratio of " + desired_ratio);
  gcode_comment("Current image: " + img.width + "x" + img.height + "     ratio: " + current_ratio);
  
  if ( current_ratio < desired_ratio ) {
    int desired_x = img.width;
    int desired_y = int(img.width / desired_ratio);
    int half_y = (img.height - desired_y) / 2;
    img2 = createImage(desired_x, desired_y, RGB);
    img2.copy(img, 0, half_y, desired_x, desired_y, 0, 0, desired_x, desired_y);
  } else {
    int desired_x = int(img.height * desired_ratio);
    int desired_y = img.height;
    int half_x = (img.width - desired_x) / 2;
    img2 = createImage(desired_x, desired_y, RGB);
    img2.copy(img, half_x, 0, desired_x, desired_y, 0, 0, desired_x, desired_y);
  }

  img = img2;
  gcode_comment("Cropped image: " + img.width + "x" + img.height + "     ratio: " + (float)img.width / (float)img.height);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_boarder(String fname, int shrink, int blur) {
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
  
  PImage boarder = createImage(img.width+(shrink*2), img.height+(shrink*2), RGB);
  PImage temp_boarder = loadImage("boarder/" + fname);
  temp_boarder.filter(GRAY);
  temp_boarder.filter(INVERT);
  temp_boarder.filter(BLUR, blur);
  
  boarder.copy(temp_boarder, 0, 0, temp_boarder.width, temp_boarder.height, 0, 0, boarder.width, boarder.height);
  img.blend(boarder, shrink, shrink, img.width, img.height,  0, 0, img.width, img.height, ADD); 
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void image_sharpen() {
  // It's possible to perform a convolution the image with different matrices
  // Simple sharpen matrix
  //float[][] matrix = { {  0, -1,  0 },
  //                     { -1,  5, -1 },
  //                     {  0, -1,  0 } }; 
  
  // Source:  https://www.taylorpetrick.com/blog/post/convolution-part3
  // subtle unsharp matrix
  float[][] matrix = { { -0.00391, -0.01563, -0.02344, -0.01563, -0.00391 },
                       { -0.01563, -0.06250, -0.09375, -0.06250, -0.01563 },
                       { -0.02344, -0.09375,  1.85980, -0.09375, -0.02344 },
                       { -0.01563, -0.06250, -0.09375, -0.06250, -0.01563 },
                       { -0.00391, -0.01563, -0.02344, -0.01563, -0.00391 } };
  
  PImage simg = createImage(img.width, img.height, RGB);
  simg.copy(img, 0, 0, img.width, img.height, 0, 0, simg.width, simg.height);
  int matrixsize = 5;
  for (int x = 0; x < simg.width; x++) {
    for (int y = 0; y < simg.height; y++ ) {
      color c = convolution(x, y, matrix, matrixsize, simg);
      int loc = x + y*simg.width;
      img.pixels[loc] = c;
    }
  }
  updatePixels();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
// Source:  https://py.processing.org/tutorials/pixels/
// Daniel Shiffman

color convolution(int x, int y, float[][] matrix, int matrixsize, PImage img) {
  float rtotal = 0.0;
  float gtotal = 0.0;
  float btotal = 0.0;
  int offset = matrixsize / 2;
  // Loop through convolution matrix
  for (int i = 0; i < matrixsize; i++){
    for (int j= 0; j < matrixsize; j++){
      // What pixel are we testing
      int xloc = x+i-offset;
      int yloc = y+j-offset;
      int loc = xloc + img.width*yloc;
      // Make sure we have not walked off the edge of the pixel array
      loc = constrain(loc,0,img.pixels.length-1);
      // Calculate the convolution
      // We sum all the neighboring pixels multiplied by the values in the convolution matrix.
      rtotal += (red(img.pixels[loc]) * matrix[i][j]);
      gtotal += (green(img.pixels[loc]) * matrix[i][j]);
      btotal += (blue(img.pixels[loc]) * matrix[i][j]);
    }
  }
  // Make sure RGB is within range
  rtotal = constrain(rtotal,0,255);
  gtotal = constrain(gtotal,0,255);
  btotal = constrain(btotal,0,255);
  // Return the resulting color
  return color(rtotal,gtotal,btotal);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////