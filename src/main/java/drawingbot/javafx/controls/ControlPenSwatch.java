package drawingbot.javafx.controls;

import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.scene.shape.Rectangle;

public class ControlPenSwatch extends Rectangle {

    public final ObservableDrawingPen drawingPen;

    public ControlPenSwatch(ObservableDrawingPen drawingPen, int width, int height){
        super(width, height);
        this.fillProperty().bind(drawingPen.javaFXColour);
        this.drawingPen = drawingPen;
    }

}