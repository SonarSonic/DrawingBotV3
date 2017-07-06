///////////////////////////////////////////////////////////////////////////////////////////////////////
// No, it's not a fancy dancy class like the snot nosed kids are doing these days.
// Now get the hell off my lawn.

///////////////////////////////////////////////////////////////////////////////////////////////////////
void gcode_header() {
  OUTPUT.println("G21");
  OUTPUT.println("G90");
  OUTPUT.println("G1 Z0");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void gcode_trailer() {
  OUTPUT.println("G1 Z0");
  OUTPUT.println("G1 X0.10 y0.10");
  OUTPUT.println("G1 X0 y0");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void gcode_comment(String comment) {
  gcode_comments += ("(" + comment + ")") + "\n";
  println(comment);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void pen_up() {
  is_pen_down = false;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void pen_down() {
  is_pen_down = true;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void move_abs(float x, float y) {
  
  d1.addline(is_pen_down, old_x, old_y, x, y);
  if (is_pen_down) {
    d1.render_last();
  }
  
  old_x = x;
  old_y = y;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void create_gcode_files (int line_count) {
  boolean is_pen_down;
  int pen_lifts;
  float pen_movement;
  float pen_drawing;
  int   lines_drawn;
  float x;
  float y;
  float distance;
  
  // Loop over all lines for every pen.
  for(int p=0; p<pen_count; p++) {    
    is_pen_down = false;
    pen_lifts = 2;
    pen_movement = 0;
    pen_drawing = 0;
    lines_drawn = 0;
    x = 0;
    y = 0;
    String gname = "gcode\\gcode_" + basefile_selected + "_pen" + p + "_" + copic_sets[current_copic_set][p] + ".txt";
    OUTPUT = createWriter(sketchPath("") + gname);
    OUTPUT.println(gcode_comments);
    gcode_header();
    
    for(int i=1; i<line_count; i++) { 
      if (d1.lines[i].pen_number == p) {
        
        float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale + gcode_offset_x;
        float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale + gcode_offset_y;
        float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale + gcode_offset_x;
        float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale + gcode_offset_y;
        distance = sqrt( sq(abs(gcode_scaled_x1 - gcode_scaled_x2)) + sq(abs(gcode_scaled_y1 - gcode_scaled_y2)) );
 
        if (x != gcode_scaled_x1 || y != gcode_scaled_y1) {
          // Oh crap, where the line starts is not where I am, pick up the pen and move there.
          OUTPUT.println("G1 Z0");
          is_pen_down = false;
          distance = sqrt( sq(abs(x - gcode_scaled_x1)) + sq(abs(y - gcode_scaled_y1)) );
          String buf = "G1 X" + nf(gcode_scaled_x1,0,2) + " Y" + nf(gcode_scaled_y1,0,2);
          OUTPUT.println(buf);
          x = gcode_scaled_x1;
          y = gcode_scaled_y1;
          pen_movement = pen_movement + distance;
          pen_lifts++;
        }
        
        if (d1.lines[i].pen_down) {
          if (is_pen_down == false) {
            OUTPUT.println("G1 Z1");
            is_pen_down = true;
          }
          pen_drawing = pen_drawing + distance;
          lines_drawn++;
        } else {
          if (is_pen_down == true) {
            OUTPUT.println("G1 Z0");
            is_pen_down = false;
            pen_movement = pen_movement + distance;
            pen_lifts++;
          }
        }
        
        String buf = "G1 X" + nf(gcode_scaled_x2,0,2) + " Y" + nf(gcode_scaled_y2,0,2);
        OUTPUT.println(buf);
        x = gcode_scaled_x2;
        y = gcode_scaled_y2;
        dx.update_limit(gcode_scaled_x2);
        dy.update_limit(gcode_scaled_y2);
      }
    }
    
    gcode_trailer();
    OUTPUT.println("(Drew " + lines_drawn + " lines for " + pen_drawing  / 25.4 / 12 + " feet)");
    OUTPUT.println("(Pen was lifted " + pen_lifts + " times for " + pen_movement  / 25.4 / 12 + " feet)");
    OUTPUT.println("(Extreams of X: " + dx.min + " thru " + dx.max + ")");
    OUTPUT.println("(Extreams of Y: " + dy.min + " thru " + dy.max + ")");
    OUTPUT.flush();
    OUTPUT.close();
    println("gcode created for pen " + p);
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void create_gcode_test_file () {
  // The dx.min are already scaled to gcode.
  float test_length = 25.4 * 2;
  
  String gname = "gcode\\gcode_" + basefile_selected + "_test.txt";
  OUTPUT = createWriter(sketchPath("") + gname);
  OUTPUT.println("(This is a test file to draw the extreams of the drawing area.)");
  OUTPUT.println("(Draws a 2 inch mark on all four corners of the paper.)");
  OUTPUT.println("(WARNING:  pen will be down.)");
  OUTPUT.println("(Extreams of X: " + dx.min + " thru " + dx.max + ")");
  OUTPUT.println("(Extreams of Y: " + dy.min + " thru " + dy.max + ")");
  gcode_header();
  
  OUTPUT.println("(Upper left)");
  OUTPUT.println("G1 X" + nf(dx.min,0,2) + " Y" + nf(dy.min + test_length,0,2));
  OUTPUT.println("G1 Z1");
  OUTPUT.println("G1 X" + nf(dx.min,0,2) + " Y" + nf(dy.min,0,2));
  OUTPUT.println("G1 X" + nf(dx.min + test_length,0,2) + " Y" + nf(dy.min,0,2));
  OUTPUT.println("G1 Z0");

  OUTPUT.println("(Upper right)");
  OUTPUT.println("G1 X" + nf(dx.max - test_length,0,2) + " Y" + nf(dy.min,0,2));
  OUTPUT.println("G1 Z1");
  OUTPUT.println("G1 X" + nf(dx.max,0,2) + " Y" + nf(dy.min,0,2));
  OUTPUT.println("G1 X" + nf(dx.max,0,2) + " Y" + nf(dy.min + test_length,0,2));
  OUTPUT.println("G1 Z0");

  OUTPUT.println("(Lower right)");
  OUTPUT.println("G1 X" + nf(dx.max,0,2) + " Y" + nf(dy.max - test_length,0,2));
  OUTPUT.println("G1 Z1");
  OUTPUT.println("G1 X" + nf(dx.max,0,2) + " Y" + nf(dy.max,0,2));
  OUTPUT.println("G1 X" + nf(dx.max - test_length,0,2) + " Y" + nf(dy.max,0,2));
  OUTPUT.println("G1 Z0");

  OUTPUT.println("(Lower left)");
  OUTPUT.println("G1 X" + nf(dx.min + test_length,0,2) + " Y" + nf(dy.max,0,2));
  OUTPUT.println("G1 Z1");
  OUTPUT.println("G1 X" + nf(dx.min,0,2) + " Y" + nf(dy.max,0,2));
  OUTPUT.println("G1 X" + nf(dx.min,0,2) + " Y" + nf(dy.max - test_length,0,2));
  OUTPUT.println("G1 Z0");

  gcode_trailer();
  OUTPUT.flush();
  OUTPUT.close();
  println("gcode test file created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
// Thanks to John Cliff for getting the SVG output moving forward.
void create_svg_file (int line_count) {
  boolean drawing_polyline = false;
  
  String gname = "gcode\\gcode_" + basefile_selected + ".svg";
  OUTPUT = createWriter(sketchPath("") + gname);
  OUTPUT.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
  OUTPUT.println("<svg width=\"" + nf(img.width * gcode_scale,0,2) + "mm\" height=\"" + nf(img.height * gcode_scale,0,2) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
  d1.set_pen_continuation_flags();
  
  // Loop over pens backwards to display dark lines last.
  // Then loop over all displayed lines.
  for(int p=pen_count-1; p>=0; p--) {    
    OUTPUT.println("<g id=\"" + copic_sets[current_copic_set][p] + "\">");
    for(int i=1; i<line_count; i++) { 
      if (d1.lines[i].pen_number == p) {
        
        // Do we add gcode_offsets needed by my bot, or zero based?
        //float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale + gcode_offset_x;
        //float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale + gcode_offset_y;
        //float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale + gcode_offset_x;
        //float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale + gcode_offset_y;
        float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale;
        float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale;
        float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale;
        float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale;

        if (d1.lines[i].pen_continuation == false && drawing_polyline) {
          OUTPUT.println("\" />");
          drawing_polyline = false;
        }

        if (d1.lines[i].pen_down) {
          if (d1.lines[i].pen_continuation) {
            String buf = nf(gcode_scaled_x2,0,2) + "," + nf(gcode_scaled_y2,0,2);
            OUTPUT.println(buf);
            drawing_polyline = true;
          } else {
            color c = copic.get_original_color(copic_sets[current_copic_set][p]);
            OUTPUT.println("<polyline fill=\"none\" stroke=\"#" + hex(c, 6) + "\" stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
            String buf = nf(gcode_scaled_x1,0,2) + "," + nf(gcode_scaled_y1,0,2);
            OUTPUT.println(buf);
            drawing_polyline = true;
          }
        }
      }
    }
    if (drawing_polyline) {
      OUTPUT.println("\" />");
      drawing_polyline = false;
    }
    OUTPUT.println("</g>");
  }
  OUTPUT.println("</svg>");
  OUTPUT.flush();
  OUTPUT.close();
  println("SVG created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////