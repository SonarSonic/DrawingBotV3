package drawingbot.render.renderer;

import drawingbot.render.renderer.RendererBase;

import java.util.function.Supplier;

public class RendererFactory {

    public String type;
    public Supplier<RendererBase> factory;

    public RendererFactory(String type, Supplier<RendererBase> factory){
        this.type = type;
        this.factory = factory;
    }

    public String getRendererType(){
        return type;
    }

    public RendererBase create(){
        return factory.get();
    }

}
