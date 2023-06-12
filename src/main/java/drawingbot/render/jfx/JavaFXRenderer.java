package drawingbot.render.jfx;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.image.ImageFilteringTask;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.render.IRenderer;
import drawingbot.render.modes.AbstractJFXDisplayMode;
import drawingbot.utils.flags.Flags;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.jfree.fx.FXGraphics2D;

public class JavaFXRenderer implements IRenderer {

    public final Rectangle2D screenBounds;
    public AbstractJFXDisplayMode displayMode;

    ///

    public static int vertexRenderLimitNormal = 0; //20000;
    public static int vertexRenderLimitBlendMode = 5000;

    public static int vertexRenderTimeOutNormal = (int)((1000F/60F)/4F);
    public static int vertexRenderTimeOutBlendMode = 0;

    public static int defaultMinTextureSize = 2048;
    public static int defaultMaxTextureSize = 4096;

    public double canvasScaling = 1F;

    ///

    public Pane pane;
    public Canvas canvas;
    public GraphicsContext graphicsFX;
    public FXGraphics2D graphicsAWT;

    ///

    public ImageFilteringTask filteringTask;

    public JavaFXRenderer(Rectangle2D screenBounds) {
        this.screenBounds = screenBounds;
    }


    public void init(){
        canvas = new Canvas(500, 500);
        graphicsFX = canvas.getGraphicsContext2D();
        graphicsAWT = new FXGraphics2D(canvas.getGraphicsContext2D());

        pane = new Pane();
        pane.getChildren().add(canvas);
        updateCanvasPosition();

        DrawingBotV3.INSTANCE.controller.viewportScrollPane.init(pane);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// RENDERING

    public void clear() {
        clearCanvas(getCurrentBackground());
    }

    ///

    @Override
    public void updateCanvasPosition(){

        pane.setMinWidth(getPaneScaledWidth());
        pane.setMinHeight(getPaneScaledHeight());

        pane.setMaxWidth(getPaneScaledWidth());
        pane.setMaxHeight(getPaneScaledHeight());

        pane.setPrefWidth(getPaneScaledWidth());
        pane.setPrefHeight(getPaneScaledHeight());


        double offsetX = pane.getWidth()/2 - canvas.getWidth()/2;
        double offsetY = pane.getHeight()/2 - canvas.getHeight()/2;
        canvas.relocate(offsetX, offsetY);
    }

    @Override
    public void preRender(){
        //reset canvas scaling
        graphicsFX.setTransform(1, 0, 0, 1, 0, 0);

        displayMode.preRender(this);

        //updateCanvasScaling();
        graphicsFX.setImageSmoothing(false);
        graphicsFX.setLineCap(StrokeLineCap.ROUND);
        graphicsFX.setLineJoin(StrokeLineJoin.ROUND);
        graphicsFX.setGlobalBlendMode(BlendMode.SRC_OVER);
        graphicsFX.save();
    }

    @Override
    public void doRender() {
        displayMode.doRender(this);
        //TODO GRID RENDERER
    }

    @Override
    public void postRender(){
        displayMode.postRender(this);
        graphicsFX.restore();
        displayMode.getRenderFlags().applyMarkedChanges();

        //update the canvas position after it has been resized
        updateCanvasPosition();
    }

    public void clearCanvas(){
        clearCanvas(DrawingBotV3.project().drawingArea.get().canvasColor.getValue());
    }

    public void clearCanvas(Color color){
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth()+1, canvas.getHeight()+1); //ensures the canva's buffer is always cleared, some blend modes will prevent fillRect from triggering this
        canvas.getGraphicsContext2D().setFill(color);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth()+1, canvas.getHeight()+1);
    }

    private double refWidth = -1, refHeight = -1;
    private ICanvas refCanvas = new SimpleCanvas(0, 0);

    @Override
    public ICanvas getRefCanvas(){
        return refCanvas;
    }

    public void setupCanvasSize(ICanvas canvas){
        if(canvas != null){
            setupCanvasSize((int)canvas.getScaledWidth(), (int)canvas.getScaledHeight());
            refCanvas = canvas;
        }
    }

    public double[] calculateCanvasSize(ICanvas canvas){
        return calculateCanvasSize((int)canvas.getScaledWidth(), (int)canvas.getScaledHeight());
    }

    public double[] calculateCanvasSize(double width, double height){
        double[] size = new double[]{width, height, 1};

        if(width > getMaxTextureSize() || height > getMaxTextureSize()){
            double max = Math.max(width, height);
            size[2] = getMaxTextureSize() / max;
            size[0] = Math.floor(width*size[2]);
            size[1] = Math.floor(height*size[2]);
        }else if(width < getMinTextureSize() || height < getMinTextureSize()){
            double max = Math.max(width, height);
            double newScaling = getMinTextureSize() / max;
            double newWidth = Math.floor(width*newScaling);
            double newHeight = Math.floor(height*newScaling);
            if(newWidth > width && newHeight > height){ //sanity check, prevents scaling down images where one side is under and one is over the limit
                size[2] = newScaling;
                size[0] = newWidth;
                size[1] = newHeight;
            }
        }
        return size;
    }

    public void setupCanvasSize(double width, double height){
        if(refWidth == width && refHeight == height){
            return;
        }
        refWidth = width;
        refHeight = height;

        double[] size = calculateCanvasSize(width, height);

        if(canvas.getWidth() == size[0] && canvas.getHeight() == size[1] && canvasScaling == size[2]){
            return;
        }

        canvas.widthProperty().setValue(size[0]);
        canvas.heightProperty().setValue(size[1]);
        canvasScaling = size[2];

        updateCanvasScaling();

        updateCanvasPosition();
        DrawingBotV3.project().resetView();
        clearCanvas();//wipe the canvas
        DrawingBotV3.project().setRenderFlag(Flags.FORCE_REDRAW, true);
    }

    public void updateCanvasScaling(){
        if(DrawingBotV3.project().dpiScaling.get()){
            canvas.setScaleX(1);
            canvas.setScaleY(1);
        }else{
            double screen_scale_x = DrawingBotV3.INSTANCE.controller.viewportScrollPane.getWidth() / ((float) canvas.getWidth());
            double screen_scale_y = DrawingBotV3.INSTANCE.controller.viewportScrollPane.getHeight() / ((float) canvas.getHeight());
            double screen_scale = Math.min(screen_scale_x, screen_scale_y);
            if(canvas.getScaleX() != screen_scale){
                canvas.setScaleX(screen_scale);
                canvas.setScaleY(screen_scale);
            }
        }
    }

    public int getMinTextureSize(){
        return defaultMinTextureSize;
    }

    public int getMaxTextureSize(){
        if(DBPreferences.INSTANCE.maxTextureSize.get() != -1){
            return DBPreferences.INSTANCE.maxTextureSize.get();
        }
        return defaultMaxTextureSize;
    }

    public int getRenderTimeout(){
        return graphicsFX.getGlobalBlendMode() == BlendMode.SRC_OVER ? vertexRenderTimeOutNormal : vertexRenderTimeOutBlendMode;
    }

    public int getVertexRenderLimit(){
        return graphicsFX.getGlobalBlendMode() == BlendMode.SRC_OVER ? vertexRenderLimitNormal : vertexRenderLimitBlendMode;
    }

    //// IRENDERER \\\\

    @Override
    public Rectangle2D getScreenBounds() {
        return screenBounds;
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public Point2D sceneToRenderer(Point2D point2D) {
        Point2D dst = canvas.sceneToLocal(point2D);
        if(dst == null){
            return new Point2D(0, 0);
        }
        return new Point2D(dst.getX()/canvasScaling, dst.getY()/ canvasScaling);
    }

    @Override
    public Point2D rendererToScene(Point2D point2D) {
        Point2D dst = new Point2D(point2D.getX()*canvasScaling, point2D.getY() * canvasScaling);
        dst = canvas.localToScene(dst);
        return dst;
    }

    @Override
    public void switchToRenderer() {
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.init(DrawingBotV3.RENDERER.pane);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.layout();
    }

    @Override
    public double rendererToSceneScale() {
        return canvasScaling * canvas.getLocalToSceneTransform().getMxx();
    }

    @Override
    public boolean isDefaultRenderer() {
        return true;
    }

    @Override
    public boolean isOpenGL() {
        return false;
    }
}
