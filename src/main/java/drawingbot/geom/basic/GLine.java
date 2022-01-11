package drawingbot.geom.basic;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.render.opengl.VertexBuilder;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class GLine extends Line2D.Float implements IGeometry, IPathElement {

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

    public int geometryIndex = -1;
    public Integer penIndex = null;
    public Integer sampledRGBA = null;
    public int groupID = -1;

    @Override
    public int getVertexCount() {
        return 2;
    }

    @Override
    public Shape getAWTShape() {
        return this;
    }

    @Override
    public Integer getGeometryIndex() {
        return geometryIndex;
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
    public int getGroupID() {
        return groupID;
    }

    @Override
    public void setGeometryIndex(Integer index) {
        geometryIndex = index;
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
    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    @Override
    public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
        pen.preRenderFX(graphics, this);
        graphics.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void renderVertices(VertexBuilder vertexBuilder, ObservableDrawingPen pen) {
        vertexBuilder.addVertex(0, x1, y1);
        vertexBuilder.addVertex(1, x2, y2);
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotLine((int)x1, (int)y1, (int)x2, (int)y2, setter);
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

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x1, y1);
    }
}
