///////////////////////////////////////////////////////////////////////////////////////////////////////
// This path finding module is the basis for nearly all my drawings.
// Find the darkest average line away from my current location and move there.
///////////////////////////////////////////////////////////////////////////////////////////////////////

class Pfm_original {

  final int    squiggle_length = 500;      // How often to lift the pen
  final int    adjustbrightness = 9;       // How fast it moves from dark to light, over-draw
  final float  desired_brightness = 251;   // How long to process.  You can always stop early with "s" key
  final int    squiggles_till_first_change = 200;

  int          tests = 13;                 // Reasonable values:  13 for development, 720 for final
  int          line_length = int(random(3, 40));  // Reasonable values:  3 through 100

  int          squiggle_count;
  int          darkest_x;
  int          darkest_y;
  float        darkest_value;
  float        darkest_neighbor = 256;

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  public void pre_processing() {
    image_crop();
    image_scale(1000);   // 900+ for final drawing
    image_sharpen();
    //image_posterize(6);
    //image_erode();
    //image_dilate();
    //image_invert();
    //image_blur(2);
    image_boarder("b3.png", 0, 0);
    image_desaturate();
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  public void find_path() {
    find_squiggle();
    if (avg_imgage_brightness() > desired_brightness ) {
      state++;
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private void find_squiggle() {
    int x, y;
  
    //find_darkest();
    find_darkest_area();
    x = darkest_x;
    y = darkest_y;
    squiggle_count++;
    pen_color = 0;
  
    find_darkest_neighbor(x, y);
    move_abs(darkest_x, darkest_y);
    pen_down();
    
    for (int s = 0; s < squiggle_length; s++) {
      find_darkest_neighbor(x, y);
      bresenham_lighten(x, y, darkest_x, darkest_y, adjustbrightness);
      move_abs(darkest_x, darkest_y);
      x = darkest_x;
      y = darkest_y;
    }
    pen_up();
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private void find_darkest() {
    darkest_value = 257;
    int darkest_loc = 0;
    
    for (int loc=0; loc < img.width * img.height; loc++) {
      float r = brightness(img.pixels[loc]);
      if (r < darkest_value) {
        darkest_value = r + random(1);
        darkest_loc = loc;
      }
    }
    darkest_x = darkest_loc % img.width;
    darkest_y = (darkest_loc-darkest_x) / img.width;
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private void find_darkest_area() {
    // Warning, Experimental: 
    // Finds the darkest square area by down sampling the img into a much smaller area then finding 
    // the darkest pixel within that.  It returns a random pixel within that darkest area.
    
    int area_size = 10;
    darkest_value = 999;
    int darkest_loc = 1;
    
    PImage img2;
    img2 = createImage(img.width / area_size, img.height / area_size, RGB);
    img2.copy(img, 0, 0, img.width, img.height, 0, 0, img2.width, img2.height);

    for (int loc=0; loc < img2.width * img2.height; loc++) {
      float r = brightness(img2.pixels[loc]);
      
      if (r < darkest_value) {
        darkest_value = r + random(1);
        darkest_loc = loc;
      }
    }
    darkest_x = darkest_loc % img2.width;
    darkest_y = (darkest_loc - darkest_x) / img2.width;
    darkest_x = darkest_x * area_size + int(random(area_size));
    darkest_y = darkest_y * area_size + int(random(area_size));
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private void find_darkest_neighbor(int start_x, int start_y) {
    darkest_neighbor = 257;
    float delta_angle;
    float start_angle;
    
    //start_angle = random(-35, -15) + cos(radians(start_x/4+(start_y/6)))*30;
    //start_angle = random(-95, -75) + cos(radians(start_y/15))*90;
    //start_angle = 36 + degrees( ( sin(radians(start_x/9+46)) + cos(radians(start_y/26+26)) ));
    //start_angle = 34 + degrees( ( sin(radians(start_x/9+46)) + cos(radians(start_y/-7+26)) ));
    //if (squiggle_count <220) { tests = 20; } else { tests = 2; }
    //start_angle = random(20, 1);       // Cuba 1
    start_angle = random(-72, -52);    // Spitfire
    //start_angle = random(-120, -140);  // skier
    //start_angle = random(-360, -1);    // gradiant magic
    //start_angle = squiggle_count % 360;
    //start_angle += squiggle_count/4;
    //start_angle = -90;
    
    //delta_angle = 180 + 10 / (float)tests;
    //delta_angle = 360.0 / (float)tests;

    if (squiggle_count < squiggles_till_first_change) { 
      //line_length = int(random(3, 60));
      delta_angle = 360.0 / (float)tests;
    } else {
      //start_angle = degrees(atan2(img.height/2.0 - start_y -470, img.width/2.0 - start_x+130) )-10+90;    // wierd spiral
      //start_angle = degrees(atan2(img.height/2.0 - start_y +145, img.width/2.0 - start_x+45) )-10+90;    //cuba car
      //start_angle = degrees(atan2(img.height/2.0 - start_y +210, img.width/2.0 - start_x-100) )-10;    // italy
      delta_angle = 180 + 7 / (float)tests;
    }
    
    for (int d=0; d<tests; d++) {
      float b = bresenham_avg_brightness(start_x, start_y, line_length, (delta_angle * d) + start_angle);
    }
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  float bresenham_avg_brightness(int x0, int y0, float distance, float degree) {
    int x1, y1;
    int sum_brightness = 0;
    int count_brightness = 0;
    ArrayList <intPoint> pnts;
    
    x1 = int(cos(radians(degree))*distance) + x0;
    y1 = int(sin(radians(degree))*distance) + y0;
    x0 = constrain(x0, 0, img.width-1);
    y0 = constrain(y0, 0, img.height-1);
    x1 = constrain(x1, 0, img.width-1);
    y1 = constrain(y1, 0, img.height-1);
    
    pnts = bresenham(x0, y0, x1, y1);
    for (intPoint p : pnts) {
      int loc = p.x + p.y*img.width;
      sum_brightness += brightness(img.pixels[loc]);
      count_brightness++;
      if (sum_brightness / count_brightness < darkest_neighbor) {
        darkest_x = p.x;
        darkest_y = p.y;
        darkest_neighbor = (float)sum_brightness / (float)count_brightness;
      }
      //println(x0+","+y0+"  "+p.x+","+p.y+"  brightness:"+sum_brightness / count_brightness+"  darkest:"+darkest_neighbor+"  "+darkest_x+","+darkest_y); 
    }
    //println();
    return( sum_brightness / count_brightness );
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  public void post_processing() {
    set_even_distribution();
    d1.evenly_distribute_pen_changes(d1.get_line_count(), pen_count);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  public void output_parameters() {
    gcode_comment("adjustbrightness: " + adjustbrightness);
    gcode_comment("squiggle_length: " + squiggle_length);
  }

}