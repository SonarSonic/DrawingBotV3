package drawingbot.javafx.observables;

import drawingbot.drawing.DrawingPen;
import drawingbot.image.ImageTools;
import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import junit.framework.TestCase;
import org.junit.Assert;

import java.awt.*;

public class ObservableDrawingPenListenerTest extends TestCase implements ObservableDrawingPen.Listener {

    public ObservableDrawingPen drawingPen;
    private Observable lastChange = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        drawingPen = new ObservableDrawingPen(1, new DrawingPen("Test", "Red", ImageTools.getARGBFromAWTColor(Color.RED)));
        drawingPen.listeners().add(this);
    }

    public void testPenNumber(){
        receiveChange(drawingPen.penNumber, drawingPen.getPenNumber()+1);
    }

    public void testEnable(){
        receiveChange(drawingPen.enable, !drawingPen.isEnabled());
    }

    public void testType(){
        receiveChange(drawingPen.type, "False");
    }

    public void testName(){
        receiveChange(drawingPen.name, "Blue");
    }

    public void testJavaFXColour(){
        receiveChange(drawingPen.javaFXColour, javafx.scene.paint.Color.AQUA);
    }

    public void testDistributionWeight(){
        receiveChange(drawingPen.distributionWeight, drawingPen.getDistributionWeight()+1);
    }

    public void testStrokeSize(){
        receiveChange(drawingPen.strokeSize, drawingPen.getStrokeSize()+1);
    }

    public void testForceOverlap(){
        receiveChange(drawingPen.forceOverlap, !drawingPen.shouldForceOverlap());
    }

    public void testUseColorSplitOpacity(){
        receiveChange(drawingPen.useColorSplitOpacity, !drawingPen.useColorSplitOpacity.get());
    }

    public void testColorSplitMultiplier(){
        receiveChange(drawingPen.colorSplitMultiplier, drawingPen.getColorSplitMultiplier()+1);
    }

    public void testColorSplitOpacity(){
        receiveChange(drawingPen.colorSplitOpacity, drawingPen.getColorSplitOpacity()+1);
    }

    public void testColorSplitOffsetX(){
        receiveChange(drawingPen.colorSplitOffsetX, drawingPen.getColorSplitOffsetX()+1);
    }

    public void testColorSplitOffsetY(){
        receiveChange(drawingPen.colorSplitOffsetY, drawingPen.getColorSplitOffsetY()+1);
    }

    public <T> void receiveChange(Property<T> observable, T newValue){
        lastChange = null;
        observable.setValue(newValue);
        Assert.assertEquals(observable, lastChange);
    }

    @Override
    public void onDrawingPenPropertyChanged(ObservableDrawingPen pen, Observable property) {
        this.lastChange = property;
    }
}
