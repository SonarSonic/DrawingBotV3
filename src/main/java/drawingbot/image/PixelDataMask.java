package drawingbot.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * An implementation of PixelData which is only for use as a SoftClip on other pixel datas, it may have other limited uses.
 * It override the default {@link java.awt.Shape} to provide a fast access contains method.
 *
 * By creating the implementation like this if we wish PixelData which take soft clips can accept any Shape or a PixelDataMask.
 *
 * Typical Mask Values
 * 0 = Ignore
 * 1 = Draw
 */
public class PixelDataMask extends PixelDataLuminance implements Shape {

    public Rectangle bounds;

    public PixelDataMask(int width, int height) {
        super(width, height);
        this.bounds = new Rectangle(0, 0, width, height);

    }

    public PixelDataMask(int width, int height, Shape maskShape) {
        this(width, height);
        updateDataFromShapeMask(maskShape);
    }

    public PixelDataMask(int width, int height, List<Shape> masks) {
        this(width, height);
        updateDataFromShapeMasks(masks);
    }

    public void updateDataFromShapeMask(Shape maskShape){
        for(int x = 0; x < width; x ++){
            for(int y = 0; y < height; y ++){
                luminance.setData(x, y, maskShape != null && maskShape.contains(x, y) ? 1 : 0);
            }
        }
    }

    public void updateDataFromShapeMasks(List<Shape> shapes){
        for(Shape shape : shapes) {
            Rectangle bounds = shape.getBounds();
            for (int x = (int) bounds.getMinX(); x <= bounds.getMaxX(); x++) {
                for (int y = (int) bounds.getMinY(); y <= bounds.getMaxY(); y++) {
                    if(shape.contains(x, y)){
                        luminance.setData(x, y, 1);
                    }
                }
            }
        }
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        // This pixel data doesn't need to be accurate to the actual luminance, we just want to make sure we pick up the correct mask values.
        luminance.setData(x, y, Math.max(Math.max(a, r), Math.max(g, b)));
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return bounds;
    }

    @Override
    public boolean contains(double x, double y) {
        if(bounds.contains(x, y)){
            return getLuminance((int)x, (int)y) > 0;
        }
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
        if(bounds.contains(p)){
            return getLuminance((int)p.getX(), (int)p.getY()) > 0;
        }
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        Rectangle2D area = r.createUnion(bounds);
        for(int x = 0; x < area.getMaxX(); x++){
            for(int y = 0; y < area.getMaxY(); y++){
                if(getLuminance(x, y) > 0){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return contains(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public boolean contains(Rectangle2D r) {
        Rectangle2D area = r.createUnion(bounds);
        for(int x = 0; x < area.getMaxX(); x++){
            for(int y = 0; y < area.getMaxY(); y++){
                if(getLuminance(x, y) == 0){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        throw new UnsupportedOperationException("Pixel Data Mask: Doesn't support Path Iterators");
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        throw new UnsupportedOperationException("Pixel Data Mask: Doesn't support Path Iterators");
    }

    @Override
    public String getType() {
        return "Mask L";
    }
}
