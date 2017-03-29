///////////////////////////////////////////////////////////////////////////////////////////////////////
// My Drawbot, "Death to Sharpie"
// Jpeg to gcode simplified (kinda sorta works version, v3.5 (beta))
//
// Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
//
// Open creative GPL source commons with some BSD public GNU foundation stuff sprinkled in...
// If anything here is remotely useable, please give me a shout.
//
// Useful math stuff:
//      http://members.chello.at/~easyfilter/bresenham.html
// GClip stuff:
//      https://forum.processing.org/two/discussion/6179/why-does-not-it-run-clipboard
///////////////////////////////////////////////////////////////////////////////////////////////////////
import java.util.Map;
import processing.pdf.*;

// Set path finding module to use
Pfm_original  pfm;
//Pfm_your_version  pfm;

// Constants 
final float   paper_size_x = 32 * 25.4;
final float   paper_size_y = 40 * 25.4;
final float   image_size_x = 28 * 25.4;
final float   image_size_y = 36 * 25.4;
final float   paper_top_to_origin = 285;  //mm, make smaller to move drawing down on paper
final int     pen_count = 6;


// Every good program should have a shit pile of badly named globals.
int     state = 1;
int     pen_selected = 0;
int     current_copic_set = 0;
int     display_line_count;
String  display_mode = "drawing";
PImage  img_orginal;               // The original image
PImage  img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw. 
PImage  img;                       // Used during drawing for current brightness levels.  Gets damaged during drawing.
float   gcode_offset_x;
float   gcode_offset_y;
float   gcode_scale;
float   screen_scale;
float   screen_scale_org;
int     screen_rotate = 0;
float   old_x = 0;
float   old_y = 0;
int     mx = 0;
int     my = 0;
int     morgx = 0;
int     morgy = 0;
int     pen_color = 0;
boolean is_pen_down;
boolean is_grid_on = false;
String  path_selected = "";
String  file_selected = "";
String  basefile_selected = "";
String  gcode_comments = "";
int     startTime = 0;

Limit   dx, dy;
Copix   copic;
PrintWriter OUTPUT;
botDrawing d1;

float[] pen_distribution = new float[pen_count];

String[][] copic_sets = {
  {"100", "N10", "N8", "N6", "N4", "N2"},       // Dark Greys
  {"100", "100", "N7", "N5", "N3", "N2"},       // Light Greys
  {"100", "W10", "W8", "W6", "W4", "W2"},       // Warm Greys
  {"100", "C10", "C8", "C6", "C4", "C2"},       // Cool Greys
  {"100", "100", "C7", "W5", "C3", "W2"},       // Mixed Greys
  {"100", "100", "W7", "C5", "W3", "C2"},       // Mixed Greys
  {"100", "100", "E49", "E27", "E13", "E00"},   // Browns
  {"100", "100", "E49", "E27", "E13", "N2"},    // Dark Grey Browns
  {"100", "100", "E49", "E27", "N4", "N2"},     // Browns
  {"100", "100", "E49", "N6", "N4", "N2"},      // Dark Grey Browns
  {"100", "100", "B37", "N6", "N4", "N2"},      // Dark Grey Blues
  {"100", "100", "R59", "N6", "N4", "N2"},      // Dark Grey Red
  {"100", "100", "G29", "N6", "N4", "N2"},      // Dark Grey Violet
  {"100", "100", "YR09", "N6", "N4", "N2"},     // Dark Grey Orange
  {"100", "100", "B39", "G28", "B26", "G14"},   // Blue Green
  {"100", "100", "B39", "V09", "B02", "V04"},   // Purples
  {"100", "E29", "R29", "R27", "R24", "R20"},   // Reds
  {"100", "E29", "YG99", "Y17", "YG03", "Y11"}, // Yellow, green
};



///////////////////////////////////////////////////////////////////////////////////////////////////////
void setup() {
  size(1415, 1100, P3D);
  surface.setResizable(true);
  colorMode(RGB);
  frameRate(999);
  //randomSeed(millis());
  randomSeed(3);
  d1 = new botDrawing();
  dx = new Limit(); 
  dy = new Limit(); 
  copic = new Copix();
  pfm = new Pfm_original(); 
  //pfm = new Pfm_your_version(); 
    
  // If the clipboard contains a URL, try to download the picture instead of using local storage.
  String url = GClip.paste();
  if (match(url, "^https?:..") != null) {
    println("URL found: "+ url);
    path_selected = url;
    state++;
  } else {
    println("URL not found on clipboard");
    selectInput("Select a image to process:", "fileSelected");
  }
  
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void draw() {
  frame.setLocation(200, 200);
  if (state != 3) { background(255, 255, 255); }
  scale(screen_scale);
  translate(mx, my);
  rotate(PI/2*screen_rotate);
  
  switch(state) {
  case 1: 
    // Waiting for filename selection
    break;
  case 2: 
    //println("State=2, Setup squiggles");
    setup_squiggles();
    startTime = millis();
    break;
  case 3: 
    //println("State=3, Drawing image");
    if (display_line_count <= 1) {
      background(255);
    } 
    pfm.find_path();
    display_line_count = d1.line_count;
    break;
  case 4: 
    pfm.post_processing();
    println("Elapsed time:  ", millis() - startTime);
    close_files_and_make_images();
    break;
  case 5: 
    render_all();
    noLoop();
    break;
  default:
    println("Invalid state: " + state);
    break;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    path_selected = selection.getAbsolutePath();
    file_selected = selection.getName();
    String[] fileparts = split(file_selected, '.');
    basefile_selected = fileparts[0];
    println("User selected " + path_selected);
    //println("User selected " + file_selected);
    //println("User selected " + basefile_selected);
    state++;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void setup_squiggles() {
  float   gcode_scale_x;
  float   gcode_scale_y;
  float   screen_scale_x;
  float   screen_scale_y;

  println("setup_squiggles running...");
  //randomSeed(millis());
  img_orginal = loadImage(path_selected, "jpeg");  // Load the image into the program  
  gcode_comment("loadImage: " + path_selected);

  img = createImage(img_orginal.width, img_orginal.height, RGB);
  img.copy(img_orginal, 0, 0, img_orginal.width, img_orginal.height, 0, 0, img_orginal.width, img_orginal.height);
  image_rotate();

  pfm.pre_processing();
  img.loadPixels();
  img_reference = createImage(img.width, img.height, RGB);
  img_reference.copy(img, 0, 0, img.width, img.height, 0, 0, img.width, img.height);
  
  gcode_scale_x = image_size_x / img.width;
  gcode_scale_y = image_size_y / img.height;
  gcode_scale = min(gcode_scale_x, gcode_scale_y);
  gcode_offset_x = - (img.width * gcode_scale / 2.0);  
  gcode_offset_y = - (paper_top_to_origin - (paper_size_y - (img.height * gcode_scale)) / 2.0);

  screen_scale_x = width / (float)img.width;
  screen_scale_y = height / (float)img.height;
  screen_scale = min(screen_scale_x, screen_scale_y);
  screen_scale_org = screen_scale;
  
  gcode_comment("Image dimensions: " + img.width + " by " + img.height);
  gcode_comment("Paper size: " + nf(paper_size_x,0,2) + " by " + nf(paper_size_y,0,2) + "      " + nf(paper_size_x/25.4,0,2) + " by " + nf(paper_size_y/25.4,0,2));
  gcode_comment("Max image size: " + nf(image_size_x,0,2) + " by " + nf(image_size_y,0,2) + "      " + nf(image_size_x/25.4,0,2) + " by " + nf(image_size_y/25.4,0,2));
  gcode_comment("Calc image size " + nf(img.width * gcode_scale,0,2) + " by " + nf(img.height * gcode_scale,0,2) + "      " + nf(img.width * gcode_scale/25.4,0,2) + " by " + nf(img.height * gcode_scale/25.4,0,2));
  //gcode_comment("Gcode scale X:  " + nf(gcode_scale_x,0,2));
  //gcode_comment("Gcode scale Y:  " + nf(gcode_scale_y,0,2));
  //gcode_comment("Gcode scale:    " + nf(gcode_scale,0,2));
  //gcode_comment("Screen scale X: " + nf(screen_scale_x,0,2));
  //gcode_comment("Screen scale Y: " + nf(screen_scale_y,0,2));
  //gcode_comment("Screen scale:   " + nf(screen_scale,0,2));
  gcode_comment("Gcode offset X: " + nf(gcode_offset_x,0,2));  
  gcode_comment("Gcode offset Y: " + nf(gcode_offset_y,0,2));  
  pfm.output_parameters();

  state++;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void grid() {
  // This will give you a rough idea of the size of the printed image, in inches.
  // Some screen scales smaller than 1.0 will sometimes display every other line
  // It looks like a big logic bug, but it just can't display a one pixel line scaled down well.
  
  blendMode(BLEND);
  if(is_grid_on) {
    int image_center_x = int(img.width / 2);
    int image_center_y = int(img.height / 2);
    int gridlines = 100;
    
    stroke(255, 64, 64, 70);
    // Vertical lines
    for (int x = -gridlines; x <= gridlines; x++) {
      int x0 = int(x * 25.4 * (1 / gcode_scale));
      line(x0 + image_center_x, -5000, x0 + image_center_x, 5000);
    }
  
    // Horizontal lines
    for (int y = -gridlines; y <= gridlines; y++) {
      int y0 = int(y * 25.4 * (1 / gcode_scale));
      line(-5000, y0 + image_center_y, 5000, y0 + image_center_y);
    }
    
    // Screen center line
    stroke(255, 64, 64, 70);
    strokeWeight(2);
    line(image_center_x, 0, image_center_x, 200000);
    line(0, image_center_y, width, image_center_y);
    strokeWeight(1);
  
    hint(DISABLE_DEPTH_TEST);      // Allow fills to be shown on top.
    
    // Faint red image rect
    fill(255, 0, 0, 8);
    rect(0,0,img.width, img.height);

    // Green pen origin dot.
    stroke(0, 255, 0, 255);
    fill(0, 255, 0, 255);
    ellipse(-gcode_offset_x / gcode_scale, -gcode_offset_y / gcode_scale, 10, 10);
    
    // Red center dot
    stroke(255, 0, 0, 255);
    fill(255, 0, 0, 255);
    ellipse(image_center_x, image_center_y, 10, 10);
    
    // Blue dot at 0,0
    stroke(0, 0, 255, 255);
    fill(0, 0, 255, 255);
    ellipse(0, 0, 10, 10);

    hint(ENABLE_DEPTH_TEST);
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void close_files_and_make_images() {
  display_line_count = d1.line_count;
  
  gcode_comment ("Extreams of X: " + dx.min + " thru " + dx.max);
  gcode_comment ("Extreams of Y: " + dy.min + " thru " + dy.max);
  state++;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void save_jpg() {
  PImage  img_drawing;
  PImage  img_drawing2;

  //img_drawing = createImage(img.width, img.height, RGB);
  //img_drawing.copy(0, 0, img.width, img.height, 0, 0, img.width, img.height);
  //img_drawing.save("what the duce.jpg");

  // Save resuling image
  save("tmptif.tif");
  img_drawing = loadImage("tmptif.tif");
  img_drawing2 = createImage(img.width, img.height, RGB);
  img_drawing2.copy(img_drawing, 0, 0, img.width, img.height, 0, 0, img.width, img.height);
  img_drawing2.save(sketchPath("") + "drawings\\" + basefile_selected + ".jpg");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void render_all() {
  println("render_all: " + display_mode + ", " + display_line_count + " lines, with pen set " + current_copic_set);
  
  if (display_mode == "drawing") {
    //<d1.render_all();
    d1.render_some(display_line_count);
  }

  if (display_mode == "pen") {
    //image(img, 0, 0);
    d1.render_one_pen(display_line_count, pen_selected);
  }
  
  if (display_mode == "original") {
    image(img_orginal, 0, 0);
  }

  if (display_mode == "reference") {
    image(img_reference, 0, 0);
  }
  
  if (display_mode == "lightened") {
    image(img, 0, 0);
  }
  grid();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void keyPressed() {

  if (key == 'd') { display_mode = "drawing";   }
  if (key == 'O') { display_mode = "original";  }
  if (key == 'o') { display_mode = "reference";  }
  if (key == 'l') { display_mode = "lightened"; }
  if (key == 'Q') { display_mode = "pen";  pen_selected = 0; }
  if (key == 'W') { display_mode = "pen";  pen_selected = 1; }
  if (key == 'E') { display_mode = "pen";  pen_selected = 2; }
  if (key == 'R') { display_mode = "pen";  pen_selected = 3; }
  if (key == 'T') { display_mode = "pen";  pen_selected = 4; }
  if (key == 'Y') { display_mode = "pen";  pen_selected = 5; }
  if (key == 'G') { is_grid_on = ! is_grid_on; }
  if (key == ']') { screen_scale *= 1.05; }
  if (key == '[') { screen_scale *= 1 / 1.05; }
  if (key == '1') { pen_distribution[0] *= 1.1; }
  if (key == '2') { pen_distribution[1] *= 1.1; }
  if (key == '3') { pen_distribution[2] *= 1.1; }
  if (key == '4') { pen_distribution[3] *= 1.1; }
  if (key == '5') { pen_distribution[4] *= 1.1; }
  if (key == '6') { pen_distribution[5] *= 1.1; }
  if (key == '!') { pen_distribution[0] *= 0.9; }
  if (key == '@') { pen_distribution[1] *= 0.9; }
  if (key == '#') { pen_distribution[2] *= 0.9; }
  if (key == '$') { pen_distribution[3] *= 0.9; }
  if (key == '%') { pen_distribution[4] *= 0.9; }
  if (key == '^') { pen_distribution[5] *= 0.9; }
  if (key == 't') { set_even_distribution(); }
  if (key == 'y') { set_black_distribution(); }
  if (key == '}') { current_copic_set++; }
  if (key == '{') { current_copic_set--; } 
  if (key == 's') { if (state == 3) { state++; } }
  if (key == '9') {
    pen_distribution[0] *= 0.90;
    pen_distribution[1] *= 0.95;
    pen_distribution[2] *= 1.00;
    pen_distribution[3] *= 1.05;
    pen_distribution[4] *= 1.10;
    pen_distribution[5] *= 1.15;
  }
  if (key == '0') {
    pen_distribution[0] *= 1.10;
    pen_distribution[1] *= 1.05;
    pen_distribution[2] *= 1.00;
    pen_distribution[3] *= 0.95;
    pen_distribution[4] *= 0.90;
    pen_distribution[5] *= 0.85;
  }
  if (key == 'g') { 
    create_gcode_files(display_line_count);
    create_gcode_test_file ();
    d1.render_to_pdf(display_line_count);
    //save_jpg();
  }

  if (key == '\\') { screen_scale = screen_scale_org; screen_rotate=0; mx=0; my=0; }
  if (key == '<') {
    int delta = -10000;
    display_line_count = int(display_line_count + delta);
    if (display_line_count < 0) { display_line_count = 0; }
    display_line_count = constrain(display_line_count, 0, d1.line_count);
    println("display_line_count: " + display_line_count);
  }
  if (key == '>') {
    int delta = 10000;
    display_line_count = int(display_line_count + delta);
    display_line_count = constrain(display_line_count, 0, d1.line_count);
    println("display_line_count: " + display_line_count);
  }
  if (key == CODED) {
    int delta = 15;
    if (keyCode == UP)    { my+= delta; };
    if (keyCode == DOWN)  { my-= delta; };
    if (keyCode == RIGHT) { mx-= delta; };
    if (keyCode == LEFT)  { mx+= delta; };
  }
  if (key == 'r') { 
    screen_rotate ++;
    if (screen_rotate == 4) { screen_rotate = 0; }
    
    switch(screen_rotate) {
      case 0: 
        my -= img.height;
        break;
      case 1: 
        mx += img.height;
        break;
      case 2: 
        my += img.height;
        break;
      case 3: 
        mx -= img.height;
        break;
     }
  }
  
  current_copic_set = constrain(current_copic_set, 0, copic_sets.length - 1);
  normalize_distribution();
  d1.distribute_pen_changes_according_to_percentages(display_line_count, pen_count);
  //surface.setSize(img.width, img.height);
  redraw();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void set_even_distribution() {
  println();
  for (int p = 0; p<pen_count; p++) {
    pen_distribution[p] = display_line_count / pen_count;
    println("pen_distribution[" + p + "] = " + pen_distribution[p]);
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void set_black_distribution() {
  println();
  for (int p=0; p<pen_count; p++) {
    pen_distribution[p] = 0;
    println("pen_distribution[" + p + "] = " + pen_distribution[p]);
  }
  pen_distribution[0] = display_line_count;
  
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void normalize_distribution() {
  float total = 0;
  for (int p=0; p<pen_count; p++) {
    total = total + pen_distribution[p];
  }
  
  println();
  println("normalize_distribution");
  for (int p = 0; p<pen_count; p++) {
    pen_distribution[p] = display_line_count * pen_distribution[p] / total;
    print("pen_distribution[" + p + "] =" );
    System.out.printf("%7.0f  ", pen_distribution[p]);
    for (int s = 0; s<int(pen_distribution[p]/total*100); s++) {
      print("*");
    }
    println();
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void mousePressed() {
  morgx = mouseX - mx; 
  morgy = mouseY - my; 
  redraw();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void mouseDragged() {
  mx = mouseX-morgx; 
  my = mouseY-morgy; 
  redraw();
}