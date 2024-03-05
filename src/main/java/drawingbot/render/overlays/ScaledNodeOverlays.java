package drawingbot.render.overlays;

import drawingbot.render.viewport.Viewport;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class ScaledNodeOverlays extends ViewportOverlayBase {

    public ScaledNodeOverlays(){

        ListChangeListener<Node> childrenListChangeListener = c -> {
            while (c.next()){
                c.getRemoved().forEach(this::hideNode);
                c.getAddedSubList().forEach(this::showNode);
            }
        };

        childrenProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeListener(childrenListChangeListener);
                oldValue.forEach(this::hideNode);
            }
            if(newValue != null){
                newValue.addListener(childrenListChangeListener);
                newValue.forEach(this::showNode);
            }
        });
        children.set(FXCollections.observableArrayList());
    }

    ////////////////////////////////////////////////////////

    public final SimpleObjectProperty<ObservableList<Node>> children = new SimpleObjectProperty<>();

    public ObservableList<Node> getChildren() {
        return children.get();
    }

    public SimpleObjectProperty<ObservableList<Node>> childrenProperty() {
        return children;
    }

    public void setChildren(ObservableList<Node> children) {
        this.children.set(children);
    }

    ////////////////////////////////////////////////////////

    private void showNode(Node node){
        if(getViewport() == null){
            return;
        }
        node.getTransforms().add(getViewport().getCanvasToViewportTransform());
        getViewport().getForegroundOverlayNodes().add(node);

    }

    private void hideNode(Node node){
        if(getViewport() == null){
            return;
        }
        node.getTransforms().remove(getViewport().getCanvasToViewportTransform());
        getViewport().getBackgroundOverlayNodes().remove(node);
    }

    ////////////////////////////////////////////////////////

    @Override
    public void activateViewportOverlay(Viewport viewport) {
        super.activateViewportOverlay(viewport);
        getChildren().forEach(this::showNode);
    }

    @Override
    public void deactivateViewportOverlay(Viewport viewport) {
        getChildren().forEach(this::hideNode);
        super.deactivateViewportOverlay(viewport);
    }

    @Override
    public String getName() {
        return "Scaled Node Overlay";
    }
}
