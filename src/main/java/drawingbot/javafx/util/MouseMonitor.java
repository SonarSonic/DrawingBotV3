package drawingbot.javafx.util;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

public class MouseMonitor implements EventHandler<MouseEvent> {

    private double x;
    private double y;
    private double sceneX;
    private double sceneY;

    @Override
    public void handle(MouseEvent event) {
        x = event.getX();
        y = event.getY();
        sceneX = event.getSceneX();
        sceneY = event.getSceneY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSceneX() {
        return sceneX;
    }

    public double getSceneY() {
        return sceneY;
    }

    public Point2D getPos(){
        return new Point2D(x, y);
    }

    public Point2D getScenePos(){
        return new Point2D(sceneX, sceneY);
    }
}
