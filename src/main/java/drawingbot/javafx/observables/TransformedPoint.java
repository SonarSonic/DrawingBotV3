package drawingbot.javafx.observables;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;

public class TransformedPoint {

    private final ObservablePoint src;
    private final ObservablePoint dst;

    public TransformedPoint(){
        this(0, 0);
    }

    public TransformedPoint(double srcX, double srcY){
        this.src = new ObservablePoint(srcX, srcY);
        this.dst = new ObservablePoint();
        init();
    }

    public TransformedPoint(ObservablePoint srcPoint){
        this.src = srcPoint;
        this.dst = new ObservablePoint();
        init();
    }

    private void init(){
        this.getSrc().xProperty().addListener((observable, oldValue, newValue) -> update());
        this.getSrc().yProperty().addListener((observable, oldValue, newValue) -> update());
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

            Point2D transformed = transform.get().transform(src.getX(), src.getY());
            dst.setX(transformed.getX());
            dst.setY(transformed.getY());
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

    public ObservablePoint getSrc() {
        return src;
    }

    ////////////////////////////////////////////////////////

    public ObservablePoint getDst() {
        return dst;
    }

    ////////////////////////////////////////////////////////
}
