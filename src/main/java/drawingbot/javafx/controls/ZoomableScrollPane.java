package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;

//SRC: https://stackoverflow.com/questions/39827911/javafx-8-scaling-zooming-scrollpane-relative-to-mouse-position
public class ZoomableScrollPane extends ScrollPane {

    public SimpleDoubleProperty scaleProperty = new SimpleDoubleProperty(1D);

    public double scaleValue = 1D;
    public double zoomIntensity = 0.02;
    public Node target;
    public Node overlays;
    public Node zoomNode;

    public ZoomableScrollPane(){
        super();
        //setPannable(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setFitToHeight(true); //center
        setFitToWidth(true); //center
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
    }

    public void init(Node target){
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));
        updateScale();
    }

    private Node outerNode(Node node){
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(e -> {
            e.consume();
            onScroll(e.getDeltaY()/10, new Point2D(e.getSceneX(), e.getSceneY()));
        });
        return outerNode;
    }

    private Node centeredNode(Node node){
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    public void updateScale() {
        scaleProperty.set(scaleValue);
        if(!DrawingBotV3.INSTANCE.displayMode.get().getRenderer().isOpenGL()){
            target.setScaleX(scaleValue);
            target.setScaleY(scaleValue);
            DrawingBotV3.RENDERER.updateCanvasScaling();
        }
    }

    public void onScroll(double wheelDelta, Point2D mousePoint) {
        if(DrawingBotV3.INSTANCE.dpiScaling.get()){
            return;
        }
        if(!DrawingBotV3.INSTANCE.displayMode.get().getRenderer().isOpenGL()){
            mousePoint = getContent().sceneToLocal(mousePoint);
        }


        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Bounds viewportBounds = getViewportBounds();
        Bounds innerBounds;
        if(!DrawingBotV3.INSTANCE.displayMode.get().getRenderer().isOpenGL()) {
            innerBounds = zoomNode.getLayoutBounds();
        }else{
            innerBounds = new BoundingBox(0, 0, DrawingBotV3.OPENGL_RENDERER.getPaneScaledWidth(), DrawingBotV3.OPENGL_RENDERER.getPaneScaledHeight());
        }

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = scaleValue * zoomFactor;
        updateScale();
        DrawingBotV3.OPENGL_RENDERER.updateCanvasPosition();
        this.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget;

        if(!DrawingBotV3.INSTANCE.displayMode.get().getRenderer().isOpenGL()){
            posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));
        }else{
            posInZoomTarget = DrawingBotV3.OPENGL_RENDERER.getPane().sceneToLocal(mousePoint);
        }

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds;

        if(!DrawingBotV3.INSTANCE.displayMode.get().getRenderer().isOpenGL()){
            updatedInnerBounds = zoomNode.getBoundsInLocal();
        }else{
            updatedInnerBounds = new BoundingBox(0, 0, DrawingBotV3.OPENGL_RENDERER.getPaneScaledWidth(), DrawingBotV3.OPENGL_RENDERER.getPaneScaledHeight());
        }

        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }
}