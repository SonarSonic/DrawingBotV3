package drawingbot.javafx.observables;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;

public class TransformedRectangle {

    private final ObservableRectangle src;
    private final ObservableRectangle dst;

    public TransformedRectangle(){
        this(0, 0, 0, 0);
    }

    public TransformedRectangle(double startX, double startY, double endX, double endY){
        this.src = new ObservableRectangle(startX, startY, endX, endY);
        this.dst = new ObservableRectangle();
        init();
    }

    public TransformedRectangle(ObservableRectangle srcRect){
        this.src = srcRect;
        this.dst = new ObservableRectangle();
        init();
    }

    private void init(){
        this.getSrcRectangle().startXProperty().addListener((observable, oldValue, newValue) -> update());
        this.getSrcRectangle().startYProperty().addListener((observable, oldValue, newValue) -> update());
        this.getSrcRectangle().endXProperty().addListener((observable, oldValue, newValue) -> update());
        this.getSrcRectangle().endYProperty().addListener((observable, oldValue, newValue) -> update());
        this.transformProperty().addListener((observable) -> {
            update();
        });
        update();
    }

    private void update(){
        if(transform.get() == null){
            dst.bind(src);
        }else{
            dst.unbind();

            Point2D startT = transform.get().transform(src.getStartX(), src.getStartY());
            dst.setStartX(startT.getX());
            dst.setStartY(startT.getY());

            Point2D endT = transform.get().transform(src.getEndX(), src.getEndY());
            dst.setEndX(endT.getX());
            dst.setEndY(endT.getY());
        }
    }

    private final ObjectProperty<Transform> transform = new SimpleObjectProperty<>();

    public Transform getTransform() {
        return transform.get();
    }

    public ObjectProperty<Transform> transformProperty() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform.set(transform);
    }

    ////////////////////////////////////////////////////////

    public ObservableRectangle getSrcRectangle() {
        return src;
    }

    ////////////////////////////////////////////////////////

    public ObservableRectangle getDstRectangle() {
        return dst;
    }

    ////////////////////////////////////////////////////////
}
