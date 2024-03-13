package drawingbot.render.viewport;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;

/**
 * The scroll pane used by the {@link Viewport} which enables zooming / panning / scrolling
 * Extended from: https://stackoverflow.com/questions/39827911/javafx-8-scaling-zooming-scrollpane-relative-to-mouse-position
 */
public class ZoomableScrollPane extends ScrollPane {

    private final Group zoomNode;
    private final Viewport viewport;

    public ZoomableScrollPane(Viewport viewport) {
        super();
        this.viewport = viewport;
        setPannable(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setFitToHeight(true);
        setFitToWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        this.zoomNode = new Group();
        setContent(outerNode(zoomNode));
        targetProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.scaleXProperty().unbind();
                oldValue.scaleYProperty().unbind();
                zoomNode.getChildren().remove(oldValue);
            }
            if (newValue != null) {
                newValue.scaleXProperty().bind(viewport.zoomProperty());
                newValue.scaleYProperty().bind(viewport.zoomProperty());
                zoomNode.getChildren().add(newValue);
            }
            resetView();
        });
        targetProperty().bind(viewport.rendererNodeProperty());
    }

    ////////////////////////////////////////////////////////

    private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(this::onScroll);
        return outerNode;
    }

    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Node> target = new SimpleObjectProperty<>();

    public Node getTarget() {
        return target.get();
    }

    public ObjectProperty<Node> targetProperty() {
        return target;
    }

    public void setTarget(Node target) {
        this.target.set(target);
    }

    ////////////////////////////////////////////////////////

    public void zoomIn() {
        viewport.setZoom(viewport.getZoom() + 0.1);
    }

    public void zoomOut() {
        viewport.setZoom(Math.max(0.01, viewport.getZoom() - 0.1));
    }

    public void resetView() {
        setHvalue(0.5);
        setVvalue(0.5);

        if (!viewport.zoomProperty().isBound()) {
            viewport.setZoom(1);
        }

        viewport.layout();

        if (!viewport.zoomProperty().isBound()) {
            viewport.setZoom(1);
        }

        setHvalue(0.5);
        setVvalue(0.5);
    }


    ////////////////////////////////////////////////////////

    public void onScroll(ScrollEvent event) {
        if(getTarget() == null){
            return;
        }

        //We're probably using DPI Scaling
        if(viewport.zoomProperty().isBound()){
            return;
        }

        double wheelDelta = event.getDeltaY()/10;

        double zoomFactor = Math.exp(wheelDelta * viewport.getZoomIntensity());

        Bounds viewportBounds = getViewportBounds();
        Bounds innerBounds = zoomNode.getLayoutBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        viewport.setZoom(viewport.getZoom() * zoomFactor);
        this.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = getTarget().parentToLocal(zoomNode.parentToLocal(new Point2D(event.getX(), event.getY())));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = getTarget().getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();

        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));

        event.consume();

    }
}