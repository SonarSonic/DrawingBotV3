package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.JavaFxJUnit4ClassRunner;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.operation.GeometryOperationOptimize;
import drawingbot.geom.operation.GeometryOperationSimplify;
import drawingbot.geom.operation.GeometryOperationUnsimplify;
import drawingbot.geom.shapes.*;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.plotting.canvas.SimpleCanvas;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JavaFxJUnit4ClassRunner.class)
public class PlottedDrawingTests {

    PlottedDrawing toCopy;
    PlottedDrawing toSimplify;

    @Before
    public void createDrawing() {
        SimpleCanvas canvas = new SimpleCanvas(1000, 1000);
        toCopy = new PlottedDrawing(canvas, DrawingBotV3.INSTANCE.drawingSets);
        toCopy.newPlottedGroup(DrawingBotV3.INSTANCE.drawingSets.activeDrawingSet.get(), null);
        toCopy.addGeometry(new GCubicCurve(50, 50, 100, 100, 100, 100, 50, 50));
        toCopy.addGeometry(new GLine(0, 0, 1000, 1000));
        toCopy.addGeometry(new GEllipse(50, 50, 100, 100));
        toCopy.addGeometry(new GRectangle(50, 50, 100, 100));

        toSimplify = toCopy.copyBase();
        toSimplify.addGeometry(new GLine(0, 0, 10, 10));
        toSimplify.addGeometry(new GLine(10, 10, 20, 20));
        toSimplify.addGeometry(new GLine(20, 20, 30, 30));
        toSimplify.addGeometry(new GCubicCurve(30, 30, 35, 35, 35, 35, 40, 40));
        toSimplify.addGeometry(new GQuadCurve(40, 40, 45, 45, 50, 50));

        GPath path = new GPath();
        path.moveTo(50, 50);
        path.lineTo(60, 60);
        path.curveTo(65, 65, 65, 65, 70, 70);
        path.closePath();
        toSimplify.addGeometry(path);

    }

    @Test
    public void testCopySize() {
        PlottedDrawing drawing = toCopy.copy();
        Assert.assertEquals(drawing.geometries.size(), toCopy.geometries.size());
        Assert.assertEquals(drawing.groups.size(), toCopy.groups.size());
        Assert.assertEquals(drawing.metadataMap.size(), toCopy.metadataMap.size());
        Assert.assertEquals(drawing.vertexCount, toCopy.vertexCount);
        Assert.assertEquals(drawing.displayedShapeMin, toCopy.displayedShapeMin);
        Assert.assertEquals(drawing.displayedShapeMax, toCopy.displayedShapeMax);
        Assert.assertEquals(drawing.ignoreWeightedDistribution, toCopy.ignoreWeightedDistribution);
    }

    @Test
    public void testCopyGeometry() {
        PlottedDrawing drawing = toCopy.copy();
        for(int i = 0; i < drawing.geometries.size(); i++){
            IGeometry refGeometry = toCopy.geometries.get(i);
            IGeometry copyGeometry = drawing.geometries.get(i);

            Assert.assertEquals(refGeometry.getGeometryIndex(), copyGeometry.getGeometryIndex());
            Assert.assertEquals(refGeometry.getPenIndex(), copyGeometry.getPenIndex());
            Assert.assertEquals(refGeometry.getSampledRGBA(), copyGeometry.getSampledRGBA());
            Assert.assertEquals(refGeometry.getGroupID(), copyGeometry.getGroupID());
            Assert.assertEquals(refGeometry.serializeData(), copyGeometry.serializeData());
        }
    }

    @Test
    public void testOptimiseGeometry() {
        PlottedDrawing drawing = toSimplify.copy();
        GeometryOperationOptimize optimize = new GeometryOperationOptimize(CanvasUtils.createCanvasScaleTransform(drawing.getCanvas()));
        PlottedDrawing outputDrawing = optimize.run(drawing);
        Assert.assertEquals(outputDrawing.geometries.size(), 1);
    }

    @Test
    public void testSimplifyGeometry() {
        PlottedDrawing drawing = toSimplify.copy();
        GeometryOperationSimplify simplify = new GeometryOperationSimplify(IGeometryFilter.BYPASS_FILTER, false, false);
        PlottedDrawing outputDrawing = simplify.run(drawing);
        Assert.assertEquals(outputDrawing.geometries.size(), 1);
    }

    @Test
    public void testUnsimplifyGeometry() {
        PlottedDrawing drawing = toSimplify.copy();
        GeometryOperationSimplify simplify = new GeometryOperationSimplify(IGeometryFilter.BYPASS_FILTER, false, false);
        GeometryOperationUnsimplify unsimplify = new GeometryOperationUnsimplify();
        PlottedDrawing outputDrawing = simplify.run(drawing);
        outputDrawing = unsimplify.run(outputDrawing);

        Assert.assertEquals(outputDrawing.geometries.size(), 8);
    }

    @Test
    public void testSerializeCoords() {
        float[] coordsA = new float[]{0.12345F, 1.2345F, 12.345F, 123.45F, 1234.5F, 12345F};
        float[] coordsB = GeometryUtils.deserializeCoords(GeometryUtils.serializeCoords(coordsA));
        Assert.assertArrayEquals(coordsA, coordsB, 0.001F);
    }
}