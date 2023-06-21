package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.JavaFxJUnit4ClassRunner;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.registry.MasterRegistry;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(JavaFxJUnit4ClassRunner.class)
public class GeometryIteratorTests {

    PlottedDrawing drawing;

    @Before
    public void before() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), new File("images/testimage.jpg"), true, false);
            DrawingBotV3.project().openImage.addListener((observable, oldValue, newValue) -> latch.countDown());
        });
        latch.await();

        final CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            DrawingBotV3.project().getPFMSettings().setPFMFactory(MasterRegistry.INSTANCE.getDefaultPFM());
            DrawingBotV3.INSTANCE.startPlotting(DrawingBotV3.context());

            DrawingBotV3.INSTANCE.taskMonitor.processingCount.addListener((observable, oldValue, newValue) -> {
                if(newValue.intValue() == 0){ //when the value changes we add export tasks for every type
                    latch2.countDown();
                }
            });
        });
        latch2.await();
        drawing = DrawingBotV3.taskManager().getCurrentDrawing();
    }

    @Test
    public void testAsyncIterator(){
        AsynchronousGeometryIterator geometryIterator = new AsynchronousGeometryIterator(drawing);
        List<IGeometry> unseen = new ArrayList<>(drawing.geometries);
        while (geometryIterator.hasNext()){
            IGeometry geometry = geometryIterator.next();
            unseen.remove(geometry);
        }
        Assert.assertEquals(unseen.size(), 0);
    }

    @Test
    public void testDrawingIterator(){
        PlottedDrawing copy = drawing.copy();
        DrawingGeometryIterator geometryIterator = new DrawingGeometryIterator(copy);
        List<IGeometry> unseen = new ArrayList<>(copy.geometries);
        while (geometryIterator.hasNext()){
            IGeometry geometry = geometryIterator.next();
            unseen.remove(geometry);
        }
        Assert.assertEquals(unseen.size(), 0);
    }

    @Test
    public void testVertexPathIterator(){
        PlottedDrawing copy = drawing.copy();
        DrawingVertexPathIterator geometryIterator = new DrawingVertexPathIterator(copy, copy.getGlobalRenderOrder(), null);
        List<IGeometry> unseen = new ArrayList<>(copy.geometries);
        while(!geometryIterator.isDone()){
            unseen.remove(geometryIterator.currentGeometry);
            geometryIterator.next();
        }
        Assert.assertEquals(unseen.size(), 0);
    }

}