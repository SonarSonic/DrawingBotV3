package drawingbot.geom.basic;

import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class GLine extends Line2D.Float implements IGeometry, IPathElement{

    public GLine() {
        super();
    }

    public GLine(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
    }

    public GLine(Point2D p1, Point2D p2) {
        super(p1, p2);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(x1, y1);
        }
        path.lineTo(x2, y2);
    }

    //// IGeometry \\\\

    public Integer penIndex = null;
    public Integer sampledRGBA = null;

    @Override
    public int getSegmentCount() {
        return 2;
    }

    @Override
    public Shape getAWTShape() {
        return this;
    }

    @Override
    public Integer getPenIndex() {
        return penIndex;
    }

    @Override
    public Integer getCustomRGBA() {
        return sampledRGBA;
    }

    @Override
    public void setPenIndex(Integer index) {
        penIndex = index;
    }

    @Override
    public void setCustomRGBA(Integer rgba) {
        sampledRGBA = rgba;
    }

    @Override
    public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
        pen.preRenderFX(graphics, this);
        graphics.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void transform(AffineTransform transform) {
        float[] coords = new float[]{x1, y1, x2, y2};
        transform.transform(coords, 0, coords, 0, 2);
        x1 = coords[0];
        y1 = coords[1];
        x2 = coords[2];
        y2 = coords[3];
    }
}
