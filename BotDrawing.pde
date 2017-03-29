///////////////////////////////////////////////////////////////////////////////////////////////////////
// A class to describe all the line segments
class botDrawing {
  private int line_count = 0;
  botLine[] lines = new botLine[10000000];
  String gcode_comment = "";
  
  void botDrawing() {
  }

  void render_last () {
    lines[line_count].render_with_copic();
  }
  
  void render_all () {
    for(int i=1; i<line_count; i++) {
      lines[i].render_with_copic();
    }
  }
  
  void render_some (int line_count) {
    for(int i=1; i<line_count; i++) {
      lines[i].render_with_copic();
    }
  }

  void render_one_pen (int line_count, int pen) {
    color c = color(255, 0, 0);

    for(int i=1; i<line_count; i++) {
    //for(int i=line_count; i>1; i--) {
      if(lines[i].pen_number == pen) {
        lines[i].render_with_copic();
      }
    }
  }

  void render_to_pdf (int line_count) {
    String pdfname = "gcode\\gcode_" + basefile_selected + ".pdf";
    PGraphics pdf = createGraphics(img.width, img.height, PDF, pdfname);
    pdf.beginDraw();
    pdf.background(255, 255, 255);
    for(int i=line_count; i>0; i--) {
      if(lines[i].pen_down) {
        color c = copic.get_original_color(copic_sets[current_copic_set][lines[i].pen_number]);
        pdf.stroke(c, 255);
        pdf.line(lines[i].x1, lines[i].y1, lines[i].x2, lines[i].y2);
      }
    }
    pdf.dispose();
    pdf.endDraw();
    println("Render PDF complete:  " + pdfname);
  }

  void addline(boolean pen_down_, float x1_, float y1_, float x2_, float y2_) {
    line_count++;
    lines[line_count] = new botLine (pen_down_, 0, x1_, y1_, x2_, y2_);
  }
  
  public int get_line_count() {
    return line_count;
  }
  
  public void evenly_distribute_pen_changes (int line_count, int total_pens) {
    for(int i=1; i<=line_count; i++) {
      int cidx = (int)map(i - 1, 0, line_count, 1, total_pens);
      lines[i].pen_number = cidx;
      //println (i + "   " + lines[i].pen_number);
    }
  }

  public void distribute_pen_changes_according_to_percentages (int line_count, int total_pens) {
    int p = 0;
    float p_total = 0;
    
    for(int i=1; i<=line_count; i++) {
      if (i > pen_distribution[p] + p_total) {
        p_total = p_total + pen_distribution[p];
        p++;
      }
      if (p > total_pens - 1) {
        // Hacky fix for off by one error
        println("ERROR: distribute_pen_changes_according_to_percentages, p:  ", p);
        p = total_pens - 1;
      }
      lines[i].pen_number = p;
      //println (i + "   " + lines[i].pen_number);
    }
  }

}