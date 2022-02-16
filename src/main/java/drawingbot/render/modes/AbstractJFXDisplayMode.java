package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.render.IDisplayMode;
import drawingbot.render.IRenderer;
import drawingbot.render.jfx.JavaFXRenderer;

public abstract class AbstractJFXDisplayMode implements IDisplayMode {

    @Override
    public void applySettings() {
        DrawingBotV3.RENDERER.displayMode = this;
    }

    @Override
    public void resetSettings() {
        //NOP
    }

    @Override
    public IRenderer getRenderer() {
        return DrawingBotV3.RENDERER;
    }

    public abstract void preRender(JavaFXRenderer jfr);

    public abstract void doRender(JavaFXRenderer jfr);

    public void postRender(JavaFXRenderer jfr){}

    @Override
    public String toString(){
        return getName();
    }
}
