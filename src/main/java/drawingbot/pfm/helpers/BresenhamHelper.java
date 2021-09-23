package drawingbot.pfm.helpers;

import drawingbot.geom.PathBuilder;
import org.joml.Vector2i;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.util.List;

import static java.lang.Math.floor;

public class BresenhamHelper {

    public Shape clippingShape = null;

    public Shape getClippingShape() {
        return clippingShape;
    }

    public void setClippingShape(Shape clippingShape) {
        this.clippingShape = clippingShape;
    }

    public BresenhamHelper(){}

    @FunctionalInterface
    public interface IPixelSetter{
        void setPixel(int x, int y);
    }

    @FunctionalInterface
    public interface IPixelSetter3D{
        void setPixel3D(int x, int y, int z);
    }

    @FunctionalInterface
    public interface IPixelSetterAA{
        void setPixelAA(int x, int y, float value);
    }

    public boolean isInside(int x, int y){
        return clippingShape == null || clippingShape.contains(x, y);
    }

    public int[] findEdge(int x0, int y0, int x1, int y1, int width, int height){
        int[] point = new int[]{x0, y0};
        plotLine(x0, y0, x1, y1, (x, y) -> {
            if(x >= 0 && x < width && y >= 0 && y < height){
                point[0] = x;
                point[1] = y;
            }
        });
        return point;
    }

    public void plotShape(Shape path, IPixelSetter setter){
        float[] coords = new float[6];
        PathIterator pathIterator = path.getPathIterator(null);

        float lastMoveX = 0;
        float lastMoveY = 0;

        float lastSegX = 0;
        float lastSegY = 0;

        while(!pathIterator.isDone()){
            switch (pathIterator.currentSegment(coords)){
                case PathIterator.SEG_MOVETO:
                    lastMoveX = coords[0];
                    lastMoveY = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    plotLine((int)lastSegX, (int)lastSegY, (int)coords[0], (int)coords[1], setter);
                    break;
                case PathIterator.SEG_QUADTO:
                    plotQuadBezier((int)lastSegX, (int)lastSegY, (int)coords[0], (int)coords[1], (int)coords[2], (int)coords[3], setter);
                    break;
                case PathIterator.SEG_CUBICTO:
                    plotCubicBezier((int)lastSegX, (int)lastSegY, (int)coords[0], (int)coords[1], (int)coords[2], (int)coords[3], (int)coords[4], (int)coords[5], setter);
                    break;
                case PathIterator.SEG_CLOSE:
                    plotLine((int)lastSegX, (int)lastSegY, (int)lastMoveX, (int)lastMoveY, setter);
                    break;
            }
            lastSegX = coords[0];
            lastSegY = coords[1];
            pathIterator.next();
        }
    }

    public void plotCatmullRom(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, float tension, IPixelSetter setter){
        plotCatmullRom(new float[]{x0, y0}, new float[]{x1, y1}, new float[]{x2, y2}, new float[]{x3, y3}, tension, setter);
    }

    public void plotCatmullRom(List<Vector2i> points, float tension, IPixelSetter setter){
        float[][] bezier = new float[4][2];

        Vector2i p0;
        Vector2i p1 = null;
        Vector2i p2 = null;
        Vector2i p3 = null;


        for(Vector2i point : points){
            p0 = p1;
            p1 = p2;
            p2 = p3;
            p3 = point;

            if(p0 != null && p1 != null && p2 != null && p3 != null){
                PathBuilder.catmullToBezier(new float[][]{new float[]{p0.x, p0.y}, new float[]{p1.x, p1.y}, new float[]{p2.x, p2.y}, new float[]{p3.x, p3.y}}, bezier, tension);
                plotCubicBezier((int)bezier[0][0], (int)bezier[0][1], (int)bezier[1][0], (int)bezier[1][1], (int)bezier[2][0], (int)bezier[2][1], (int)bezier[3][0], (int)bezier[3][1], setter);
            }
        }
    }

    public void plotCatmullRom(float[] p1, float[] p2, float[] p3, float[] p4, float tension, IPixelSetter setter){
        float[][] bezier = PathBuilder.catmullToBezier(new float[][]{p1, p2, p3, p4}, new float[4][2], tension);
        plotCubicBezier((int)bezier[0][0], (int)bezier[0][1], (int)bezier[1][0], (int)bezier[1][1], (int)bezier[2][0], (int)bezier[2][1], (int)bezier[3][0], (int)bezier[3][1], setter);
        //line((int)p2[0], (int)p2[1], (int)p3[0], (int)p3[1], (x, y) -> {setter.setPixel(x,y); return false; });
    }

    public void plotCatmullRomAA(float[] p1, float[] p2, float[] p3, float[] p4, float tension, IPixelSetterAA setter){
        float[][] bezier = PathBuilder.catmullToBezier(new float[][]{p1, p2, p3, p4}, new float[4][2], tension);
        plotCubicBezierSegAA((int)bezier[0][0], (int)bezier[0][1], (int)bezier[1][0], (int)bezier[1][1], (int)bezier[2][0], (int)bezier[2][1], (int)bezier[3][0], (int)bezier[3][1], setter);
    }



    ///SRC: Bresenham Curve Rasterizing Algorithms - https://github.com/zingl/Bresenham by Zingl Alois
    public void plotLine(int x0, int y0, int x1, int y1, IPixelSetter setter)
    {
        int dx =  Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
        int dy = -Math.abs(y1-y0), sy = y0<y1 ? 1 : -1;
        int err = dx+dy, e2;                                  /* error value e_xy */

        for (;;) {                                                        /* loop */
            if(!isInside(x0, y0)){
                return;
            }
            setter.setPixel(x0, y0);
            e2 = 2*err;
            if (e2 >= dy) {                                       /* e_xy+e_x > 0 */
                if (x0 == x1) break;
                err += dy; x0 += sx;
            }
            if (e2 <= dx) {                                       /* e_xy+e_y < 0 */
                if (y0 == y1) break;
                err += dx; y0 += sy;
            }
        }
    }

    public void plotLine3d(int x0, int y0, int z0, int x1, int y1, int z1, IPixelSetter3D setter3D)
    {
        int dx = Math.abs(x1-x0), sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1-y0), sy = y0 < y1 ? 1 : -1;
        int dz = Math.abs(z1-z0), sz = z0 < z1 ? 1 : -1;
        int dm = dx > dy && dx > dz ? dx : dy > dz ? dy : dz, i = dm; /* max diff */
        x1 = y1 = z1 = dm/2;                                      /* error offset */

        for (;;) {                                                    /* loop */
            if(!isInside(x0, y0)){
                return;
            }
            setter3D.setPixel3D(x0,y0,z0);
            if (i-- == 0) break;
            x1 -= dx; if (x1 < 0) { x1 += dm; x0 += sx; }
            y1 -= dy; if (y1 < 0) { y1 += dm; y0 += sy; }
            z1 -= dz; if (z1 < 0) { z1 += dm; z0 += sz; }
        }
    }

    public void plotEllipse(int xm, int ym, int a, int b, IPixelSetter setter)
    {
        int x = -a, y = 0;           /* II. quadrant from bottom left to top right */
        long e2 = (long)b*b, err = (long)x*(2*e2+x)+e2;         /* error of 1.step */

        do {
            setter.setPixel(xm-x, ym+y);                                 /*   I. Quadrant */
            setter.setPixel(xm+x, ym+y);                                 /*  II. Quadrant */
            setter.setPixel(xm+x, ym-y);                                 /* III. Quadrant */
            setter.setPixel(xm-x, ym-y);                                 /*  IV. Quadrant */
            e2 = 2*err;
            if (e2 >= (x*2+1)*(long)b*b)                           /* e_xy+e_x > 0 */
                err += (++x*2+1)*(long)b*b;
            if (e2 <= (y*2+1)*(long)a*a)                           /* e_xy+e_y < 0 */
                err += (++y*2+1)*(long)a*a;
        } while (x <= 0);

        while (y++ < b) {                  /* too early stop of flat ellipses a=1, */
            setter.setPixel(xm, ym+y);                        /* -> finish tip of ellipse */
            setter.setPixel(xm, ym-y);
        }
    }


    public void plotOptimizedEllipse(int xm, int ym, int a, int b, IPixelSetter setter)
    {
        int x = -a, y = 0;          /* II. quadrant from bottom left to top right */
        int e2 = b, dx = (1+2*x)*e2*e2;                       /* error increment  */
        int dy = x*x, err = dx+dy;                             /* error of 1.step */

        do {
            setter.setPixel(xm-x, ym+y);                                 /*   I. Quadrant */
            setter.setPixel(xm+x, ym+y);                                 /*  II. Quadrant */
            setter.setPixel(xm+x, ym-y);                                 /* III. Quadrant */
            setter.setPixel(xm-x, ym-y);                                 /*  IV. Quadrant */
            e2 = 2*err;
            if (e2 >= dx) { x++; err += dx += 2*(long)b*b; }             /* x step */
            if (e2 <= dy) { y++; err += dy += 2*(long)a*a; }             /* y step */
        } while (x <= 0);

        while (y++ < b) {            /* too early stop for flat ellipses with a=1, */
            setter.setPixel(xm, ym+y);                        /* -> finish tip of ellipse */
            setter.setPixel(xm, ym-y);
        }
    }

    public void plotCircle(int xm, int ym, int r, IPixelSetter setter)
    {
        int x = -r, y = 0, err = 2-2*r;                /* bottom left to top right */
        do {
            setter.setPixel(xm-x, ym+y);                            /*   I. Quadrant +x +y */
            setter.setPixel(xm-y, ym-x);                            /*  II. Quadrant -x +y */
            setter.setPixel(xm+x, ym-y);                            /* III. Quadrant -x -y */
            setter.setPixel(xm+y, ym+x);                            /*  IV. Quadrant +x -y */
            r = err;
            if (r <= y) err += ++y*2+1;                             /* e_xy+e_y < 0 */
            if (r > x || err > y)                  /* e_xy+e_x > 0 or no 2nd y-step */
                err += ++x*2+1;                                     /* -> x-step now */
        } while (x < 0);
    }

    public void plotEllipseRect(int x0, int y0, int x1, int y1, IPixelSetter setter)
    {                              /* rectangular parameter enclosing the ellipse */
        int a = Math.abs(x1-x0), b = Math.abs(y1-y0), b1 = b&1;                 /* diameter */
        double dx = 4*(1.0-a)*b*b, dy = 4*(b1+1)*a*a;           /* error increment */
        double err = dx+dy+b1*a*a, e2;                          /* error of 1.step */

        if (x0 > x1) { x0 = x1; x1 += a; }        /* if called with swapped points */
        if (y0 > y1) y0 = y1;                                  /* .. exchange them */
        y0 += (b+1)/2; y1 = y0-b1;                               /* starting pixel */
        a = 8*a*a; b1 = 8*b*b;

        do {
            setter.setPixel(x1, y0);                                      /*   I. Quadrant */
            setter.setPixel(x0, y0);                                      /*  II. Quadrant */
            setter.setPixel(x0, y1);                                      /* III. Quadrant */
            setter.setPixel(x1, y1);                                      /*  IV. Quadrant */
            e2 = 2*err;
            if (e2 <= dy) { y0++; y1--; err += dy += a; }                 /* y step */
            if (e2 >= dx || 2*err > dy) { x0++; x1--; err += dx += b1; }  /* x step */
        } while (x0 <= x1);

        while (y0-y1 <= b) {                /* too early stop of flat ellipses a=1 */
            setter.setPixel(x0-1, y0);                         /* -> finish tip of ellipse */
            setter.setPixel(x1+1, y0++);
            setter.setPixel(x0-1, y1);
            setter.setPixel(x1+1, y1--);
        }
    }

    public void plotQuadBezierSeg(int x0, int y0, int x1, int y1, int x2, int y2, IPixelSetter setter)
    {                                  /* plot a limited quadratic Bezier segment */
        int sx = x2-x1, sy = y2-y1;
        long xx = x0-x1, yy = y0-y1, xy;              /* relative values for checks */
        double dx, dy, err, cur = xx*sy-yy*sx;                         /* curvature */

        assert(xx*sx <= 0 && yy*sy <= 0);       /* sign of gradient must not change */

        if (sx*(long)sx+sy*(long)sy > xx*xx+yy*yy) {      /* begin with longer part */
            x2 = x0; x0 = sx+x1; y2 = y0; y0 = sy+y1; cur = -cur;       /* swap P0 P2 */
        }
        if (cur != 0) {                                         /* no straight line */
            xx += sx; xx *= sx = x0 < x2 ? 1 : -1;                /* x step direction */
            yy += sy; yy *= sy = y0 < y2 ? 1 : -1;                /* y step direction */
            xy = 2*xx*yy; xx *= xx; yy *= yy;               /* differences 2nd degree */
            if (cur*sx*sy < 0) {                                /* negated curvature? */
                xx = -xx; yy = -yy; xy = -xy; cur = -cur;
            }
            dx = 4.0*sy*cur*(x1-x0)+xx-xy;                  /* differences 1st degree */
            dy = 4.0*sx*cur*(y0-y1)+yy-xy;
            xx += xx; yy += yy; err = dx+dy+xy;                     /* error 1st step */
            do {
                setter.setPixel(x0,y0);                                          /* plot curve */
                if (x0 == x2 && y0 == y2) return;       /* last pixel -> curve finished */
                boolean errorCheck = 2*err < dx;                /* save value for test of y step */
                if (2*err > dy) { x0 += sx; dx -= xy; err += dy += yy; }      /* x step */
                if (errorCheck) { y0 += sy; dy -= xy; err += dx += xx; }      /* y step */
            } while (dy < 0 && dx > 0);        /* gradient negates -> algorithm fails */
        }
        plotLine(x0, y0, x2, y2, setter);                       /* plot remaining part to end */
    }

    public void plotQuadBezier(int x0, int y0, int x1, int y1, int x2, int y2, IPixelSetter setter)
    {                                          /* plot any quadratic Bezier curve */
        int x = x0-x1, y = y0-y1;
        double t = x0-2*x1+x2, r;

        if ((long)x*(x2-x1) > 0) {                        /* horizontal cut at P4? */
            if ((long)y*(y2-y1) > 0)                     /* vertical cut at P6 too? */
                if (Math.abs((y0-2*y1+y2)/t*x) > Math.abs(y)) {               /* which first? */
                    x0 = x2; x2 = x+x1; y0 = y2; y2 = y+y1;            /* swap points */
                }                            /* now horizontal cut at P4 comes first */
            t = (x0-x1)/t;
            r = (1-t)*((1-t)*y0+2.0*t*y1)+t*t*y2;                       /* By(t=P4) */
            t = (x0*x2-x1*x1)*t/(x0-x1);                       /* gradient dP4/dx=0 */
            x = (int) floor(t+0.5); y = (int) floor(r+0.5);
            r = (y1-y0)*(t-x0)/(x1-x0)+y0;                  /* intersect P3 | P0 P1 */
            plotQuadBezierSeg(x0,y0, x, (int) floor(r+0.5), x,y, setter);
            r = (y1-y2)*(t-x2)/(x1-x2)+y2;                  /* intersect P4 | P1 P2 */
            x0 = x1 = x; y0 = y; y1 = (int) floor(r+0.5);             /* P0 = P4, P1 = P8 */
        }
        if ((long)(y0-y1)*(y2-y1) > 0) {                    /* vertical cut at P6? */
            t = y0-2*y1+y2; t = (y0-y1)/t;
            r = (1-t)*((1-t)*x0+2.0*t*x1)+t*t*x2;                       /* Bx(t=P6) */
            t = (y0*y2-y1*y1)*t/(y0-y1);                       /* gradient dP6/dy=0 */
            x = (int) floor(r+0.5); y = (int) floor(t+0.5);
            r = (x1-x0)*(t-y0)/(y1-y0)+x0;                  /* intersect P6 | P0 P1 */
            plotQuadBezierSeg(x0,y0, (int) floor(r+0.5),y, x,y, setter);
            r = (x1-x2)*(t-y2)/(y1-y2)+x2;                  /* intersect P7 | P1 P2 */
            x0 = x; x1 = (int) floor(r+0.5); y0 = y1 = y;             /* P0 = P6, P1 = P7 */
        }
        plotQuadBezierSeg(x0,y0, x1,y1, x2, y2, setter);                  /* remaining part */
    }

    public void plotQuadRationalBezierSeg(int x0, int y0, int x1, int y1, int x2, int y2, float w, IPixelSetter setter)
    {                   /* plot a limited rational Bezier segment, squared weight */
        int sx = x2-x1, sy = y2-y1;                   /* relative values for checks */
        double dx = x0-x2, dy = y0-y2, xx = x0-x1, yy = y0-y1;
        double xy = xx*sy+yy*sx, cur = xx*sy-yy*sx, err;               /* curvature */

        assert(xx*sx <= 0.0 && yy*sy <= 0.0);   /* sign of gradient must not change */

        if (cur != 0.0 && w > 0.0) {                            /* no straight line */
            if (sx*(long)sx+sy*(long)sy > xx*xx+yy*yy) {    /* begin with longer part */
                x2 = x0; x0 -= dx; y2 = y0; y0 -= dy; cur = -cur;         /* swap P0 P2 */
            }
            xx = 2.0*(4.0*w*sx*xx+dx*dx);                   /* differences 2nd degree */
            yy = 2.0*(4.0*w*sy*yy+dy*dy);
            sx = x0 < x2 ? 1 : -1;                                /* x step direction */
            sy = y0 < y2 ? 1 : -1;                                /* y step direction */
            xy = -2.0*sx*sy*(2.0*w*xy+dx*dy);

            if (cur*sx*sy < 0.0) {                              /* negated curvature? */
                xx = -xx; yy = -yy; xy = -xy; cur = -cur;
            }
            dx = 4.0*w*(x1-x0)*sy*cur+xx/2.0+xy;            /* differences 1st degree */
            dy = 4.0*w*(y0-y1)*sx*cur+yy/2.0+xy;

            if (w < 0.5 && (dy > xy || dx < xy)) {   /* flat ellipse, algorithm fails */
                cur = (w+1.0)/2.0; w = (float)Math.sqrt(w); xy = 1.0/(w+1.0);
                sx = (int) floor((x0+2.0*w*x1+x2)*xy/2.0+0.5);    /* subdivide curve in half */
                sy = (int) floor((y0+2.0*w*y1+y2)*xy/2.0+0.5);
                dx = floor((w*x1+x0)*xy+0.5); dy = floor((y1*w+y0)*xy+0.5);
                plotQuadRationalBezierSeg(x0,y0, (int)dx, (int)dy, sx,sy, (int)cur, setter);/* plot separately */
                dx = floor((w*x1+x2)*xy+0.5); dy = floor((y1*w+y2)*xy+0.5);
                plotQuadRationalBezierSeg(sx,sy, (int)dx, (int)dy, x2,y2, (int)cur, setter);
                return;
            }
            err = dx+dy-xy;                                           /* error 1.step */
            do {
                setter.setPixel(x0, y0);                                          /* plot curve */
                if (x0 == x2 && y0 == y2) return;       /* last pixel -> curve finished */
                boolean xErr = 2*err > dy; boolean yErr = 2*(err+yy) < -dy;/* save value for test of x step */
                if (2*err < dx || xErr) { y0 += sy; dy += xy; err += dx += xx; }/* y step */
                if (2*err > dx || yErr) { x0 += sx; dx += xy; err += dy += yy; }/* x step */
            } while (dy <= xy && dx >= xy);    /* gradient negates -> algorithm fails */
        }
        plotLine(x0,y0, x2,y2, setter);                     /* plot remaining needle to end */
    }

    public void plotQuadRationalBezier(int x0, int y0, int x1, int y1, int x2, int y2, float w, IPixelSetter setter){
        /* plot any quadratic rational Bezier curve */
        int x = x0-2*x1+x2, y = y0-2*y1+y2;
        double xx = x0-x1, yy = y0-y1, ww, t, q;

        assert(w >= 0.0);

        if (xx*(x2-x1) > 0) {                             /* horizontal cut at P4? */
            if (yy*(y2-y1) > 0)                          /* vertical cut at P6 too? */
                if (Math.abs(xx*y) > Math.abs(yy*x)) {                       /* which first? */
                    x0 = x2; x2 = (int)xx+x1; y0 = y2; y2 = (int)yy+y1;          /* swap points */
                }                            /* now horizontal cut at P4 comes first */
            if (x0 == x2 || w == 1.0) t = (x0-x1)/(double)x;
            else {                                 /* non-rational or rational case */
                q = Math.sqrt(4.0*w*w*(x0-x1)*(x2-x1)+(x2-x0)*(long)(x2-x0));
                if (x1 < x0) q = -q;
                t = (2.0*w*(x0-x1)-x0+x2+q)/(2.0*(1.0-w)*(x2-x0));        /* t at P4 */
            }
            q = 1.0/(2.0*t*(1.0-t)*(w-1.0)+1.0);                 /* sub-divide at t */
            xx = (t*t*(x0-2.0*w*x1+x2)+2.0*t*(w*x1-x0)+x0)*q;               /* = P4 */
            yy = (t*t*(y0-2.0*w*y1+y2)+2.0*t*(w*y1-y0)+y0)*q;
            ww = t*(w-1.0)+1.0; ww *= ww*q;                    /* squared weight P3 */
            w = (float)(((1.0-t)*(w-1.0)+1.0)*Math.sqrt(q));                         /* weight P8 */
            x = (int) floor(xx+0.5); y = (int) floor(yy+0.5);                             /* P4 */
            yy = (xx-x0)*(y1-y0)/(x1-x0)+y0;                /* intersect P3 | P0 P1 */
            plotQuadRationalBezierSeg(x0,y0, x,(int) floor(yy+0.5), x,y, (float) ww, setter);
            yy = (xx-x2)*(y1-y2)/(x1-x2)+y2;                /* intersect P4 | P1 P2 */
            y1 = (int) floor(yy+0.5); x0 = x1 = x; y0 = y;            /* P0 = P4, P1 = P8 */
        }
        if ((y0-y1)*(long)(y2-y1) > 0) {                    /* vertical cut at P6? */
            if (y0 == y2 || w == 1.0) t = (y0-y1)/(y0-2.0*y1+y2);
            else {                                 /* non-rational or rational case */
                q = Math.sqrt(4.0*w*w*(y0-y1)*(y2-y1)+(y2-y0)*(long)(y2-y0));
                if (y1 < y0) q = -q;
                t = (2.0*w*(y0-y1)-y0+y2+q)/(2.0*(1.0-w)*(y2-y0));        /* t at P6 */
            }
            q = 1.0/(2.0*t*(1.0-t)*(w-1.0)+1.0);                 /* sub-divide at t */
            xx = (t*t*(x0-2.0*w*x1+x2)+2.0*t*(w*x1-x0)+x0)*q;               /* = P6 */
            yy = (t*t*(y0-2.0*w*y1+y2)+2.0*t*(w*y1-y0)+y0)*q;
            ww = t*(w-1.0)+1.0; ww *= ww*q;                    /* squared weight P5 */
            w = (float)(((1.0-t)*(w-1.0)+1.0)*Math.sqrt(q));                         /* weight P7 */
            x = (int) floor(xx+0.5); y = (int) floor(yy+0.5);                             /* P6 */
            xx = (x1-x0)*(yy-y0)/(y1-y0)+x0;                /* intersect P6 | P0 P1 */
            plotQuadRationalBezierSeg(x0,y0, (int) floor(xx+0.5),y, x,y, (float) ww, setter);
            xx = (x1-x2)*(yy-y2)/(y1-y2)+x2;                /* intersect P7 | P1 P2 */
            x1 = (int) floor(xx+0.5); x0 = x; y0 = y1 = y;            /* P0 = P6, P1 = P7 */
        }
        plotQuadRationalBezierSeg(x0,y0, x1,y1, x2,y2, w*w, setter);          /* remaining */
    }

    public void plotRotatedEllipse(int x, int y, int a, int b, float angle, IPixelSetter setter)
    {                                   /* plot ellipse rotated by angle (radian) */
        double xd = (long)a*a, yd = (long)b*b;
        double s = Math.sin(angle), zd = (xd-yd)*s;                  /* ellipse rotation */
        xd = Math.sqrt(xd-zd*s);
        yd = Math.sqrt(yd+zd*s);           /* surrounding rectangle */
        a = (int)(xd + 0.5); b = (int)(yd + 0.5); zd = zd*a*b/(xd*yd);           /* scale to integer */
        plotRotatedEllipseRect(x-a,y-b, x+a,y+b, (long)(4*zd*Math.cos(angle)), setter);
    }

    public void plotRotatedEllipseRect(int x0, int y0, int x1, int y1, long zd, IPixelSetter setter)
    {                  /* rectangle enclosing the ellipse, integer rotation angle */
        int xd = x1-x0, yd = y1-y0;
        float w = xd*(long)yd;
        if (zd == 0){
            plotEllipseRect(x0,y0, x1,y1, setter);          /* looks nicer */
            return;
        }
        if (w != 0.0) w = (w-zd)/(w+w);                    /* squared weight of P1 */
        assert(w <= 1.0 && w >= 0.0);                /* limit angle to |zd|<=xd*yd */
        xd = (int) floor(xd*w+0.5); yd = (int) floor(yd*w+0.5);           /* snap xe,ye to int */
        plotQuadRationalBezierSeg(x0,y0+yd, x0,y0, x0+xd,y0, 1-w, setter);
        plotQuadRationalBezierSeg(x0,y0+yd, x0,y1, x1-xd,y1, w, setter);
        plotQuadRationalBezierSeg(x1,y1-yd, x1,y1, x1-xd,y1, 1-w, setter);
        plotQuadRationalBezierSeg(x1,y1-yd, x1,y0, x0+xd,y0, w, setter);
    }

    public void plotCubicBezierSeg(int x0, int y0, float x1, float y1, float x2, float y2, int x3, int y3, IPixelSetter setter){
        /* plot limited cubic Bezier segment */
        int f, fx, fy, leg = 1;
        int sx = x0 < x3 ? 1 : -1, sy = y0 < y3 ? 1 : -1;        /* step direction */
        float xc = -Math.abs(x0+x1-x2-x3), xa = xc-4*sx*(x1-x2), xb = sx*(x0-x1-x2+x3);
        float yc = -Math.abs(y0+y1-y2-y3), ya = yc-4*sy*(y1-y2), yb = sy*(y0-y1-y2+y3);
        double ab, ac, bc, cb, xx, xy, yy, dx, dy, ex, EP = 0.01, pxy;
        /* check for curve restrains */
        /* slope P0-P1 == P2-P3    and  (P0-P3 == P1-P2      or   no slope change) */
        assert((x1-x0)*(x2-x3) < EP && ((x3-x0)*(x1-x2) < EP || xb*xb < xa*xc+EP));
        assert((y1-y0)*(y2-y3) < EP && ((y3-y0)*(y1-y2) < EP || yb*yb < ya*yc+EP));

        if (xa == 0 && ya == 0) {                              /* quadratic Bezier */
            sx = (int) floor((3*x1-x0+1)/2); sy = (int) floor((3*y1-y0+1)/2);   /* new midpoint */
            plotQuadBezierSeg(x0,y0, sx,sy, x3,y3, setter);
            return;
        }
        x1 = (x1-x0)*(x1-x0)+(y1-y0)*(y1-y0)+1;                    /* line lengths */
        x2 = (x2-x3)*(x2-x3)+(y2-y3)*(y2-y3)+1;
        do {                                                /* loop over both ends */
            ab = xa*yb-xb*ya; ac = xa*yc-xc*ya; bc = xb*yc-xc*yb;
            ex = ab*(ab+ac-3*bc)+ac*ac;       /* P0 part of self-intersection loop? */
            f = ex > 0 ? 1 : (int) Math.sqrt(1 + 1024 / x1);               /* calculate resolution */
            ab *= f; ac *= f; bc *= f; ex *= f*f;            /* increase resolution */
            xy = 9*(ab+ac+bc)/8; cb = 8*(xa-ya);  /* init differences of 1st degree */
            dx = 27*(8*ab*(yb*yb-ya*yc)+ex*(ya+2*yb+yc))/64-ya*ya*(xy-ya);
            dy = 27*(8*ab*(xb*xb-xa*xc)-ex*(xa+2*xb+xc))/64-xa*xa*(xy+xa);
            /* init differences of 2nd degree */
            xx = 3*(3*ab*(3*yb*yb-ya*ya-2*ya*yc)-ya*(3*ac*(ya+yb)+ya*cb))/4;
            yy = 3*(3*ab*(3*xb*xb-xa*xa-2*xa*xc)-xa*(3*ac*(xa+xb)+xa*cb))/4;
            xy = xa*ya*(6*ab+6*ac-3*bc+cb); ac = ya*ya; cb = xa*xa;
            xy = 3*(xy+9*f*(cb*yb*yc-xb*xc*ac)-18*xb*yb*ab)/8;

            if (ex < 0) {         /* negate values if inside self-intersection loop */
                dx = -dx; dy = -dy; xx = -xx; yy = -yy; xy = -xy; ac = -ac; cb = -cb;
            }                                     /* init differences of 3rd degree */
            ab = 6*ya*ac; ac = -6*xa*ac; bc = 6*ya*cb; cb = -6*xa*cb;
            dx += xy; ex = dx+dy; dy += xy;                    /* error of 1st step */

            cycle: for (pxy = xy, fx = fy = f; x0 != x3 && y0 != y3; ) {
                setter.setPixel(x0, y0);                                       /* plot curve */
                do {                                  /* move sub-steps of one pixel */
                    if (dx > pxy || dy < pxy) break cycle;       /* confusing values */
                    y1 = (float) (2*ex-dy);                    /* save value for test of y step */
                    if (2*ex >= dx) {                                   /* x sub-step */
                        fx--; ex += dx += xx; dy += xy += ac; yy += bc; xx += ab;
                    }
                    if (y1 <= 0) {                                      /* y sub-step */
                        fy--; ex += dy += yy; dx += xy += bc; xx += ac; yy += cb;
                    }
                } while (fx > 0 && fy > 0);                       /* pixel complete? */
                if (2*fx <= f) { x0 += sx; fx += f; }                      /* x step */
                if (2*fy <= f) { y0 += sy; fy += f; }                      /* y step */
                if (pxy == xy && dx < 0 && dy > 0) pxy = EP;  /* pixel ahead valid */
            }
            xx = x0; x0 = x3; x3 = (int) xx; sx = -sx; xb = -xb;             /* swap legs */
            yy = y0; y0 = y3; y3 = (int) yy; sy = -sy; yb = -yb; x1 = x2;
        } while (leg--!=0);                                          /* try other end */
            plotLine(x0, y0, x3, y3, setter);       /* remaining part in case of cusp or crunode */
    }


    public void plotCubicBezier(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, IPixelSetter setter)
    {                                              /* plot any cubic Bezier curve */
        int n = 0, i = 0;
        long xc = x0+x1-x2-x3, xa = xc-4*(x1-x2);
        long xb = x0-x1-x2+x3, xd = xb+4*(x1+x2);
        long yc = y0+y1-y2-y3, ya = yc-4*(y1-y2);
        long yb = y0-y1-y2+y3, yd = yb+4*(y1+y2);
        float fx0 = x0, fx1, fx2, fx3, fy0 = y0, fy1, fy2, fy3;
        double t1 = xb*xb-xa*xc, t2;
        double[] t = new double [5];
        /* sub-divide curve at gradient sign changes */
        if (xa == 0) {                                               /* horizontal */
            if (Math.abs(xc) < 2*Math.abs(xb)) t[n++] = xc/(2.0*xb);            /* one change */
        } else if (t1 > 0.0) {                                      /* two changes */
            t2 = Math.sqrt(t1);
            t1 = (xb-t2)/xa; if (Math.abs(t1) < 1.0) t[n++] = t1;
            t1 = (xb+t2)/xa; if (Math.abs(t1) < 1.0) t[n++] = t1;
        }
        t1 = yb*yb-ya*yc;
        if (ya == 0) {                                                 /* vertical */
            if (Math.abs(yc) < 2*Math.abs(yb)) t[n++] = yc/(2.0*yb);            /* one change */
        } else if (t1 > 0.0) {                                      /* two changes */
            t2 = Math.sqrt(t1);
            t1 = (yb-t2)/ya; if (Math.abs(t1) < 1.0) t[n++] = t1;
            t1 = (yb+t2)/ya; if (Math.abs(t1) < 1.0) t[n++] = t1;
        }
        for (i = 1; i < n; i++)                         /* bubble sort of 4 points */
            if ((t1 = t[i-1]) > t[i]) { t[i-1] = t[i]; t[i] = t1; i = 0; }

        t1 = -1.0; t[n] = 1.0;                                /* begin / end point */
        for (i = 0; i <= n; i++) {                 /* plot each segment separately */
            t2 = t[i];                                /* sub-divide at t[i-1], t[i] */
            fx1 = (float) ((t1*(t1*xb-2*xc)-t2*(t1*(t1*xa-2*xb)+xc)+xd)/8-fx0);
            fy1 = (float) ((t1*(t1*yb-2*yc)-t2*(t1*(t1*ya-2*yb)+yc)+yd)/8-fy0);
            fx2 = (float) ((t2*(t2*xb-2*xc)-t1*(t2*(t2*xa-2*xb)+xc)+xd)/8-fx0);
            fy2 = (float) ((t2*(t2*yb-2*yc)-t1*(t2*(t2*ya-2*yb)+yc)+yd)/8-fy0);
            fx0 -= fx3 = (float) ((t2*(t2*(3*xb-t2*xa)-3*xc)+xd)/8);
            fy0 -= fy3 = (float) ((t2*(t2*(3*yb-t2*ya)-3*yc)+yd)/8);
            x3 = (int) floor(fx3+0.5); y3 = (int) floor(fy3+0.5);        /* scale bounds to int */
            if (fx0 != 0.0) { fx1 *= fx0 = (x0-x3)/fx0; fx2 *= fx0; }
            if (fy0 != 0.0) { fy1 *= fy0 = (y0-y3)/fy0; fy2 *= fy0; }
            if (x0 != x3 || y0 != y3)                            /* segment t1 - t2 */
                plotCubicBezierSeg(x0,y0, (int)(x0+fx1),(int)(y0+fy1), (int)(x0+fx2),(int)(y0+fy2), x3,y3, setter);
            x0 = x3; y0 = y3; fx0 = fx3; fy0 = fy3; t1 = t2;
        }
    }

    public void plotLineAA(int x0, int y0, int x1, int y1, IPixelSetterAA setterAA)
    {             /* draw a black (0) anti-aliased line on white (255) background */
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, x2;
        long dx = Math.abs(x1-x0), dy = Math.abs(y1-y0), err = dx*dx+dy*dy;
        long e2 = err == 0 ? 1 : (long) (0xffff7fL / Math.sqrt(err));     /* multiplication factor */

        dx *= e2; dy *= e2; err = dx-dy;                       /* error value e_xy */
        for ( ; ; ){                                                 /* pixel loop */
            setterAA.setPixelAA(x0,y0, (int) (Math.abs(err-dx+dy)>>16));
            e2 = err; x2 = x0;
            if (2*e2 >= -dx) {                                            /* x step */
                if (x0 == x1) break;
                if (e2+dy < 0xff0000L) setterAA.setPixelAA(x0,y0+sy, (int) ((e2+dy)>>16));
                err -= dy; x0 += sx;
            }
            if (2*e2 <= dy) {                                             /* y step */
                if (y0 == y1) break;
                if (dx-e2 < 0xff0000L) setterAA.setPixelAA(x2+sx,y0, (int) ((dx-e2)>>16));
                err += dx; y0 += sy;
            }
        }

    }
    public void plotCircleAA(int xm, int ym, int r, IPixelSetterAA setterAA)
    {                     /* draw a black anti-aliased circle on white background */
        int x = -r, y = 0;           /* II. quadrant from bottom left to top right */
        int i, x2, e2, err = 2-2*r;                             /* error of 1.step */
        r = 1-err;
        do {
            i = 255*Math.abs(err-2*(x+y)-2)/r;               /* get blend value of pixel */
            setterAA.setPixelAA(xm-x, ym+y, i);                             /*   I. Quadrant */
            setterAA.setPixelAA(xm-y, ym-x, i);                             /*  II. Quadrant */
            setterAA.setPixelAA(xm+x, ym-y, i);                             /* III. Quadrant */
            setterAA.setPixelAA(xm+y, ym+x, i);                             /*  IV. Quadrant */
            e2 = err; x2 = x;                                    /* remember values */
            if (err+y > 0) {                                              /* x step */
                i = 255*(err-2*x-1)/r;                              /* outward pixel */
                if (i < 256) {
                    setterAA.setPixelAA(xm-x, ym+y+1, i);
                    setterAA.setPixelAA(xm-y-1, ym-x, i);
                    setterAA.setPixelAA(xm+x, ym-y-1, i);
                    setterAA.setPixelAA(xm+y+1, ym+x, i);
                }
                err += ++x*2+1;
            }
            if (e2+x2 <= 0) {                                             /* y step */
                i = 255*(2*y+3-e2)/r;                                /* inward pixel */
                if (i < 256) {
                    setterAA.setPixelAA(xm-x2-1, ym+y, i);
                    setterAA.setPixelAA(xm-y, ym-x2-1, i);
                    setterAA.setPixelAA(xm+x2+1, ym-y, i);
                    setterAA.setPixelAA(xm+y, ym+x2+1, i);
                }
                err += ++y*2+1;
            }
        } while (x < 0);
    }

    public void plotEllipseRectAA(int x0, int y0, int x1, int y1, IPixelSetterAA setterAA)
    {        /* draw a black anti-aliased rectangular ellipse on white background */
        long a = Math.abs(x1-x0), b = Math.abs(y1-y0), b1 = b&1;                 /* diameter */
        float dx = (float) (4*(a-1.0)*b*b), dy = 4*(b1+1)*a*a;            /* error increment */
        float ed, i, err = b1*a*a-dx+dy;                        /* error of 1.step */
        boolean f;

        if (a == 0 || b == 0){
            plotLineAA(x0,y0, x1,y1, setterAA);
            return;
        }
        if (x0 > x1) { x0 = x1; x1 += a; }        /* if called with swapped points */
        if (y0 > y1) y0 = y1;                                  /* .. exchange them */
        y0 += (b+1)/2; y1 = (int) (y0-b1);                               /* starting pixel */
        a = 8*a*a; b1 = 8*b*b;

        for (;;) {                             /* approximate ed=sqrt(dx*dx+dy*dy) */
            i = Math.min(dx,dy); ed = Math.max(dx,dy);
            if (y0 == y1+1 && err > dy && a > b1) ed = (float) (255*4./a);           /* x-tip */
            else ed = 255/(ed+2*ed*i*i/(4*ed*ed+i*i));             /* approximation */
            i = ed*Math.abs(err+dx-dy);           /* get intensity value by pixel error */
           setterAA.setPixelAA(x0,y0, i); setterAA.setPixelAA(x0,y1, i);
           setterAA.setPixelAA(x1,y0, i);setterAA.setPixelAA(x1,y1, i);

            if (f = 2*err+dy >= 0) {                  /* x step, remember condition */
                if (x0 >= x1) break;
                i = ed*(err+dx);
                if (i < 255) {
                   setterAA.setPixelAA(x0,y0+1, i);setterAA.setPixelAA(x0,y1-1, i);
                   setterAA.setPixelAA(x1,y0+1, i);setterAA.setPixelAA(x1,y1-1, i);
                }          /* do error increment later since values are still needed */
            }
            if (2*err <= dx) {                                            /* y step */
                i = ed*(dy-err);
                if (i < 255) {
                   setterAA.setPixelAA(x0+1,y0, i);setterAA.setPixelAA(x1-1,y0, i);
                   setterAA.setPixelAA(x0+1,y1, i);setterAA.setPixelAA(x1-1,y1, i);
                }
                y0++; y1--; err += dy += a;
            }
            if (f) { x0++; x1--; err -= dx -= b1; }            /* x error increment */
        }
        if (--x0 == x1++)                       /* too early stop of flat ellipses */
            while (y0-y1 < b) {
                i = 255*4*Math.abs(err+dx)/b1;               /* -> finish tip of ellipse */
               setterAA.setPixelAA(x0,++y0, i);setterAA.setPixelAA(x1,y0, i);
               setterAA.setPixelAA(x0,--y1, i);setterAA.setPixelAA(x1,y1, i);
                err += dy += a;
            }
    }

    public void plotQuadBezierSegAA(int x0, int y0, int x1, int y1, int x2, int y2, IPixelSetterAA setterAA)
        {                    /* draw an limited anti-aliased quadratic Bezier segment */
            int sx = x2-x1, sy = y2-y1;
            long xx = x0-x1, yy = y0-y1, xy;             /* relative values for checks */
            double dx, dy, err, ed, cur = xx*sy-yy*sx;                    /* curvature */

            assert(xx*sx <= 0 && yy*sy <= 0);      /* sign of gradient must not change */

            if (sx*(long)sx+sy*(long)sy > xx*xx+yy*yy) {     /* begin with longer part */
                x2 = x0; x0 = sx+x1; y2 = y0; y0 = sy+y1; cur = -cur;     /* swap P0 P2 */
            }
            if (cur != 0)
            {                                                      /* no straight line */
                xx += sx; xx *= sx = x0 < x2 ? 1 : -1;              /* x step direction */
                yy += sy; yy *= sy = y0 < y2 ? 1 : -1;              /* y step direction */
                xy = 2*xx*yy; xx *= xx; yy *= yy;             /* differences 2nd degree */
                if (cur*sx*sy < 0) {                              /* negated curvature? */
                    xx = -xx; yy = -yy; xy = -xy; cur = -cur;
                }
                dx = 4.0*sy*(x1-x0)*cur+xx-xy;                /* differences 1st degree */
                dy = 4.0*sx*(y0-y1)*cur+yy-xy;
                xx += xx; yy += yy; err = dx+dy+xy;                   /* error 1st step */
                do {
                    cur = Math.min(dx+xy,-xy-dy);
                    ed = Math.max(dx+xy,-xy-dy);               /* approximate error distance */
                    ed += 2*ed*cur*cur/(4*ed*ed+cur*cur);
                    setterAA.setPixelAA(x0,y0, (float) (255*Math.abs(err-dx-dy-xy)/ed));          /* plot curve */
                    if (x0 == x2 || y0 == y2) break;     /* last pixel -> curve finished */
                    x1 = x0; cur = dx-err;
                    boolean yErr = 2*err+dy < 0;
                    if (2*err+dx > 0) {                                        /* x step */
                        if (err-dy < ed)setterAA.setPixelAA(x0,y0+sy, (float) (255*Math.abs(err-dy)/ed));
                        x0 += sx; dx -= xy; err += dy += yy;
                    }
                    if (yErr) {                                                  /* y step */
                        if (cur < ed)setterAA.setPixelAA(x1+sx,y0, (float) (255*Math.abs(cur)/ed));
                        y0 += sy; dy -= xy; err += dx += xx;
                    }
                } while (dy < dx);                  /* gradient negates -> close curves */
            }
            plotLineAA(x0,y0, x2,y2, setterAA);                  /* plot remaining needle to end */
        }

    public void plotQuadRationalBezierSegAA(int x0, int y0, int x1, int y1,
        int x2, int y2, float w, IPixelSetterAA setterAA)
        {   /* draw an anti-aliased rational quadratic Bezier segment, squared weight */
            int sx = x2-x1, sy = y2-y1;                  /* relative values for checks */
            double dx = x0-x2, dy = y0-y2, xx = x0-x1, yy = y0-y1;
            double xy = xx*sy+yy*sx, cur = xx*sy-yy*sx, err, ed;          /* curvature */
            boolean f;

            assert(xx*sx <= 0.0 && yy*sy <= 0.0);  /* sign of gradient must not change */

            if (cur != 0.0 && w > 0.0) {                           /* no straight line */
                if (sx*(long)sx+sy*(long)sy > xx*xx+yy*yy) {  /* begin with longer part */
                    x2 = x0; x0 -= dx; y2 = y0; y0 -= dy; cur = -cur;      /* swap P0 P2 */
                }
                xx = 2.0*(4.0*w*sx*xx+dx*dx);                 /* differences 2nd degree */
                yy = 2.0*(4.0*w*sy*yy+dy*dy);
                sx = x0 < x2 ? 1 : -1;                              /* x step direction */
                sy = y0 < y2 ? 1 : -1;                              /* y step direction */
                xy = -2.0*sx*sy*(2.0*w*xy+dx*dy);

                if (cur*sx*sy < 0) {                              /* negated curvature? */
                    xx = -xx; yy = -yy; cur = -cur; xy = -xy;
                }
                dx = 4.0*w*(x1-x0)*sy*cur+xx/2.0+xy;          /* differences 1st degree */
                dy = 4.0*w*(y0-y1)*sx*cur+yy/2.0+xy;

                if (w < 0.5 && dy > dx) {              /* flat ellipse, algorithm fails */
                    cur = (w+1.0)/2.0; w = (float) Math.sqrt(w); xy = 1.0/(w+1.0);
                    sx = (int) floor((x0+2.0*w*x1+x2)*xy/2.0+0.5); /* subdivide curve in half  */
                    sy = (int) floor((y0+2.0*w*y1+y2)*xy/2.0+0.5);
                    dx = floor((w*x1+x0)*xy+0.5); dy = floor((y1*w+y0)*xy+0.5);
                    plotQuadRationalBezierSegAA(x0,y0, (int)dx,(int)dy, sx,sy, (float)cur, setterAA); /* plot apart */
                    dx = floor((w*x1+x2)*xy+0.5); dy = floor((y1*w+y2)*xy+0.5);
                    plotQuadRationalBezierSegAA(sx,sy, (int)dx,(int)dy, x2,y2, (float)cur, setterAA);
                    return;
                }
                err = dx+dy-xy;                                       /* error 1st step */
                do {                                                      /* pixel loop */
                    cur = Math.min(dx-xy,xy-dy); ed = Math.max(dx-xy,xy-dy);
                    ed += 2*ed*cur*cur/(4.*ed*ed+cur*cur); /* approximate error distance */
                    x1 = (int) (255*Math.abs(err-dx-dy+xy)/ed);    /* get blend value by pixel error */
                    if (x1 < 256)setterAA.setPixelAA(x0,y0, x1);                   /* plot curve */
                    if (f = 2*err+dy < 0) {                                    /* y step */
                        if (y0 == y2) return;             /* last pixel -> curve finished */
                        if (dx-err < ed)setterAA.setPixelAA(x0+sx,y0, (float) (255*Math.abs(dx-err)/ed));
                    }
                    if (2*err+dx > 0) {                                        /* x step */
                        if (x0 == x2) return;             /* last pixel -> curve finished */
                        if (err-dy < ed)setterAA.setPixelAA(x0,y0+sy, (float) (255*Math.abs(err-dy)/ed));
                        x0 += sx; dx += xy; err += dy += yy;
                    }
                    if (f) { y0 += sy; dy += xy; err += dx += xx; }            /* y step */
                } while (dy < dx);               /* gradient negates -> algorithm fails */
            }
            plotLineAA(x0, y0, x2, y2, setterAA);                  /* plot remaining needle to end */
        }

    public void plotCubicBezierSegAA(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, IPixelSetterAA setterAA)
    {                           /* plot limited anti-aliased cubic Bezier segment */
        int f, fx, fy, leg = 1;
        int sx = x0 < x3 ? 1 : -1, sy = y0 < y3 ? 1 : -1;        /* step direction */
        float xc = -Math.abs(x0+x1-x2-x3), xa = xc-4*sx*(x1-x2), xb = sx*(x0-x1-x2+x3);
        float yc = -Math.abs(y0+y1-y2-y3), ya = yc-4*sy*(y1-y2), yb = sy*(y0-y1-y2+y3);
        double ab, ac, bc, ba, xx, xy, yy, dx, dy, ex, px, py, ed, ip, EP = 0.1;
        /* check for curve restrains */
        /* slope P0-P1 == P2-P3    and  (P0-P3 == P1-P2      or   no slope change) */
        assert((x1-x0)*(x2-x3) < EP && ((x3-x0)*(x1-x2) < EP || xb*xb < xa*xc+EP));
        assert((y1-y0)*(y2-y3) < EP && ((y3-y0)*(y1-y2) < EP || yb*yb < ya*yc+EP));
        if (xa == 0 && ya == 0) {                                /* quadratic Bezier */
            plotQuadBezierSegAA(x0, y0, (3 * x1 - x0) >> 1, (3 * y1 - y0) >> 1, x3, y3, setterAA);
            return;
        }
        x1 = (x1-x0)*(x1-x0)+(y1-y0)*(y1-y0)+1;                    /* line lengths */
        x2 = (x2-x3)*(x2-x3)+(y2-y3)*(y2-y3)+1;
        exit:
        do {                                                /* loop over both ends */
            ab = xa*yb-xb*ya; ac = xa*yc-xc*ya; bc = xb*yc-xc*yb;
            ip = 4*ab*bc-ac*ac;                   /* self intersection loop at all? */
            ex = ab*(ab+ac-3*bc)+ac*ac;       /* P0 part of self-intersection loop? */
            f = ex > 0 ? 1 : (int) floor(Math.sqrt(1 + 1024 / x1));   /* calc resolution */
            ab *= f; ac *= f; bc *= f; ex *= f*f;            /* increase resolution */
            xy = 9*(ab+ac+bc)/8; ba = 8*(xa-ya);  /* init differences of 1st degree */
            dx = 27*(8*ab*(yb*yb-ya*yc)+ex*(ya+2*yb+yc))/64-ya*ya*(xy-ya);
            dy = 27*(8*ab*(xb*xb-xa*xc)-ex*(xa+2*xb+xc))/64-xa*xa*(xy+xa);
            /* init differences of 2nd degree */
            xx = 3*(3*ab*(3*yb*yb-ya*ya-2*ya*yc)-ya*(3*ac*(ya+yb)+ya*ba))/4;
            yy = 3*(3*ab*(3*xb*xb-xa*xa-2*xa*xc)-xa*(3*ac*(xa+xb)+xa*ba))/4;
            xy = xa*ya*(6*ab+6*ac-3*bc+ba); ac = ya*ya; ba = xa*xa;
            xy = 3*(xy+9*f*(ba*yb*yc-xb*xc*ac)-18*xb*yb*ab)/8;

            if (ex < 0) {         /* negate values if inside self-intersection loop */
                dx = -dx; dy = -dy; xx = -xx; yy = -yy; xy = -xy; ac = -ac; ba = -ba;
            }                                     /* init differences of 3rd degree */
            ab = 6*ya*ac; ac = -6*xa*ac; bc = 6*ya*ba; ba = -6*xa*ba;
            dx += xy; ex = dx+dy; dy += xy;                    /* error of 1st step */
            loop:
            for (fx = fy = f; ; ) {
                if (x0 == x3 || y0 == y3) break exit;
                y1 = (int) Math.min(Math.abs(xy-dx),Math.abs(dy-xy));
                ed = Math.max(Math.abs(xy-dx),Math.abs(dy-xy));   /* approximate err */
                ed = f*(ed+2*ed*y1*y1/(4*ed*ed+y1*y1));
                y1 = (int) (255*Math.abs(ex-(f-fx+1)*dx-(f-fy+1)*dy+f*xy)/ed);
                if (y1 < 256) setterAA.setPixelAA(x0, y0, y1);                  /* plot curve */
                px = Math.abs(ex-(f-fx+1)*dx+(fy-1)*dy);   /* pixel varensity x move */
                py = Math.abs(ex+(fx-1)*dx-(f-fy+1)*dy);   /* pixel varensity y move */
                y2 = y0;
                do {                                  /* move sub-steps of one pixel */
                    if (dx+xx > xy || dy+yy < xy) break loop;  /* two x or y steps */
                    double yErr = 2*ex+dx;                    /* save value for test of y step */
                    if (2*ex+dy > 0) {                                  /* x sub-step */
                        fx--; ex += dx += xx; dy += xy += ac; yy += bc; xx += ab;
                    } else if (yErr > 0) break loop;                /* tiny nearly cusp */
                    if (yErr <= 0) {                                      /* y sub-step */
                        fy--; ex += dy += yy; dx += xy += bc; xx += ac; yy += ba;
                    }
                } while (fx > 0 && fy > 0);                       /* pixel complete? */
                if (2*fy <= f) {                           /* x+ anti-aliasing pixel */
                    if (py < ed) setterAA.setPixelAA(x0+sx, y0, (float) (255*py/ed));      /* plot curve */
                    y0 += sy; fy += f;                                      /* y step */
                }
                if (2*fx <= f) {                           /* y+ anti-aliasing pixel */
                    if (px < ed) setterAA.setPixelAA(x0, y2+sy, (float) (255*px/ed));      /* plot curve */
                    x0 += sx; fx += f;                                      /* x step */
                }
            }
            if (2*ex < dy && 2*fy <= f+2) {         /* round x+ approximation pixel */
                if (py < ed) setterAA.setPixelAA(x0+sx, y0, (float) (255*py/ed));         /* plot curve */
                y0 += sy;
            }
            if (2*ex > dx && 2*fx <= f+2) {         /* round y+ approximation pixel */
                if (px < ed) setterAA.setPixelAA(x0, y2+sy, (float) (255*px/ed));         /* plot curve */
                x0 += sx;
            }
            xx = x0; x0 = x3; x3 = (int) xx; sx = -sx; xb = -xb;             /* swap legs */
            yy = y0; y0 = y3; y3 = (int) yy; sy = -sy; yb = -yb; x1 = x2;
        } while (leg-->0);                                          /* try other end */
        plotLineAA(x0,y0, x3,y3, setterAA);     /* remaining part in case of cusp or crunode */
    }

    /*
        void plotLineWidth(int x0, int y0, int x1, int y1, float wd)
        {                                    /* plot an anti-aliased line of width wd
            int dx = Math.abs(x1-x0), sx = x0 < x1 ? 1 : -1;
            int dy = Math.abs(y1-y0), sy = y0 < y1 ? 1 : -1;
            int err = dx-dy, e2, x2, y2;                           /* error value e_xy
            float ed = dx+dy == 0 ? 1 : (float) Math.sqrt((float) dx * dx + (float) dy * dy);

            for (wd = (wd+1)/2; ; ) {                                    /* pixel loop
                setPixelColor(x0, y0, Math.max(0,255*(Math.abs(err-dx+dy)/ed-wd+1)));
                e2 = err; x2 = x0;
                if (2*e2 >= -dx) {                                            /* x step
                    for (e2 += dy, y2 = y0; e2 < ed*wd && (y1 != y2 || dx > dy); e2 += dx)
                        setPixelColor(x0, y2 += sy, Math.max(0,255*(Math.abs(e2)/ed-wd+1)));
                    if (x0 == x1) break;
                    e2 = err; err -= dy; x0 += sx;
                }
                if (2*e2 <= dy) {                                             /* y step
                    for (e2 = dx-e2; e2 < ed*wd && (x1 != x2 || dx < dy); e2 += dy)
                        setPixelColor(x2 += sx, y0, Math.max(0,255*(Math.abs(e2)/ed-wd+1)));
                    if (y0 == y1) break;
                    err += dx; y0 += sy;
                }
            }
        }

     */

    public void plotQuadSpline(int n, int x[], int y[], IPixelSetter pixelSetter)
        {                         /* plot quadratic spline, destroys input arrays x,y */
            int M_MAX = 6;
            float mi = 1;                    /* diagonal constants of matrix */
            float[] m = new float[M_MAX];
            int i, x0, y0, x1, y1, x2 = x[n], y2 = y[n];

            assert(n > 1);                        /* need at least 3 points P[0]..P[n] */

            x[1] = x0 = 8*x[1]-2*x[0];                          /* first row of matrix */
            y[1] = y0 = 8*y[1]-2*y[0];

            for (i = 2; i < n; i++) {                                 /* forward sweep */
                if (i-2 < M_MAX) m[i-2] = mi = 1.0F/(6.0F-mi);
                x[i] = x0 = (int) floor(8*x[i]-x0*mi+0.5F);                        /* store yi */
                y[i] = y0 = (int) floor(8*y[i]-y0*mi+0.5F);
            }
            x1 = (int) floor((x0-2*x2)/(5.0-mi)+0.5);                 /* correction last row */
            y1 = (int) floor((y0-2*y2)/(5.0-mi)+0.5);

            for (i = n-2; i > 0; i--) {                           /* back substitution */
                if (i <= M_MAX) mi = m[i-1];
                x0 = (int) floor((x[i]-x1)*mi+0.5);                            /* next corner */
                y0 = (int) floor((y[i]-y1)*mi+0.5);
                plotQuadBezier((x0+x1)/2,(y0+y1)/2, x1,y1, x2,y2, pixelSetter);
                x2 = (x0+x1)/2; x1 = x0;
                y2 = (y0+y1)/2; y1 = y0;
            }
            plotQuadBezier(x[0],y[0], x1,y1, x2,y2, pixelSetter);
        }

    public void plotCubicSpline(int n, int[] x, int[] y, IPixelSetter pixelSetter)
        {                             /* plot cubic spline, destroys input arrays x,y */
            int M_MAX = 6;
            float mi = 0.25F;                 /* diagonal constants of matrix */
            float[] m = new float[M_MAX];
            int x3 = x[n-1], y3 = y[n-1], x4 = x[n], y4 = y[n];
            int i, x0, y0, x1, y1, x2, y2;

            assert(n > 2);                        /* need at least 4 points P[0]..P[n] */

            x[1] = x0 = 12*x[1]-3*x[0];                         /* first row of matrix */
            y[1] = y0 = 12*y[1]-3*y[0];

            for (i = 2; i < n; i++) {                                /* foreward sweep */
                if (i-2 < M_MAX) m[i-2] = mi = (float) (0.25/(2.0-mi));
                x[i] = x0 = (int) floor(12*x[i]-2*x0*mi+0.5);
                y[i] = y0 = (int) floor(12*y[i]-2*y0*mi+0.5);
            }
            x2 = (int) floor((x0-3*x4)/(7-4*mi)+0.5);                    /* correct last row */
            y2 = (int) floor((y0-3*y4)/(7-4*mi)+0.5);
            plotCubicBezier(x3,y3, (x2+x4)/2,(y2+y4)/2, x4,y4, x4,y4, pixelSetter);

            if (n-3 < M_MAX) mi = m[n-3];
            x1 = (int) floor((x[n-2]-2*x2)*mi+0.5);
            y1 = (int) floor((y[n-2]-2*y2)*mi+0.5);
            for (i = n-3; i > 0; i--) {                           /* back substitution */
                if (i <= M_MAX) mi = m[i-1];
                x0 = (int) floor((x[i]-2*x1)*mi+0.5);
                y0 = (int) floor((y[i]-2*y1)*mi+0.5);
                x4 = (int) floor((x0+4*x1+x2+3)/6.0);                     /* reconstruct P[i] */
                y4 = (int) floor((y0+4*y1+y2+3)/6.0);
                plotCubicBezier(x4,y4,
                        (int)floor((2*x1+x2)/3+0.5),(int)floor((2*y1+y2)/3+0.5),
                        (int)floor((x1+2*x2)/3+0.5),(int)floor((y1+2*y2)/3+0.5),
                        x3,y3, pixelSetter);
                x3 = x4; y3 = y4; x2 = x1; y2 = y1; x1 = x0; y1 = y0;
            }
            x0 = x[0]; x4 = (int)floor((3*x0+7*x1+2*x2+6)/12.0);        /* reconstruct P[1] */
            y0 = y[0]; y4 = (int)floor((3*y0+7*y1+2*y2+6)/12.0);
            plotCubicBezier(x4,y4, (int)floor((2*x1+x2)/3+0.5),(int)floor((2*y1+y2)/3+0.5),
                    (int)floor((x1+2*x2)/3+0.5),(int)floor((y1+2*y2)/3+0.5), x3,y3, pixelSetter);
            plotCubicBezier(x0,y0, x0,y0, (x0+x1)/2,(y0+y1)/2, x4,y4, pixelSetter);
        }

}
