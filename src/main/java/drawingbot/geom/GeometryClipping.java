package drawingbot.geom;

import drawingbot.geom.shapes.*;
import drawingbot.image.PixelDataMask;
import drawingbot.plotting.PathBuilder;
import drawingbot.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Deprecated //will be replaced in future versions
public class GeometryClipping {

    //TODO someone decide what accuracy is needed or make this configurable
    public static float curveSplits = 128;
    public static float accuracy = 0.5F;

    public static boolean shouldClip(Shape shape, IGeometry geometry, boolean outside) {
        if(outside){
            Rectangle2D bounds = geometry.getAWTShape().getBounds2D();
            return shape.intersects(bounds.getX(), bounds.getY(), Math.max(0.1, bounds.getWidth()), Math.max(0.1, bounds.getHeight()));
        }
        // Fast rectangle checks to avoid unnecessary clipping operations
        if(shape instanceof Rectangle2D rectangle2D){
            if(geometry instanceof GLine line){
                return !rectangle2D.contains(line.getX1(), line.getY1()) || !rectangle2D.contains(line.getX2(), line.getY2());
            }
        }
        Rectangle2D bounds = geometry.getAWTShape().getBounds2D();
        return !shape.contains(bounds.getX(), bounds.getY(), Math.max(0.1, bounds.getWidth()), Math.max(0.1, bounds.getHeight()));
    }

    public static List<IGeometry> clip(Shape shape, IGeometry geometry, double tolerance, boolean outside) {
        List<IGeometry> pathElements = new ArrayList<>();
        GeometryUtils.splitGPath(geometry instanceof GPath ? (GPath) geometry : new GPath(geometry.getAWTShape()), pathElements::add);

        List<IGeometry> geometries = new ArrayList<>();
        PathBuilder pathBuilder = new PathBuilder(geometries::add);
        pathBuilder.startPath();

        // Change the tolerance on PixelDataMasks to only be pixel accurate, as they can't clip at higher resolution
        if(shape instanceof PixelDataMask){
            tolerance = 1;
        }

        for (IGeometry element : pathElements) {
            if (element instanceof GLine) {
                recursiveSplitLine(shape, (GLine) element, outside, tolerance, (g) -> {
                    GLine line = (GLine) g;
                    pathBuilder.lineTo(line.getX1(), line.getY1(), line.getX2(), line.getY2());
                });
            } else if (element instanceof GQuadCurve) {
                recursiveSplitGQuadCurve(shape, (GQuadCurve) element, outside, tolerance, (g) -> {
                    GCubicCurve cubic = (GCubicCurve) g;
                    pathBuilder.curveTo(cubic.getX1(), cubic.getY1(), cubic.getCtrlX1(), cubic.getCtrlY1(), cubic.getCtrlX2(), cubic.getCtrlY2(), cubic.getX2(), cubic.getY2());
                });
            } else if (element instanceof GCubicCurve) {
                recursiveSplitCubicCurve(shape, (GCubicCurve) element, outside, tolerance, (g) -> {
                    GCubicCurve cubic = (GCubicCurve) g;
                    pathBuilder.curveTo(cubic.getX1(), cubic.getY1(), cubic.getCtrlX1(), cubic.getCtrlY1(), cubic.getCtrlX2(), cubic.getCtrlY2(), cubic.getX2(), cubic.getY2());
                });
            } else {
                throw new UnsupportedOperationException("Invalid element type: " + element.getClass().getSimpleName());
            }
        }
        pathBuilder.endPath();
        geometries.forEach(g -> GeometryUtils.copyGeometryData(g, geometry));

        //assert geometries.size() != 1 || geometries.get(0).getAWTShape().equals(geometry.getAWTShape());

        return geometries;
    }

    public static void recursiveSplitLine(Shape shape, GLine line, boolean outside, double tolerance, Consumer<IGeometry> consumer) {
        if(shape instanceof Rectangle2D){
            Rectangle2D bounds = (Rectangle2D) shape;
            if(bounds.contains(line.getX1(), line.getY1()) && bounds.contains(line.getX2(), line.getY2())){
                consumer.accept(line);
                return;
            }
        }
        float[] start = new float[]{line.getX1(), line.getY1()};
        float[] end = new float[]{line.getX2(), line.getY2()};
        double length = Utils.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        double position = 0;
        boolean isInside = shape.contains(start[0], start[1]);

        if(outside){
            isInside = !isInside;
        }

        float[] lastSample = null;

        while (position < length) {
            float[] point = pointAlongLineSegment(start, end, position / length);

            //Increment the position for the next point
            position += accuracy;

            // Often our samples will fall within the same pixel, we don't need this level of accuracy, so wait until the distance between samples is greater than the tolerance, we don't use distance calculation because it's slower, and instead check for a big enough change in X or Y
            if(lastSample != null){
                if(Math.abs(lastSample[0] - point[0]) < tolerance && Math.abs(lastSample[1] - point[1]) < tolerance){
                    continue;
                }
            }
            lastSample = point;

            boolean contains = shape.contains(point[0], point[1]);

            if(outside){
                contains = !contains;
            }

            if (isInside != contains) {
                if (isInside) consumer.accept(new GLine(start[0], start[1], point[0], point[1]));
                recursiveSplitLine(shape, new GLine(point[0], point[1], end[0], end[1]), outside, tolerance, consumer);
                return;
            }
        }
        if (isInside) {
            consumer.accept(line);
        }
    }

    public static void recursiveSplitGQuadCurve(Shape shape, GQuadCurve curve, boolean outside, double tolerance, Consumer<IGeometry> consumer) {
        //splitting a quad curve will results in two cubic curves
        recursiveSplitCubicCurve(shape, new GCubicCurve(curve), outside, tolerance, consumer);
    }

    /**
     * Note: this method is inaccurate but fast, for a more accurate method, evenly spaced points would need to be used
     */
    public static void recursiveSplitCubicCurve(Shape shape, GCubicCurve curve, boolean outside, double tolerance, Consumer<IGeometry> consumer) {
        float[] start = new float[]{curve.getX1(), curve.getY1()};
        float[] control1 = new float[]{curve.getCtrlX1(), curve.getCtrlY1()};
        float[] control2 = new float[]{curve.getCtrlX2(), curve.getCtrlY2()};
        float[] end = new float[]{curve.getX2(), curve.getY2()};
        double samples = Math.max(curveSplits, Utils.distance(curve.getX1(), curve.getY1(), curve.getCtrlX1(), curve.getCtrlY1()) +
                                       Utils.distance(curve.getCtrlX1(), curve.getCtrlY1(), curve.getCtrlX2(), curve.getCtrlY2()) +
                                       Utils.distance(curve.getCtrlX2(), curve.getCtrlY2(), curve.getX2(), curve.getY2()));
        double sample = 0;
        boolean isInside = shape.contains(start[0], start[1]);

        if(outside){
            isInside = !isInside;
        }

        float[] lastSample = null;

        while (sample < samples) {
            float[] point = pointAlongCubicCurve(start, control1, control2, end, (float) (sample / samples));
            sample += 1;

            // Often our samples will fall within the same pixel, we don't need this level of accuracy, so wait until the distance between samples is greater than the tolerance, we don't use distance calculation because it's slower, and instead check for a big enough change in X or Y
            if(lastSample != null){
                if(Math.abs(lastSample[0] - point[0]) < tolerance && Math.abs(lastSample[1] - point[1]) < tolerance){
                    continue;
                }
            }
            lastSample = point;

            boolean contains = shape.contains(point[0], point[1]);

            if(outside){
                contains = !contains;
            }

            if (isInside != contains) {
                float[] outputLeft = new float[8];
                float[] outputRight = new float[8];
                splitCubicCurve(curve.toFloatArray(), outputLeft, outputRight, (float) (sample / samples));
                GCubicCurve curveLeft = new GCubicCurve(outputLeft);
                GCubicCurve curveRight = new GCubicCurve(outputRight);

                //avoids rare rounding errors when the point exists exactly on the boundary of a shape, which could cause infinite loops
                curveLeft.awtCubicCurve.x2 = point[0];
                curveLeft.awtCubicCurve.y2 = point[1];
                curveRight.awtCubicCurve.x1 = point[0];
                curveRight.awtCubicCurve.y1 = point[1];

                if (isInside) consumer.accept(curveLeft);
                recursiveSplitCubicCurve(shape, curveRight, outside, tolerance, consumer);
                return;
            }
        }
        if (isInside) {
            consumer.accept(curve);
        }
    }

    public static void splitCubicCurve(float[] inputCurve, float[] outputLeft, float[] outputRight, float t) {
        float startX = inputCurve[0];
        float startY = inputCurve[1];
        float ctrlX1 = inputCurve[2];
        float ctrlY1 = inputCurve[3];
        float ctrlX2 = inputCurve[4];
        float ctrlY2 = inputCurve[5];
        float endX = inputCurve[6];
        float endY = inputCurve[7];

        float midPointX = (ctrlX2 - ctrlX1) * t + ctrlX1;
        float midPointY = (ctrlY2 - ctrlY1) * t + ctrlY1;

        //1st control point of the 1st curve
        float ctrl1X = (ctrlX1 - startX) * t + startX;
        float ctrl1Y = (ctrlY1 - startY) * t + startY;

        //2nd control point of the 2nd curve
        float ctrl4X = (endX - ctrlX2) * t + ctrlX2;
        float ctrl4Y = (endY - ctrlY2) * t + ctrlY2;

        //2nd control point of the 1st curve
        float ctrl2X = (midPointX - ctrl1X) * t + ctrl1X;
        float ctrl2Y = (midPointY - ctrl1Y) * t + ctrl1Y;

        //1st control point of the 2nd curve
        float ctrl3X = (ctrl4X - midPointX) * t + midPointX;
        float ctrl3Y = (ctrl4Y - midPointY) * t + midPointY;

        //end point of the first curve / start point of the second curve
        float x2 = (ctrl3X - ctrl2X) * t + ctrl2X;
        float y2 = (ctrl3Y - ctrl2Y) * t + ctrl2Y;

        outputLeft[0] = startX;
        outputLeft[1] = startY;
        outputLeft[2] = ctrl1X;
        outputLeft[3] = ctrl1Y;
        outputLeft[4] = ctrl2X;
        outputLeft[5] = ctrl2Y;
        outputLeft[6] = x2;
        outputLeft[7] = y2;

        outputRight[0] = x2;
        outputRight[1] = y2;
        outputRight[2] = ctrl3X;
        outputRight[3] = ctrl3Y;
        outputRight[4] = ctrl4X;
        outputRight[5] = ctrl4Y;
        outputRight[6] = endX;
        outputRight[7] = endY;
    }


    public static float[] pointAlongLineSegment(float[] start, float[] end, double index){
        if (index <= 0.0){
            return start;
        }
        if (index >= 1.0){
            return end;
        }
        float x = (float) ((end[0] - start[0]) * index + start[0]);
        float y = (float) ((end[1] - start[1]) * index + start[1]);
        return new float[]{x, y};
    }

    public static float[] pointAlongQuadraticCurve(float[] start, float[] control, float[] end, double index){
        if (index <= 0.0){
            return start;
        }
        if (index >= 1.0){
            return end;
        }
        float x = (float) ((1F - index) * (1F - index) * start[0] + 2F * (1F - index) * index * control[0] + index * index * end[0]);
        float y = (float) ((1F - index) * (1 - index) * start[1] + 2 * (1 - index) * index * control[1] + index * index * end[1]);
        return new float[]{x, y};
    }

    public static float[] pointAlongCubicCurve(float[] start, float[] control1, float[] control2, float[] end, double index){
        if (index <= 0.0){
            return start;
        }
        if (index >= 1.0){
            return end;
        }
        float x = (float) (((1 - index) * (1 - index) * (1 - index)) * start[0] + 3 * ((1 - index) * (1 - index)) * index * control1[0] + 3 * (1 - index) * (index * index) * control2[0] + (index * index * index) * end[0]);
        float y = (float) (((1 - index) * (1 - index) * (1 - index)) * start[1] + 3 * ((1 - index) * (1 - index)) * index * control1[1] + 3 * (1 - index) * (index * index) * control2[1] + (index * index * index) * end[1]);
        return new float[]{x, y};
    }

}