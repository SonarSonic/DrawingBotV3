package drawingbot.render.shapes;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public enum TransformModes {

    NW_RESIZE(JFXShapeManager.SCALE, Cursor.NW_RESIZE),
    NE_RESIZE(JFXShapeManager.SCALE, Cursor.NE_RESIZE),
    SW_RESIZE(JFXShapeManager.SCALE, Cursor.SW_RESIZE),
    SE_RESIZE(JFXShapeManager.SCALE, Cursor.SE_RESIZE),
    N_RESIZE(JFXShapeManager.SCALE, Cursor.N_RESIZE),
    W_RESIZE(JFXShapeManager.SCALE, Cursor.W_RESIZE),
    E_RESIZE(JFXShapeManager.SCALE, Cursor.E_RESIZE),
    S_RESIZE(JFXShapeManager.SCALE, Cursor.S_RESIZE),

    NW_ROTATE(JFXShapeManager.ROTATE, Cursor.NE_RESIZE),
    NE_ROTATE(JFXShapeManager.ROTATE, Cursor.SE_RESIZE),
    SW_ROTATE(JFXShapeManager.ROTATE, Cursor.SE_RESIZE),
    SE_ROTATE(JFXShapeManager.ROTATE, Cursor.NE_RESIZE),

    N_SKEW(JFXShapeManager.SKEW, Cursor.E_RESIZE),
    E_SKEW(JFXShapeManager.SKEW, Cursor.N_RESIZE),
    S_SKEW(JFXShapeManager.SKEW, Cursor.W_RESIZE),
    W_SKEW(JFXShapeManager.SKEW, Cursor.N_RESIZE),

    MOVE(JFXShapeManager.TRANSLATE, Cursor.MOVE);

    public Cursor cursor;
    public int type;

    TransformModes(int type, Cursor cursor) {
        this.type = type;
        this.cursor = cursor;
    }

    public boolean isTranslation() {
        return type == JFXShapeManager.TRANSLATE;
    }

    public boolean isRotation() {
        return type == JFXShapeManager.ROTATE;
    }

    public boolean isSkew() {
        return type == JFXShapeManager.SKEW;
    }

    public boolean isScale() {
        return type == JFXShapeManager.SCALE;
    }

    public TransformModes opposite() {
        return switch (this) {
            case NW_RESIZE -> SE_RESIZE;
            case NE_RESIZE -> SW_RESIZE;
            case SW_RESIZE -> NE_RESIZE;
            case SE_RESIZE -> NW_RESIZE;
            case N_RESIZE -> S_RESIZE;
            case E_RESIZE -> W_RESIZE;
            case S_RESIZE -> N_RESIZE;
            case W_RESIZE -> E_RESIZE;

            case NW_ROTATE -> SE_ROTATE;
            case NE_ROTATE -> SW_ROTATE;
            case SW_ROTATE -> NE_ROTATE;
            case SE_ROTATE -> NW_ROTATE;

            case N_SKEW -> S_SKEW;
            case E_SKEW -> W_SKEW;
            case S_SKEW -> N_SKEW;
            case W_SKEW -> E_SKEW;

            case MOVE -> MOVE;
        };
    }

    public Point2D anchorPoint(Bounds refRect) {
        return switch (this) {
            case NE_RESIZE, NE_ROTATE -> new Point2D(refRect.getMinX(), refRect.getMinY() + refRect.getHeight());
            case NW_RESIZE, NW_ROTATE -> new Point2D(refRect.getMinX() + refRect.getWidth(), refRect.getMinY() + refRect.getHeight());
            case SE_RESIZE, SE_ROTATE -> new Point2D(refRect.getMinX(), refRect.getMinY());
            case SW_RESIZE, SW_ROTATE -> new Point2D(refRect.getMinX() + refRect.getWidth(), refRect.getMinY());
            case N_RESIZE, N_SKEW -> new Point2D(refRect.getMinX() + refRect.getWidth() / 2, refRect.getMinY() + refRect.getHeight());
            case E_RESIZE, E_SKEW -> new Point2D(refRect.getMinX(), refRect.getMinY() + refRect.getHeight() / 2);
            case S_RESIZE, S_SKEW -> new Point2D(refRect.getMinX() + refRect.getWidth() / 2, refRect.getMinY());
            case W_RESIZE, W_SKEW -> new Point2D(refRect.getMinX() + refRect.getWidth(), refRect.getMinY() + refRect.getHeight() / 2);
            case MOVE -> new Point2D(refRect.getMinX(), refRect.getMinY());
        };
    }

    public void bindings(Node handle, double width, double height, Rectangle boundingBox) {
        switch (this) {
            case NW_RESIZE, NW_ROTATE -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().subtract(height / 2));
            }
            case NE_RESIZE, NE_ROTATE -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().add(boundingBox.widthProperty()).subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().subtract(height / 2));
            }
            case SW_RESIZE, SW_ROTATE -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().add(boundingBox.heightProperty()).subtract(height / 2));
            }
            case SE_RESIZE, SE_ROTATE -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().add(boundingBox.widthProperty()).subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().add(boundingBox.heightProperty()).subtract(height / 2));
            }
            case N_RESIZE, N_SKEW -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().add(boundingBox.widthProperty().divide(2)).subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().subtract(height / 2));
            }
            case W_RESIZE, W_SKEW -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().add(boundingBox.heightProperty().divide(2)).subtract(height / 2));
            }
            case E_RESIZE, E_SKEW -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().add(boundingBox.widthProperty()).subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().add(boundingBox.heightProperty().divide(2)).subtract(height / 2));
            }
            case S_RESIZE, S_SKEW -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().add(boundingBox.widthProperty().divide(2)).subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().add(boundingBox.heightProperty()).subtract(height / 2));
            }
            case MOVE -> {
                handle.layoutXProperty().bind(boundingBox.layoutXProperty().add(boundingBox.widthProperty().divide(2)).subtract(width / 2));
                handle.layoutYProperty().bind(boundingBox.layoutYProperty().add(boundingBox.heightProperty().divide(2)).subtract(height / 2));
            }
        }
    }
}
