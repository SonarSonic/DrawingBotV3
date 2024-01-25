package drawingbot.test;

import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyChangeListener;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.utils.*;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/*
public class PropertyUtilTest extends TestCase {

    public ObservableCanvas canvas;

    public void setUp() throws Exception {
        super.setUp();
        canvas = new ObservableCanvas();
    }

    private final AtomicReference<Observable> current = new AtomicReference<>();
    private final AtomicBoolean received = new AtomicBoolean(false);

    private void setupGenericObservableTest(IProperties properties){

        PropertyUtil.createPropertyChangeListener(properties, (value, changed) -> {
            if (changed.contains(current.get())) {
                received.set(true);
            }
        });

    }

    private <O extends Observable> void testObservable(String name, O observable, Consumer<O> test){
        if(observable instanceof Property){
            Property<?> prop = (Property<?>) observable;
            if(prop.isBound()){
                return;
            }
        }
        current.set(observable);
        received.set(false);
        test.accept(observable);
        assertTrue("Observable Test Failed "  + name + " " + observable.toString(), received.get());
    }

    private <O extends Observable> void testObservable(String name, O observable) {
        if(observable instanceof BooleanProperty){
            testBooleanObservable(name, (BooleanProperty)observable);
        }else if(observable instanceof IntegerProperty){
            testIntegerObservable(name, (IntegerProperty)observable);
        }else if(observable instanceof DoubleProperty){
            testDoubleObservable(name, (DoubleProperty)observable);
        }else if(observable instanceof LongProperty){
            testLongObservable(name, (LongProperty)observable);
        }else if(observable instanceof FloatProperty){
            testFloatObservable(name, (FloatProperty)observable);
        }
    }

    private <O extends BooleanProperty> void testBooleanObservable(String name, O observable){
        testObservable(name, observable, o -> o.set(!observable.get()));
    }

    private <O extends IntegerProperty> void testIntegerObservable(String name, O observable){
        testObservable(name, observable, o -> o.set(observable.get()+1));
    }

    private <O extends DoubleProperty> void testDoubleObservable(String name, O observable){
        testObservable(name, observable, o -> o.set(observable.get()+1));
    }

    private <O extends LongProperty> void testLongObservable(String name, O observable){
        testObservable(name, observable, o -> o.set(observable.get()+1));
    }

    private <O extends FloatProperty> void testFloatObservable(String name, O observable){
        testObservable(name, observable, o -> o.set(observable.get()+1));
    }

    private <E extends Enum<E>> void testEnumObservable(String name, ObjectProperty<E> observable, E[] values){
        testObservable(name, observable, o -> {
            E oldValue = o.get();
            int next = oldValue.ordinal() + 1;
            E newValue = values[next < values.length ? next : 0];
            o.set(newValue);
        });
    }


    public void testCanvasListeners(){
        ObservableCanvas observableCanvas = new ObservableCanvas();
        setupGenericObservableTest(observableCanvas);
        testObservable("useOriginalSizing", observableCanvas.useOriginalSizing);
        testEnumObservable("croppingMode", observableCanvas.croppingMode, EnumCroppingMode.values());
        testEnumObservable("clippingMode", observableCanvas.clippingMode, EnumClippingMode.values());
        testEnumObservable("inputUnits", observableCanvas.inputUnits, UnitsLength.values());
        testObservable("width", observableCanvas.width);
        testObservable("height", observableCanvas.height);
        testObservable("drawingAreaPaddingLeft", observableCanvas.drawingAreaPaddingLeft);
        testObservable("drawingAreaPaddingRight", observableCanvas.drawingAreaPaddingRight);
        testObservable("drawingAreaPaddingTop", observableCanvas.drawingAreaPaddingTop);
        testObservable("drawingAreaPaddingBottom", observableCanvas.drawingAreaPaddingBottom);
        //testObservable("drawingAreaPaddingGangedValue", observableCanvas.drawingAreaPaddingGangedValue);
        testObservable("drawingAreaGangPadding", observableCanvas.drawingAreaGangPadding);
        testEnumObservable("orientation", observableCanvas.orientation, EnumOrientation.values());
        testObservable("targetPenWidth", observableCanvas.targetPenWidth);
        testEnumObservable("rescaleMode", observableCanvas.rescaleMode, EnumRescaleMode.values());
        testEnumObservable("rescaleMode", observableCanvas.rescaleMode, EnumRescaleMode.values());
        testObservable("canvasColor", observableCanvas.canvasColor, o -> {
            o.set(Color.WHITE);
            o.set(Color.BLACK);
        });
        testObservable("backgroundColor", observableCanvas.backgroundColor, o -> {
            o.set(Color.WHITE);
            o.set(Color.BLACK);
        });
    }


    public void testBooleanListener(){
        AtomicBoolean received = new AtomicBoolean(false);
        Property<?> target = canvas.useOriginalSizing;
        PropertyChangeListener<ObservableCanvas> listener = PropertyUtil.createPropertyChangeListener(canvas, (value, changed) -> {
            if(changed.contains(target)){
                received.set(true);
            }
        });
        canvas.useOriginalSizing.set(!canvas.useOriginalSizing.get());
        assertTrue(received.get());
    }

    public void testFloatListener(){
        AtomicBoolean received = new AtomicBoolean(false);
        Property<?> target = canvas.height;
        PropertyChangeListener<ObservableCanvas> listener = PropertyUtil.createPropertyChangeListener(canvas, (value, changed) -> {
            if(changed.contains(target)){
                received.set(true);
            }
        });
        canvas.height.set(1500);
        assertTrue(received.get());
    }

    public void testObjectListener(){
        AtomicBoolean received = new AtomicBoolean(false);
        Property<?> target = canvas.canvasColor;
        PropertyChangeListener<ObservableCanvas> listener = PropertyUtil.createPropertyChangeListener(canvas, (value, changed) -> {
            if(changed.contains(target)){
                received.set(true);
            }
        });
        canvas.canvasColor.set(Color.ALICEBLUE);
        assertTrue(received.get());
    }
}

 */