package drawingbot.javafx.util;

import drawingbot.plotting.canvas.ObservableCanvas;
import javafx.beans.property.Property;
import javafx.scene.paint.Color;
import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicBoolean;

public class PropertyUtilTest extends TestCase {

    public ObservableCanvas canvas;

    public void setUp() throws Exception {
        super.setUp();
        canvas = new ObservableCanvas();
    }

    public void testBooleanListener(){
        AtomicBoolean received = new AtomicBoolean(false);
        Property<?> target = canvas.optimiseForPrint;
        PropertyListListener<ObservableCanvas> listener = PropertyUtil.addPropertyListListener(canvas, (value, changed) -> {
            if(changed.contains(target)){
                received.set(true);
            }
        });
        canvas.optimiseForPrint.set(!canvas.optimiseForPrint.get());
        PropertyUtil.removePropertyListListener(canvas, listener);
        assertTrue(received.get());
    }

    public void testFloatListener(){
        AtomicBoolean received = new AtomicBoolean(false);
        Property<?> target = canvas.height;
        PropertyListListener<ObservableCanvas> listener = PropertyUtil.addPropertyListListener(canvas, (value, changed) -> {
            if(changed.contains(target)){
                received.set(true);
            }
        });
        canvas.height.set(1500);
        PropertyUtil.removePropertyListListener(canvas, listener);
        assertTrue(received.get());
    }

    public void testObjectListener(){
        AtomicBoolean received = new AtomicBoolean(false);
        Property<?> target = canvas.canvasColor;
        PropertyListListener<ObservableCanvas> listener = PropertyUtil.addPropertyListListener(canvas, (value, changed) -> {
            if(changed.contains(target)){
                received.set(true);
            }
        });
        canvas.canvasColor.set(Color.ALICEBLUE);
        PropertyUtil.removePropertyListListener(canvas, listener);
        assertTrue(received.get());
    }
}