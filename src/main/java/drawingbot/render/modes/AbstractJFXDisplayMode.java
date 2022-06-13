package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.render.IDisplayMode;
import drawingbot.render.IRenderer;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;

public abstract class AbstractJFXDisplayMode implements IDisplayMode {

    public final FlagStates renderFlags = new FlagStates(Flags.RENDER_CATEGORY);

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

    public void preRender(JavaFXRenderer jfr){

    }

    public void doRender(JavaFXRenderer jfr){

    }

    public void postRender(JavaFXRenderer jfr){

    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public FlagStates getRenderFlags() {
        return renderFlags;
    }
}
