package drawingbot.utils;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottedDrawing;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VertexIterator implements PathIterator {

    public List<IGeometry> geometries;
    public IGeometry lastGeometry;
    public IGeometry currentGeometry;
    public boolean changedGeometry = true;
    public int geometryVertexCount = 0;

    public Iterator<IGeometry> geometryIterator;
    public PathIterator pathIterator;
    public AffineTransform transform;

    private final FakeShape fakeShape;
    private final WrappedIterator wrappedIterator;

    private int lastSegmentType = -1;
    private double[] lastSegment = new double[6];

    public VertexIterator(List<IGeometry> geometries, @Nullable AffineTransform transform, boolean reverse){
        assert !geometries.isEmpty();
        this.geometries = geometries;

        if(reverse){
            this.geometries = new ArrayList<>(geometries);
            Collections.reverse(this.geometries);
        }

        this.transform = transform;
        this.lastGeometry = null;
        this.geometryIterator = this.geometries.iterator();
        this.currentGeometry = geometryIterator.next();
        this.pathIterator = currentGeometry.getAWTShape().getPathIterator(transform);

        this.wrappedIterator = new WrappedIterator(this, 0);
        this.fakeShape = new FakeShape(wrappedIterator);
    }

    @Override
    public int getWindingRule() {
        return PathIterator.WIND_EVEN_ODD;
    }

    @Override
    public boolean isDone() {
        return !geometryIterator.hasNext() && (pathIterator == null || pathIterator.isDone());
    }

    @Override
    public void next() {
        if(pathIterator.isDone()){
            nextGeometry();
        }else{
            pathIterator.next();
            changedGeometry = false;
            geometryVertexCount ++;
        }
    }

    public void nextGeometry(){
        lastGeometry = currentGeometry;
        if(geometryIterator.hasNext()){
            currentGeometry = geometryIterator.next();
            pathIterator = currentGeometry.getAWTShape().getPathIterator(transform);
            changedGeometry = true;
            geometryVertexCount = 0;
        }else{
            currentGeometry = null;
            pathIterator = null;
        }
        changedGeometry = true;
        geometryVertexCount = 0;
    }

    @Override
    public int currentSegment(float[] coords) {
        return pathIterator.currentSegment(coords);
    }

    @Override
    public int currentSegment(double[] coords) {
        return pathIterator.currentSegment(coords);
    }

    //TODO FIXME me skipping first / last path, when set to 1 vertex per frame
    public void renderVerticesAWT(PlottedDrawing drawing, Graphics2D graphics, int maxSegments){
        int segments = 0;
        while(!isDone() && segments < maxSegments){
            ObservableDrawingPen pen = drawing.drawingPenSet.getPen(currentGeometry.getPenIndex());
            if(geometryVertexCount == 0 && currentGeometry.getSegmentCount() + segments < maxSegments){
                currentGeometry.renderAWT(graphics, pen);
                segments += currentGeometry.getSegmentCount();
                nextGeometry();
            }else{
                double moveX = 0;
                double moveY = 0;
                switch (lastSegmentType){
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        moveX = lastSegment[0];
                        moveY = lastSegment[1];
                        break;
                    case PathIterator.SEG_QUADTO:
                        moveX = lastSegment[1];
                        moveY = lastSegment[2];
                        break;
                    case PathIterator.SEG_CUBICTO:
                        moveX = lastSegment[2];
                        moveY = lastSegment[3];
                        break;
                    case PathIterator.SEG_CLOSE:
                        ///NOTE: minor bug, if the last segment was a close it will be ignored currently...
                        break;
                }

                wrappedIterator.setAndResetSegments(maxSegments - segments, lastSegmentType != -1 && lastSegmentType != SEG_CLOSE, moveX, moveY);
                pen.preRenderAWT(graphics, currentGeometry);
                graphics.draw(fakeShape);

                if(pathIterator != null && !pathIterator.isDone()){
                    lastSegmentType = wrappedIterator.currentSegment(lastSegment);
                }else{
                    lastSegmentType = -1;
                }

                segments += wrappedIterator.segments;
                next();
            }
        }
    }

    public void renderGeometryFX(PlottedDrawing drawing, Graphics2D graphics, int maxSegments){
        /*
        int segments = 0;
        while(!isDone()){
            ObservableDrawingPen pen = drawing.drawingPenSet.getPen(currentGeometry.getPenIndex());
            if(geometryVertexCount == 0 && currentGeometry.getSegmentCount() + segments < maxSegments){
                currentGeometry.renderAWT(graphics, pen);
                nextGeometry();
                lastSegmentType = -1;
            }else{
                double moveX = 0;
                double moveY = 0;
                switch (lastSegmentType){
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        moveX = lastSegment[0];
                        moveY = lastSegment[1];
                        break;
                    case PathIterator.SEG_QUADTO:
                        moveX = lastSegment[1];
                        moveY = lastSegment[2];
                        break;
                    case PathIterator.SEG_CUBICTO:
                        moveX = lastSegment[2];
                        moveY = lastSegment[3];
                        break;
                    case PathIterator.SEG_CLOSE:
                        ///NOTE: minor bug, if the last segment was a close it will be ignored currently...
                        break;
                }

                wrappedIterator.setAndResetSegments(maxSegments - segments, lastSegmentType != -1 && lastSegmentType != SEG_CLOSE, moveX, moveY);
                pen.preRenderAWT(graphics, currentGeometry);
                graphics.draw(fakeShape);

                lastSegmentType = wrappedIterator.currentSegment(lastSegment);
                segments += wrappedIterator.segments;
                next();
            }
        }

         */
    }

    public static class WrappedIterator implements PathIterator{

        public VertexIterator wrapped;
        public int maxSegments;
        public int segments;

        public boolean hasMoveTo;
        public double moveX;
        public double moveY;

        public WrappedIterator(VertexIterator wrapped, int maxSegments){
            this.wrapped = wrapped;
            this.maxSegments = maxSegments;
        }

        public void setAndResetSegments(int maxSegments, boolean hasMoveTo, double moveX, double moveY){
            this.maxSegments = maxSegments;
            this.segments = 0;

            this.hasMoveTo = hasMoveTo;
            this.moveX = moveX;
            this.moveY = moveY;
        }

        @Override
        public int getWindingRule() {
            return wrapped.getWindingRule();
        }

        @Override
        public boolean isDone() {
            return wrapped.pathIterator == null || wrapped.pathIterator.isDone() || segments > maxSegments + 1;
        }

        @Override
        public void next() {
            if(hasMoveTo){
                hasMoveTo = false;
            }else{
                wrapped.next();
                segments++;
            }
        }

        @Override
        public int currentSegment(float[] coords) {
            if(hasMoveTo){
                coords[0] = (float) moveX;
                coords[1] = (float) moveY;
                return PathIterator.SEG_MOVETO;
            }
            return wrapped.currentSegment(coords);
        }

        @Override
        public int currentSegment(double[] coords) {
            if(hasMoveTo){
                coords[0] = moveX;
                coords[1] = moveY;
                return PathIterator.SEG_MOVETO;
            }
            return wrapped.currentSegment(coords);
        }
    }

    public static class FakeShape implements Shape {

        public final WrappedIterator iterator;

        public FakeShape(WrappedIterator iterator){
            this.iterator = iterator;
        }

        @Override
        public Rectangle getBounds() {
            return iterator.wrapped.currentGeometry.getAWTShape().getBounds();
        }

        @Override
        public Rectangle2D getBounds2D() {
            return iterator.wrapped.currentGeometry.getAWTShape().getBounds2D();
        }

        @Override
        public boolean contains(double x, double y) {
            return iterator.wrapped.currentGeometry.getAWTShape().contains(x, y);
        }

        @Override
        public boolean contains(Point2D p) {
            return iterator.wrapped.currentGeometry.getAWTShape().contains(p);
        }

        @Override
        public boolean intersects(double x, double y, double w, double h) {
            return iterator.wrapped.currentGeometry.getAWTShape().intersects(x, y, w, h);
        }

        @Override
        public boolean intersects(Rectangle2D r) {
            return iterator.wrapped.currentGeometry.getAWTShape().intersects(r);
        }

        @Override
        public boolean contains(double x, double y, double w, double h) {
            return iterator.wrapped.currentGeometry.getAWTShape().contains(x, y, w, h);
        }

        @Override
        public boolean contains(Rectangle2D r) {
            return iterator.wrapped.currentGeometry.getAWTShape().contains(r);
        }

        @Override
        public PathIterator getPathIterator(AffineTransform at) {
            return iterator;
        }

        @Override
        public PathIterator getPathIterator(AffineTransform at, double flatness) {
            return iterator;
        }
    }
}
