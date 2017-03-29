///////////////////////////////////////////////////////////////////////////////////////////////////////
class intPoint {
  int x, y;
  
  intPoint(int x_, int y_) {
    x = x_;
    y = y_;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
// Algorithm was developed by Jack Elton Bresenham in 1962
// http://en.wikipedia.org/wiki/Bresenham's_line_algorithm
// Traslated from pseudocode labled "Simplification" from the link above.
///////////////////////////////////////////////////////////////////////////////////////////////////////
ArrayList <intPoint> bresenham(int x0, int y0, int x1, int y1) {
  int sx, sy;
  int err;
  int e2;
  ArrayList <intPoint> pnts = new ArrayList <intPoint>();

  int dx = abs(x1-x0);
  int dy = abs(y1-y0);
  if (x0 < x1) { sx = 1; } else { sx = -1; }
  if (y0 < y1) { sy = 1; } else { sy = -1; }
  err = dx-dy;
  while (true) {
    pnts.add(new intPoint(x0, y0));
    if ((x0 == x1) && (y0 == y1)) {
      return pnts;
    }
    e2 = 2*err;
    if (e2 > -dy) {
      err = err - dy;
      x0 = x0 + sx;
    }
    if (e2 < dx) {
      err = err + dx;
      y0 = y0 + sy;
    }
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
// Midpoint circle algorithm
// https://en.wikipedia.org/wiki/Midpoint_circle_algorithm
// I had to create 8 arrays of points then append them, because normaly order is not important.
///////////////////////////////////////////////////////////////////////////////////////////////////////
ArrayList <intPoint> midpoint_circle(int x0, int y0, int radius) {
  ArrayList <intPoint> pnts = new ArrayList <intPoint>();
  
  ArrayList <intPoint> p1 = new ArrayList <intPoint>();
  ArrayList <intPoint> p2 = new ArrayList <intPoint>();
  ArrayList <intPoint> p3 = new ArrayList <intPoint>();
  ArrayList <intPoint> p4 = new ArrayList <intPoint>();
  ArrayList <intPoint> p5 = new ArrayList <intPoint>();
  ArrayList <intPoint> p6 = new ArrayList <intPoint>();
  ArrayList <intPoint> p7 = new ArrayList <intPoint>();
  ArrayList <intPoint> p8 = new ArrayList <intPoint>();
  
  int x = radius;
  int y = 0;
  int err = 0;

  while (x >= y) {
    p1.add(new intPoint(x0 + x, y0 + y));
    p2.add(new intPoint(x0 + y, y0 + x));
    p3.add(new intPoint(x0 - y, y0 + x));
    p4.add(new intPoint(x0 - x, y0 + y));
    p5.add(new intPoint(x0 - x, y0 - y));
    p6.add(new intPoint(x0 - y, y0 - x));
    p7.add(new intPoint(x0 + y, y0 - x));
    p8.add(new intPoint(x0 + x, y0 - y));

    if (err <= 0) {
        y += 1;
        err += 2*y + 1;
    }
    if (err > 0) {
        x -= 1;
        err -= 2*x + 1;
    }
  }
  
  for (intPoint p : p1) { pnts.add(p); }
  for (intPoint p : p2) { pnts.add(p); }
  for (intPoint p : p3) { pnts.add(p); }
  for (intPoint p : p4) { pnts.add(p); }
  for (intPoint p : p5) { pnts.add(p); }
  for (intPoint p : p6) { pnts.add(p); }
  for (intPoint p : p7) { pnts.add(p); }
  for (intPoint p : p8) { pnts.add(p); }
  return pnts;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
  public void bresenham_lighten(int x0, int y0, int x1, int y1, int adjustbrightness) {
    ArrayList <intPoint> pnts;
  
    pnts = bresenham(x0, y0, x1, y1);
    for (intPoint p : pnts) {
      lighten_one_pixel(adjustbrightness * 5, p.x, p.y);
    }
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////