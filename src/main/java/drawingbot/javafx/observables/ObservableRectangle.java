package drawingbot.javafx.observables;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;

public class ObservableRectangle {

    public ObservableRectangle(){}

    public ObservableRectangle(double startX, double startY, double endX, double endY){
        this.setStartX(startX);
        this.setStartY(startY);
        this.setEndX(endX);
        this.setEndY(endY);
    }

    public ObservableRectangle(ObservableValue<Double> bindStartX, ObservableValue<Double> bindStartY, ObservableValue<Double> bindEndX, ObservableValue<Double> bindEndY){
        this.startXProperty().bind(bindStartX);
        this.startYProperty().bind(bindStartY);
        this.endXProperty().bind(bindEndX);
        this.endYProperty().bind(bindEndY);
    }

    ////////////////////////////////////////////////////////

    public final DoubleProperty startX = new SimpleDoubleProperty();

    public double getStartX() {
        return startX.get();
    }

    public DoubleProperty startXProperty() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX.set(startX);
    }

    ////////////////////////////////////////////////////////

    public final DoubleProperty startY = new SimpleDoubleProperty();

    public double getStartY() {
        return startY.get();
    }

    public DoubleProperty startYProperty() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY.set(startY);
    }

    ////////////////////////////////////////////////////////

    public final DoubleProperty endX = new SimpleDoubleProperty();

    public double getEndX() {
        return endX.get();
    }

    public DoubleProperty endXProperty() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX.set(endX);
    }

    ////////////////////////////////////////////////////////

    public final DoubleProperty endY = new SimpleDoubleProperty();

    public double getEndY() {
        return endY.get();
    }

    public DoubleProperty endYProperty() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY.set(endY);
    }

    ////////////////////////////////////////////////////////

    private DoubleProperty width = null;

    public double getWidth() {
        return width == null ? getEndX()-getStartX() : width.get();
    }

    public DoubleProperty widthProperty() {
        if(width == null){
            width = new SimpleDoubleProperty();
            width.bind(endXProperty().subtract(startXProperty()));
        }
        return width;
    }

    ////////////////////////////////////////////////////////

    private DoubleProperty height = null;

    public double getHeight() {
        return height == null ? getEndY()-getStartY() : height.get();
    }

    public DoubleProperty heightProperty() {
        if(height == null){
            height = new SimpleDoubleProperty();
            height.bind(endYProperty().subtract(startYProperty()));
        }
        return height;
    }

    ////////////////////////////////////////////////////////

    private DoubleProperty centerY = null;

    public double getCenterY() {
        return centerY == null ? getStartY() + (getEndY()-getStartY())/2 : centerY.get();
    }

    public DoubleProperty centerYProperty() {
        if(centerY == null){
            centerY = new SimpleDoubleProperty();
            centerY.bind(Bindings.createDoubleBinding(() -> getStartY() + (getEndY()-getStartY())/2, startYProperty(), endYProperty()));
        }
        return centerY;
    }

    ////////////////////////////////////////////////////////

    private DoubleProperty centerX = null;

    public double getCenterX() {
        return centerX == null ? getStartX() + (getEndX()-getStartX())/2 : centerX.get();
    }

    public DoubleProperty centerXProperty() {
        if(centerX == null){
            centerX = new SimpleDoubleProperty();
            centerX.bind(Bindings.createDoubleBinding(() -> getStartX() + (getEndX()-getStartX())/2, startYProperty(), endYProperty()));
        }
        return centerX;
    }

    ////////////////////////////////////////////////////////

    public void bind(ObservableRectangle other){
        this.startXProperty().bind(other.startXProperty());
        this.startYProperty().bind(other.startYProperty());
        this.endXProperty().bind(other.endXProperty());
        this.endYProperty().bind(other.endYProperty());
    }

    public void unbind(){
        this.startXProperty().unbind();
        this.startYProperty().unbind();
        this.endXProperty().unbind();
        this.endYProperty().unbind();
    }

    public void bindBidirectional(ObservableRectangle other){
        this.startXProperty().bindBidirectional(other.startXProperty());
        this.startYProperty().bindBidirectional(other.startYProperty());
        this.endXProperty().bindBidirectional(other.endXProperty());
        this.endYProperty().bindBidirectional(other.endYProperty());
    }

    public void unbindBidirectional(ObservableRectangle other){
        this.startXProperty().unbindBidirectional(other.startXProperty());
        this.startYProperty().unbindBidirectional(other.startYProperty());
        this.endXProperty().unbindBidirectional(other.endXProperty());
        this.endYProperty().unbindBidirectional(other.endYProperty());
    }

    ////////////////////////////////////////////////////////

    public void bindStart(ObservablePoint startPoint){
        this.startXProperty().bind(startPoint.xProperty());
        this.startYProperty().bind(startPoint.yProperty());
    }

    public void unbindStart(){
        this.startXProperty().unbind();
        this.startYProperty().unbind();
    }

    public void bindStartBidirectional(ObservablePoint startPoint){
        this.startXProperty().bindBidirectional(startPoint.xProperty());
        this.startYProperty().bindBidirectional(startPoint.yProperty());
    }

    public void unbindStartBidirectional(ObservablePoint startPoint){
        this.startXProperty().unbindBidirectional(startPoint.xProperty());
        this.startYProperty().unbindBidirectional(startPoint.yProperty());
    }

    ////////////////////////////////////////////////////////

    public void bindEnd(ObservablePoint startPoint){
        this.endXProperty().bind(startPoint.xProperty());
        this.endYProperty().bind(startPoint.yProperty());
    }

    public void unbindEnd(){
        this.endXProperty().unbind();
        this.endYProperty().unbind();
    }

    public void bindEndBidirectional(ObservablePoint endPoint){
        this.endXProperty().bindBidirectional(endPoint.xProperty());
        this.endYProperty().bindBidirectional(endPoint.yProperty());
    }

    public void unbindEndBidirectional(ObservablePoint endPoint){
        this.endXProperty().unbindBidirectional(endPoint.xProperty());
        this.endYProperty().unbindBidirectional(endPoint.yProperty());
    }

    ////////////////////////////////////////////////////////

}