package drawingbot.render;

public abstract class AbstractRenderer {

    public boolean imageFiltersDirty = false;
    public boolean drawingAreaDirty = false;

    public boolean markRenderDirty = true;

    public abstract void init();

    public abstract void draw();

    public final void reRender(){
        markRenderDirty = true;
    }

    ///JFX only
    public void forceCanvasUpdate(){

    }

    public void clearProcessRendering(){

    }
}
