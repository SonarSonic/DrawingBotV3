package drawingbot.javafx.observables;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;

public class ObservablePoint implements Observable {

    public ObservablePoint(){}

    public ObservablePoint(double x, double y){
        this.set(x, y);
    }

    public ObservablePoint(ObservableValue<Double> bindX, ObservableValue<Double> bindY){
        this.xProperty().bind(bindX);
        this.yProperty().bind(bindY);
    }

    public void set(double x, double y){
        this.setX(x);
        this.setY(y);
    }

    ////////////////////////////////////////////////////////

    public final DoubleProperty x = new SimpleDoubleProperty();

    public double getX() {
        return x.get();
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public void setX(double x) {
        this.x.set(x);
    }

    ////////////////////////////////////////////////////////

    public final DoubleProperty y = new SimpleDoubleProperty();

    public double getY() {
        return y.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public void setY(double y) {
        this.y.set(y);
    }

    ////////////////////////////////////////////////////////

    public void bind(ObservablePoint other){
        this.xProperty().bind(other.xProperty());
        this.yProperty().bind(other.yProperty());
    }

    public void unbind(){
        this.xProperty().unbind();
        this.yProperty().unbind();
    }

    public void bindBidirectional(ObservablePoint other){
        this.xProperty().bindBidirectional(other.xProperty());
        this.yProperty().bindBidirectional(other.yProperty());
    }

    public void unbindBidirectional(ObservablePoint other){
        this.xProperty().unbindBidirectional(other.xProperty());
        this.yProperty().unbindBidirectional(other.yProperty());
    }

    @Override
    public void addListener(InvalidationListener listener) {

    }

    @Override
    public void removeListener(InvalidationListener listener) {

    }

    ////////////////////////////////////////////////////////
}
