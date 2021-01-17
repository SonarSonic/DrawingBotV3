///////////////////////////////////////////////////////////////////////////////////////////////////////
// My Drawbot, "Death to Sharpie"
// Jpeg to gcode simplified (kinda sorta works version, v3.75 (beta))
//
// Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
//
// Open creative GPL source commons with some BSD public GNU foundation stuff sprinkled in...
// If anything here is remotely useable, please give me a shout.
//
// Useful math:    http://members.chello.at/~easyfilter/bresenham.html
// GClip:          https://forum.processing.org/two/discussion/6179/why-does-not-it-run-clipboard
// Dynamic class:  https://processing.org/discourse/beta/num_1262759715.html
///////////////////////////////////////////////////////////////////////////////////////////////////////
import java.util.Map;
import processing.pdf.*;


// Constants 
final float   paper_size_x = 32 * 25.4;
final float   paper_size_y = 40 * 25.4;
final float   image_size_x = 28 * 25.4;
final float   image_size_y = 36 * 25.4;
final float   paper_top_to_origin = 285;      //mm, make smaller to move drawing down on paper
final float   pen_width = 0.65;               //mm, determines image_scale, reduce, if solid black areas are speckled with white holes.
final int     pen_count = 6;
final char    gcode_decimal_seperator = '.';    
final int     gcode_decimals = 2;             // Number of digits right of the decimal point in the gcode files.
final int     svg_decimals = 2;               // Number of digits right of the decimal point in the SVG file.
final float   grid_scale = 25.4;              // Use 10.0 for centimeters, 25.4 for inches, and between 444 and 529.2 for cubits.


// Every good program should have a shit pile of badly named globals.
Class cl = null;
pfm ocl;
int current_pfm = 0;
String[] pfms = {"PFM_original", "PFM_spiral", "PFM_squares"}; 

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
boolean ctrl_down = false;

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
  {"100", "100", "R29", "R27", "R24", "R20"},   // Reds
  {"100", "E29", "YG99", "Y17", "YG03", "Y11"} // Yellow, green
};



///////////////////////////////////////////////////////////////////////////////////////////////////////
void setup() {
  size(1415, 900, P3D);
  frame.setLocation(200, 200);
  surface.setResizable(true);
  surface.setTitle("Drawbot_image_to_gcode_v2, version 3.75");
  colorMode(RGB);
  frameRate(999);
  //randomSeed(millis());
  randomSeed(3);
  d1 = new botDrawing();
  dx = new Limit(); 
  dy = new Limit(); 
  copic = new Copix();
  loadInClass(pfms[current_pfm]);

  // If the clipboard contains a URL, try to download the picture instead of using local storage.
  String url = GClip.paste();
  if (match(url.toLowerCase(), "^https?:...*(jpg|png)") != null) {
    println("Image URL found on clipboard: "+ url);
    path_selected = url;
    state++;
  } else {
    println("image URL not found on clipboard");
    selectInput("Select an image to process:", "fileSelected");
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void draw() {
  if (state != 3) { background(255, 255, 255); }
  scale(screen_scale);
  translate(mx, my);
  rotate(HALF_PI*screen_rotate);
  
  switch(state) {
  case 1: 
    //println("State=1, Waiting for filename selection");
    break;
  case 2:
    //println("State=2, Setup squiggles");
    loop();
    setup_squiggles();
    startTime = millis();
    break;
  case 3: 
    //println("State=3, Drawing image");
    if (display_line_count <= 1) {
      background(255);
    } 
    ocl.find_path();
    display_line_count = d1.line_count;
    break;
  case 4: 
    println("State=4, pfm.post_processing");
    ocl.post_processing();

    set_even_distribution();
    normalize_distribution();
    d1.evenly_distribute_pen_changes(d1.get_line_count(), pen_count);
    d1.distribute_pen_changes_according_to_percentages(display_line_count, pen_count);

    println("elapsed time: " + (millis() - startTime) / 1000.0 + " seconds");
    display_line_count = d1.line_count;
  
    gcode_comment ("extreams of X: " + dx.min + " thru " + dx.max);
    gcode_comment ("extreams of Y: " + dy.min + " thru " + dy.max);
    state++;
    break;
  case 5: 
    render_all();
    noLoop();
    break;
  default:
    println("invalid state: " + state);
    break;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void fileSelected(File selection) {
  if (selection == null) {
    println("no image file selected, exiting program.");
    exit();
  } else {
    path_selected = selection.getAbsolutePath();
    file_selected = selection.getName();
    String[] fileparts = split(file_selected, '.');
    basefile_selected = fileparts[0];
    println("user selected: " + path_selected);
    //println("user selected: " + file_selected);
    //println("user selected: " + basefile_selected);
    state++;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void setup_squiggles() {
  float   gcode_scale_x;
  float   gcode_scale_y;
  float   screen_scale_x;
  float   screen_scale_y;

  //println("setup_squiggles...");

  d1.line_count = 0;
  //randomSeed(millis());
  img = loadImage(path_selected, "jpeg");  // Load the image into the program  
  gcode_comment("loaded image: " + path_selected);

  image_rotate();

  img_orginal = createImage(img.width, img.height, RGB);
  img_orginal.copy(img, 0, 0, img.width, img.height, 0, 0, img.width, img.height);

  ocl.pre_processing();
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
  
  gcode_comment("final dimensions: " + img.width + " by " + img.height);
  gcode_comment("paper_size: " + nf(paper_size_x,0,2) + " by " + nf(paper_size_y,0,2) + "      " + nf(paper_size_x/25.4,0,2) + " by " + nf(paper_size_y/25.4,0,2));
  gcode_comment("drawing size max: " + nf(image_size_x,0,2) + " by " + nf(image_size_y,0,2) + "      " + nf(image_size_x/25.4,0,2) + " by " + nf(image_size_y/25.4,0,2));
  gcode_comment("drawing size calculated " + nf(img.width * gcode_scale,0,2) + " by " + nf(img.height * gcode_scale,0,2) + "      " + nf(img.width * gcode_scale/25.4,0,2) + " by " + nf(img.height * gcode_scale/25.4,0,2));
  gcode_comment("gcode_scale X:  " + nf(gcode_scale_x,0,2));
  gcode_comment("gcode_scale Y:  " + nf(gcode_scale_y,0,2));
  gcode_comment("gcode_scale:    " + nf(gcode_scale,0,2));
  //gcode_comment("screen_scale X: " + nf(screen_scale_x,0,2));
  //gcode_comment("screen_scale Y: " + nf(screen_scale_y,0,2));
  //gcode_comment("screen_scale:   " + nf(screen_scale,0,2));
  ocl.output_parameters();

  state++;
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
void keyReleased() {
  if (keyCode == CONTROL) { ctrl_down = false; }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void keyPressed() {
  if (keyCode == CONTROL) { ctrl_down = true; }

  if (key == 'p') {
    current_pfm ++;
    if (current_pfm >= pfms.length) { current_pfm = 0; }
    //display_line_count = 0;
    loadInClass(pfms[current_pfm]); 
    state = 2;
  }
  
  if (key == 'd') { display_mode = "drawing";   }
  if (key == 'O') { display_mode = "original";  }
  if (key == 'o') { display_mode = "reference";  }
  if (key == 'l') { display_mode = "lightened"; }
  if (keyCode == 49 && ctrl_down && pen_count > 0) { display_mode = "pen";  pen_selected = 0; }  // ctrl 1
  if (keyCode == 50 && ctrl_down && pen_count > 1) { display_mode = "pen";  pen_selected = 1; }  // ctrl 2
  if (keyCode == 51 && ctrl_down && pen_count > 2) { display_mode = "pen";  pen_selected = 2; }  // ctrl 3
  if (keyCode == 52 && ctrl_down && pen_count > 3) { display_mode = "pen";  pen_selected = 3; }  // ctrl 4
  if (keyCode == 53 && ctrl_down && pen_count > 4) { display_mode = "pen";  pen_selected = 4; }  // ctrl 5
  if (keyCode == 54 && ctrl_down && pen_count > 5) { display_mode = "pen";  pen_selected = 5; }  // ctrl 6
  if (keyCode == 55 && ctrl_down && pen_count > 6) { display_mode = "pen";  pen_selected = 6; }  // ctrl 7
  if (keyCode == 56 && ctrl_down && pen_count > 7) { display_mode = "pen";  pen_selected = 7; }  // ctrl 8
  if (keyCode == 57 && ctrl_down && pen_count > 8) { display_mode = "pen";  pen_selected = 8; }  // ctrl 9
  if (keyCode == 48 && ctrl_down && pen_count > 9) { display_mode = "pen";  pen_selected = 9; }  // ctrl 0
  if (key == 'G') { is_grid_on = ! is_grid_on; }
  if (key == ']') { screen_scale *= 1.05; }
  if (key == '[') { screen_scale *= 1 / 1.05; }
  if (key == '1' && pen_count > 0) { pen_distribution[0] *= 1.1; }
  if (key == '2' && pen_count > 1) { pen_distribution[1] *= 1.1; }
  if (key == '3' && pen_count > 2) { pen_distribution[2] *= 1.1; }
  if (key == '4' && pen_count > 3) { pen_distribution[3] *= 1.1; }
  if (key == '5' && pen_count > 4) { pen_distribution[4] *= 1.1; }
  if (key == '6' && pen_count > 5) { pen_distribution[5] *= 1.1; }
  if (key == '7' && pen_count > 6) { pen_distribution[6] *= 1.1; }
  if (key == '8' && pen_count > 7) { pen_distribution[7] *= 1.1; }
  if (key == '9' && pen_count > 8) { pen_distribution[8] *= 1.1; }
  if (key == '0' && pen_count > 9) { pen_distribution[9] *= 1.1; }
  if (key == '!' && pen_count > 0) { pen_distribution[0] *= 0.9; }
  if (key == '@' && pen_count > 1) { pen_distribution[1] *= 0.9; }
  if (key == '#' && pen_count > 2) { pen_distribution[2] *= 0.9; }
  if (key == '$' && pen_count > 3) { pen_distribution[3] *= 0.9; }
  if (key == '%' && pen_count > 4) { pen_distribution[4] *= 0.9; }
  if (key == '^' && pen_count > 5) { pen_distribution[5] *= 0.9; }
  if (key == '&' && pen_count > 6) { pen_distribution[6] *= 0.9; }
  if (key == '*' && pen_count > 7) { pen_distribution[7] *= 0.9; }
  if (key == '(' && pen_count > 8) { pen_distribution[8] *= 0.9; }
  if (key == ')' && pen_count > 9) { pen_distribution[9] *= 0.9; }
  if (key == 't') { set_even_distribution(); }
  if (key == 'y') { set_black_distribution(); }
  if (key == 'x') { mouse_point(); }  
  if (key == '}' && current_copic_set < copic_sets.length -1) { current_copic_set++; }
  if (key == '{' && current_copic_set >= 1)                   { current_copic_set--; }
  
  if (key == 's') { if (state == 3) { state++; } }
  if (keyCode == 65 && ctrl_down)  {
    println("Holly freak, Ctrl-A was pressed!");
  }
  if (key == '9') {
    if (pen_count > 0) { pen_distribution[0] *= 1.00; }
    if (pen_count > 1) { pen_distribution[1] *= 1.05; }
    if (pen_count > 2) { pen_distribution[2] *= 1.10; }
    if (pen_count > 3) { pen_distribution[3] *= 1.15; }
    if (pen_count > 4) { pen_distribution[4] *= 1.20; }
    if (pen_count > 5) { pen_distribution[5] *= 1.25; }
    if (pen_count > 6) { pen_distribution[6] *= 1.30; }
    if (pen_count > 7) { pen_distribution[7] *= 1.35; }
    if (pen_count > 8) { pen_distribution[8] *= 1.40; }
    if (pen_count > 9) { pen_distribution[9] *= 1.45; }
  }
  if (key == '0') {
    if (pen_count > 0) { pen_distribution[0] *= 1.00; }
    if (pen_count > 1) { pen_distribution[1] *= 0.95; }
    if (pen_count > 2) { pen_distribution[2] *= 0.90; }
    if (pen_count > 3) { pen_distribution[3] *= 0.85; }
    if (pen_count > 4) { pen_distribution[4] *= 0.80; }
    if (pen_count > 5) { pen_distribution[5] *= 0.75; }
    if (pen_count > 6) { pen_distribution[6] *= 0.70; }
    if (pen_count > 7) { pen_distribution[7] *= 0.65; }
    if (pen_count > 8) { pen_distribution[8] *= 0.60; }
    if (pen_count > 9) { pen_distribution[9] *= 0.55; }
}
  if (key == 'g') { 
    create_gcode_files(display_line_count);
    create_gcode_test_file ();
    create_svg_file(display_line_count);
    d1.render_to_pdf(display_line_count);
    d1.render_each_pen_to_pdf(display_line_count);
  }

  if (key == '\\') { screen_scale = screen_scale_org; screen_rotate=0; mx=0; my=0; }
  if (key == '<') {
    int delta = -10000;
    display_line_count = int(display_line_count + delta);
    display_line_count = constrain(display_line_count, 0, d1.line_count);
    //println("display_line_count: " + display_line_count);
  }
  if (key == '>') {
    int delta = 10000;
    display_line_count = int(display_line_count + delta);
    display_line_count = constrain(display_line_count, 0, d1.line_count);
    //println("display_line_count: " + display_line_count);
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
  
  normalize_distribution();
  d1.distribute_pen_changes_according_to_percentages(display_line_count, pen_count);
  //surface.setSize(img.width, img.height);
  redraw();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void set_even_distribution() {
  println("set_even_distribution");
  for (int p = 0; p<pen_count; p++) {
    pen_distribution[p] = display_line_count / pen_count;
    //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void set_black_distribution() {
  println("set_black_distribution");
  for (int p=0; p<pen_count; p++) {
    pen_distribution[p] = 0;
    //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
  }
  pen_distribution[0] = display_line_count;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void normalize_distribution() {
  float total = 0;

  println();
  //println("normalize_distribution");

  for (int p=0; p<pen_count; p++) {
    total = total + pen_distribution[p];
  }
  
  for (int p = 0; p<pen_count; p++) {
    pen_distribution[p] = display_line_count * pen_distribution[p] / total;
    print("Pen " + p + ", ");
    System.out.printf("%-4s", copic_sets[current_copic_set][p]);
    System.out.printf("%8.0f  ", pen_distribution[p]);
    
    // Display approximately one star for every percent of total
    for (int s = 0; s<int(pen_distribution[p]/total*100); s++) {
      print("*");
    }
    println();
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
public void loadInClass(String pfm_name){
  String className = this.getClass().getName() + "$" + pfm_name;
  try {
    cl = Class.forName(className);
  } catch (ClassNotFoundException e) { 
    println("\nError unknown PFM: " + className); 
  }
  
  ocl = null;
  if (cl != null) {
    try {
      // Get the constructor(s)
      java.lang.reflect.Constructor[] ctors = cl.getDeclaredConstructors();
      // Create an instance with the parent object as parameter (needed for inner classes)
      ocl = (pfm) ctors[0].newInstance(new Object[] { this });
    } catch (InstantiationException e) {
      println("Cannot create an instance of " + className);
    } catch (IllegalAccessException e) {
      println("Cannot access " + className + ": " + e.getMessage());
    } catch (Exception e) {
       // Lot of stuff can go wrong...
       e.printStackTrace();
    }
  }
  println("\nloaded PFM: " + className); 
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void mousePressed() {
  morgx = mouseX - mx; 
  morgy = mouseY - my; 
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void mouseDragged() {
  mx = mouseX-morgx; 
  my = mouseY-morgy; 
  redraw();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This is the pfm interface, it contains the only methods the main code can call.
// As well as any variables that all pfm modules must have.
interface pfm {
  //public int x=0;
  public void pre_processing();
  public void find_path();
  public void post_processing();
  public void output_parameters();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
