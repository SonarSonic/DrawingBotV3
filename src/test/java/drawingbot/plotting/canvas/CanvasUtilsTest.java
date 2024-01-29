package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.EnumRescaleMode;
import drawingbot.utils.UnitsLength;
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

public class CanvasUtilsTest extends TestCase {

    ObservableCanvas reference;

    public void setUp() throws Exception {
        super.setUp();
        reference = new ObservableCanvas();
        reference.useOriginalSizing.set(false);
        reference.croppingMode.set(EnumCroppingMode.SCALE_TO_FIT);
        reference.inputUnits.set(UnitsLength.CENTIMETRES);
        reference.width.set(1000);
        reference.height.set(600);
        reference.drawingAreaGangPadding.set(false);
        reference.drawingAreaPaddingLeft.set(10);
        reference.drawingAreaPaddingRight.set(70);
        reference.drawingAreaPaddingTop.set(50);
        reference.drawingAreaPaddingBottom.set(100);
        reference.rescaleMode.set(EnumRescaleMode.HIGH_QUALITY);
        reference.targetPenWidth.set(0.7F);
    }

    public void testSimpleCanvas(){
        SimpleCanvas canvas = new SimpleCanvas(reference);
        assertTrue(ICanvas.matchingCanvas(canvas, reference));
    }

    public void testOriginalSizedCanvas(){
        SimpleCanvas canvas = new SimpleCanvas(reference);
        canvas.useOriginalSizing = true;
        ImageCanvas imageCanvas = new ImageCanvas(canvas, new SimpleCanvas(800, 400), false);
        assertEquals(imageCanvas.getWidth(), 800F);
        assertEquals(imageCanvas.getHeight(), 400F);
        assertEquals(imageCanvas.getDrawingOffsetX(), 0F);
        assertEquals(imageCanvas.getDrawingOffsetY(), 0F);
    }

    public void testRetargetCanvas(){
        SimpleCanvas canvas = new SimpleCanvas(reference);
        ICanvas inches = CanvasUtils.retargetCanvas(canvas, UnitsLength.INCHES);
        ICanvas revert = CanvasUtils.retargetCanvas(inches, canvas.getUnits());

        assertTrue(ICanvas.matchingCanvas(revert, reference));
    }

}