package drawingbot.javafx.observables;

import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.ColorSeparationSettings;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.List;

public class ObservableDrawingSetListenerTest extends TestCase implements ObservableDrawingSet.Listener {

    public ObservableDrawingSet drawingSet;
    private Observable lastDrawingSetPropertyChange = null;
    private Observable lastDrawingPenPropertyChange = null;
    private ColorSeparationHandler receivedColourHandlerChange = null;
    private ObservableDrawingPen addedDrawingPen = null;
    private ObservableDrawingPen removedDrawingPen = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DrawingSet set = new DrawingSet("Unit Test", "RGB", List.of(
            new DrawingPen("Test", "Red", ImageTools.getARGBFromColor(Color.RED)),
            new DrawingPen("Test", "Green", ImageTools.getARGBFromColor(Color.GREEN)),
            new DrawingPen("Test", "Blue", ImageTools.getARGBFromColor(Color.BLUE))
        ));
        drawingSet = new ObservableDrawingSet(set);
        drawingSet.listeners().add(this);
    }

    public void testType(){
        receiveChange(drawingSet.type, drawingSet.getType() + " Test");
    }

    public void testName(){
        receiveChange(drawingSet.name, drawingSet.getName() + " Test");
    }

    public void testDistributionType(){
        receiveChange(drawingSet.distributionType, EnumDistributionType.LUMINANCE_WEIGHTED);
    }

    public void testDistributionOrder(){
        receiveChange(drawingSet.distributionOrder, EnumDistributionOrder.REVERSED);
    }

    public void testColourSeparationHandler(){
        receiveChange(drawingSet.colorHandler, new ColorSeparationHandler("TEST"));
    }

    public void testColourSeparationHandlerEvent(){
        ColorSeparationHandler handler = new ColorSeparationHandler("TEST");
        drawingSet.colorHandler.set(handler);
        Assert.assertEquals(receivedColourHandlerChange, handler);
    }

    public void testColourSeparationSettings(){
        receiveChange(drawingSet.colorSettings, new ColorSeparationSettings() {
            @Override
            public ColorSeparationSettings copy() {
                return this;
            }
        });
    }

    public void testInternalPenChange(){
        ObservableDrawingPen drawingPen = drawingSet.getPen(0);
        drawingPen.name.set(drawingPen.getName() + " Test");
        Assert.assertEquals(lastDrawingPenPropertyChange, drawingPen.name);
    }

    public void testAddPen(){
        ObservableDrawingPen pen = drawingSet.addNewPen(new DrawingPen("Test", "Yellow", ImageTools.getARGBFromColor(Color.YELLOW)), true);
        Assert.assertEquals(addedDrawingPen, pen);
    }

    public void testRemovePen(){
        ObservableDrawingPen pen = drawingSet.getPen(0);
        drawingSet.pens.remove(pen);
        Assert.assertEquals(removedDrawingPen, pen);
    }

    public <T> void receiveChange(Property<T> observable, T newValue){
        lastDrawingSetPropertyChange = null;
        observable.setValue(newValue);
        Assert.assertEquals(observable, lastDrawingSetPropertyChange);
    }

    @Override
    public void onDrawingPenPropertyChanged(ObservableDrawingPen pen, Observable property) {
        lastDrawingPenPropertyChange = property;
    }

    @Override
    public void onDrawingSetPropertyChanged(ObservableDrawingSet set, Observable property) {
        lastDrawingSetPropertyChange = property;
    }

    @Override
    public void onColourSeparatorChanged(ObservableDrawingSet set, ColorSeparationHandler oldValue, ColorSeparationHandler newValue) {
        receivedColourHandlerChange = newValue;
    }

    @Override
    public void onDrawingPenAdded(ObservableDrawingPen pen) {
        addedDrawingPen = pen;
    }

    @Override
    public void onDrawingPenRemoved(ObservableDrawingPen pen) {
        removedDrawingPen = pen;
    }
}
