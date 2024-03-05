package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.render.renderer.RendererFactory;
import drawingbot.render.viewport.Viewport;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class DisplayModeBase {

    public abstract String getName();
    /**
     * @return the {@link RendererFactory} to provide the {@link drawingbot.render.renderer.RendererBase} used to render this {@link DisplayModeBase}
     */
    public abstract RendererFactory getRendererFactory();

    public void activateDisplayMode(Viewport viewport){
        //NOP
    }

    public void deactivateDisplayMode(Viewport viewport){
        //NOP
    }

    /**
     * @return if this {@link DisplayModeBase} should be hidden in menus
     */
    public boolean isHidden(){
        return false;
    }

    private final BooleanProperty active = new SimpleBooleanProperty(false);

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    ////////////////////////////////////////////////////////

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

    private final ObjectProperty<ICanvas> canvas = new SimpleObjectProperty<>();

    public ICanvas getCanvas() {
        return canvas.get();
    }

    public ObjectProperty<ICanvas> canvasProperty() {
        return canvas;
    }

    public void setCanvas(ICanvas canvas) {
        this.canvas.set(canvas);
    }

    public ICanvas getDefaultCanvas(){
        return DrawingBotV3.project().getDrawingArea();
    }

    ////////////////////////////////////////////////////////

    @Override
    public String toString(){
        return getName();
    }
}
