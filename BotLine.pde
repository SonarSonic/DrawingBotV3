///////////////////////////////////////////////////////////////////////////////////////////////////////
// A class to describe one line segment
//
// Because of a bug in processing.org the MULTIPLY blendMode does not take into account the alpha of
// either source or destination.  If this gets corrected, tweaks to the stroke alpha might be more 
// representative of a Copic marker.  Right now it over emphasizes the darkening when overlaps
// of the same pen occur.

class botLine {
  int pen_number;
  boolean pen_down;
  float x1;
  float y1;
  float x2;
  float y2;
  
  botLine(boolean pen_down_, int pen_number_, float x1_, float y1_, float x2_, float y2_) {
    pen_down = pen_down_;
    pen_number = pen_number_;
    x1 = x1_;
    y1 = y1_;
    x2 = x2_;
    y2 = y2_;
  }

  void render_with_copic() {
    if(pen_down) {
      color c = copic.get_original_color(copic_sets[current_copic_set][pen_number]);
      //stroke(c, 255-brightness(c));
      stroke(c);
      //strokeWeight(2);
      //blendMode(BLEND);
      blendMode(MULTIPLY);
      line(x1, y1, x2, y2);
    }
  }

}

///////////////////////////////////////////////////////////////////////////////////////////////////////