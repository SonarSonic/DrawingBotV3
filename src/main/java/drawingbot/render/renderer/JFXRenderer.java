package drawingbot.render.renderer;

import drawingbot.DrawingBotV3;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.render.RenderUtils;
import drawingbot.render.modes.IJFXDisplayMode;
import javafx.beans.binding.Bindings;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;

public class JFXRenderer extends RendererBase {

    public static RendererFactory JFX_RENDERER_FACTORY = new RendererFactory("JFX", JFXRenderer::new);

    ///

    public Pane pane;
    public Canvas jfxCanvas;
    public GraphicsContext graphicsFX;

    ///

    ////////////////////////////////////////////////////////


    public void initRenderer(){
        jfxCanvas = new Canvas(500, 500);
        jfxCanvas.scaleXProperty().bind(getViewport().scaleToFitProperty());
        jfxCanvas.scaleYProperty().bind(getViewport().scaleToFitProperty());
        jfxCanvas.widthProperty().bind(getViewport().displayedWidthProperty());
        jfxCanvas.heightProperty().bind(getViewport().displayedHeightProperty());
        jfxCanvas.widthProperty().addListener(observable -> clearCanvas());
        jfxCanvas.heightProperty().addListener(observable -> clearCanvas());

        pane = new StackPane(jfxCanvas);
        pane.minWidthProperty().bind(Bindings.max(getViewport().displayedWidthProperty(), getViewport().displayedHeightProperty()));
        pane.minHeightProperty().bind(pane.minWidthProperty());

        graphicsFX = jfxCanvas.getGraphicsContext2D();
        graphicsFX.setImageSmoothing(false);
        graphicsFX.setLineCap(StrokeLineCap.ROUND);
        graphicsFX.setLineJoin(StrokeLineJoin.ROUND);

        renderScale.bind(Bindings.createDoubleBinding(this::calculateRenderScale, getViewport().canvasScaledWidthProperty(), getViewport().canvasScaledHeightProperty()));

        final Affine toScene = new Affine();
        rendererToSceneTransformProperty().bind(Bindings.createObjectBinding(() -> {
            toScene.setToTransform(jfxCanvas.getLocalToSceneTransform());
            toScene.appendScale(getRenderScale(), getRenderScale());
            return toScene;
        }, jfxCanvas.localToSceneTransformProperty(), renderScaleProperty()));

        final Affine fromScene = new Affine();
        sceneToRendererTransformProperty().bind(Bindings.createObjectBinding(() -> {
            if(getRendererToSceneTransform().determinant() != 0){
                fromScene.setToTransform(getRendererToSceneTransform().createInverse());
            }else{
                fromScene.setToIdentity();
            }
            return fromScene;
        }, rendererToSceneTransformProperty()));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public double calculateRenderScale(){
        return calculateRenderScale(getViewport(), RenderUtils.defaultMinTextureSize, RenderUtils.defaultMaxTextureSize);
    }

    public int getVertexRenderLimit(){
        return getViewport().getRendererBlendMode() != EnumBlendMode.NORMAL ? RenderUtils.vertexRenderLimitBlendMode : RenderUtils.vertexRenderLimitNormal;
    }

    public int getVertexRenderTimeOut(){
        return getViewport().getRendererBlendMode() != EnumBlendMode.NORMAL ? RenderUtils.vertexRenderTimeOutBlendMode : RenderUtils.vertexRenderTimeOutNormal;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// RENDERING

    @Override
    public void doRender() {
        //If the renderer is invisible, don't render anything
        if(pane.getParent() == null){
            return;
        }

        if(displayMode.get() instanceof IJFXDisplayMode jfxDisplayMode){

            //Update the state of the display mode
            jfxDisplayMode.onRenderTick(this);

            //Re-Render the canvas if the Display Mode is dirty
            if(jfxDisplayMode.isRenderDirty(this)){
                graphicsFX.setGlobalBlendMode(BlendMode.SRC_OVER);
                graphicsFX.setTransform(1, 0, 0, 1, 0, 0);
                graphicsFX.save();
                jfxDisplayMode.preRender(this);
                jfxDisplayMode.doRender(this);
                jfxDisplayMode.postRender(this);
                graphicsFX.restore();
            }
        }

    }

    public void clearCanvas(){
        clearCanvas(DrawingBotV3.project().drawingArea.get().canvasColor.getValue());
    }

    public void clearCanvas(Color color){
        jfxCanvas.getGraphicsContext2D().clearRect(0, 0, jfxCanvas.getWidth()+1, jfxCanvas.getHeight()+1); //ensures the canva's buffer is always cleared, some blend modes will prevent fillRect from triggering this
        jfxCanvas.getGraphicsContext2D().setFill(color);
        jfxCanvas.getGraphicsContext2D().fillRect(0, 0, jfxCanvas.getWidth()+1, jfxCanvas.getHeight()+1);
    }

    @Override
    public void activateRenderer() {
        getViewport().setRendererNode(pane);
    }

    @Override
    public void deactivateRenderer() {
        getViewport().setRendererNode(null);
    }

    @Override
    public boolean isJavaFXRenderer() {
        return true;
    }

    @Override
    public boolean isOpenGLRenderer() {
        return false;
    }
}
