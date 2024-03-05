package drawingbot.render.overlays;

import drawingbot.javafx.util.IMultipleChangeListener;
import drawingbot.javafx.util.IMultipleEventHandler;
import drawingbot.javafx.util.MultipleEventHandler;
import drawingbot.javafx.util.MultipleListenerHandler;
import drawingbot.render.viewport.Viewport;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * A base class for viewport overlays, add the basic required properties and adds
 */
public abstract class ViewportOverlayBase implements IMultipleChangeListener, IMultipleEventHandler {

    private final ObjectProperty<Viewport> viewport = new SimpleObjectProperty<>();

    public Viewport getViewport() {
        return viewport.get();
    }

    public ObjectProperty<Viewport> viewportProperty() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport.set(viewport);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty enabled = new SimpleBooleanProperty(this,"active", false);

    public boolean getEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    ////////////////////////////////////////////////////////

    private MultipleListenerHandler multipleListenerHandler = null;

    /**
     * The listener handler supplied here is assumed to be used for listeners relating to activation, so they can be easily removed
     */
    @Override
    public MultipleListenerHandler getListenerHandler() {
        if(multipleListenerHandler == null){
            multipleListenerHandler = new MultipleListenerHandler();
        }
        return multipleListenerHandler;
    }

    ////////////////////////////////////////////////////////

    private MultipleEventHandler multipleEventHandler = null;

    /**
     * The listener handler supplied here is assumed to be used for events relating to activation, so they can be easily removed
     */
    @Override
    public MultipleEventHandler getMultipleEventHandler() {
        if(multipleEventHandler == null){
            multipleEventHandler = new MultipleEventHandler();
        }
        return multipleEventHandler;
    }

    ////////////////////////////////////////////////////////

    private boolean init = false;

    public void initOverlay(){
        //NOP
    }

    @MustBeInvokedByOverriders
    public void activateViewportOverlay(Viewport viewport) {
        setViewport(viewport);
        if(!init){
            initOverlay();
            init = true;
        }
    }

    @MustBeInvokedByOverriders
    public void deactivateViewportOverlay(Viewport viewport) {
        setViewport(null);
        if(multipleListenerHandler != null){
            multipleListenerHandler.unregisterAll();
        }
        if(multipleEventHandler != null){
            multipleEventHandler.removeAll();
        }
    }

    public void onRenderTick() {
        //NOP
    }

    public abstract String getName();
}
