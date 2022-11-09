package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.geom.snapping.ISnappingGuide;
import drawingbot.geom.snapping.RectangleSnappingGuide;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeManager;
import drawingbot.render.shapes.TransformModes;
import drawingbot.utils.Utils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import javafx.scene.transform.TransformChangedEvent;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ShapeOverlays extends AbstractOverlay{

    public static final ShapeOverlays INSTANCE = new ShapeOverlays();

    public static final String SELECTION_BOUNDING_BOX_STYLE_CLASS = "selection-bounding-box";
    public static final String SELECTION_TRANSFORM_HANDLE_STYLE_CLASS = "selection-resize-handle";

    /**
     * The global transform should be applied to any node which should be relative to the canvas and is within the overlay anchor pane
     * JavaFX Viewport Coordinate Space -> Canvas Coordinate Space
     */
    public Affine globalTransform = new Affine();
    {
        globalTransform.setOnTransformChanged(this::onGlobalTransformChanged);
    }

    public AnchorPane editOverlaysPane;
    public Rectangle boundingBox;

    public final SimpleBooleanProperty updatingBounds = new SimpleBooleanProperty();
    public final SimpleDoubleProperty drawingBoundingBoxX = new SimpleDoubleProperty();
    public final SimpleDoubleProperty drawingBoundingBoxY = new SimpleDoubleProperty();
    public final SimpleDoubleProperty drawingBoundingBoxWidth = new SimpleDoubleProperty();
    public final SimpleDoubleProperty drawingBoundingBoxHeight = new SimpleDoubleProperty();

    public final SimpleBooleanProperty enableRotation = new SimpleBooleanProperty(true);
    public final SimpleBooleanProperty showRotateControls = new SimpleBooleanProperty();
    public final SimpleBooleanProperty showDraggingControls = new SimpleBooleanProperty();

    public Shape anchorPointMarker;
    public final SimpleDoubleProperty anchorPointX = new SimpleDoubleProperty();
    public final SimpleDoubleProperty anchorPointY = new SimpleDoubleProperty();

    public final AnchorPane geometriesPane = new AnchorPane();

    public final List<ISnappingGuide> snappingGuides = new ArrayList<>();
    public final SimpleBooleanProperty enableSnapping = new SimpleBooleanProperty(true);
    public final RectangleSnappingGuide drawingSnappingGuide = new RectangleSnappingGuide(0,0,0,0);
    public final RectangleSnappingGuide pageSnappingGuide = new RectangleSnappingGuide(0,0,0,0);

    @Override
    public void init() {
        setActive(true);
        snappingGuides.add(drawingSnappingGuide);
        snappingGuides.add(pageSnappingGuide);

        geometriesPane.setPickOnBounds(false);

        editOverlaysPane = new AnchorPane();
        editOverlaysPane.setPickOnBounds(false);
        editOverlaysPane.setManaged(false);

        boundingBox = new Rectangle(0, 0, 400, 400);
        boundingBox.getStyleClass().add(SELECTION_BOUNDING_BOX_STYLE_CLASS);
        boundingBox.setManaged(false);
        boundingBox.setPickOnBounds(false);
        boundingBox.setOnMousePressed(e -> {
            if(e.isPrimaryButtonDown()) {
                onHandlePressed(TransformModes.MOVE, e);
            }
        });
        boundingBox.setOnMouseDragged(e -> {
            if(e.isPrimaryButtonDown()) {
                onHandleDragged(TransformModes.MOVE, e);
            }
        });

        drawingBoundingBoxX.addListener((observable, oldValue, newValue) -> {
            if(!updatingBounds.get()){
                double moveX = newValue.doubleValue() - oldValue.doubleValue();
                JFXShapeManager.INSTANCE.transformSelected(AffineTransform.getTranslateInstance(moveX, 0));
                JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
                JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::finishTransform);
            }
        });

        drawingBoundingBoxY.addListener((observable, oldValue, newValue) -> {
            if(!updatingBounds.get()){
                double moveY = newValue.doubleValue() - oldValue.doubleValue();
                JFXShapeManager.INSTANCE.transformSelected(AffineTransform.getTranslateInstance(0, moveY));
                JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
                JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::finishTransform);
            }
        });

        drawingBoundingBoxWidth.addListener((observable, oldValue, newValue) -> {
            if(!updatingBounds.get()){
                double scale = newValue.doubleValue() / oldValue.doubleValue();

                AffineTransform transform = new AffineTransform();
                transform.translate(drawingBoundingBoxX.get(),  drawingBoundingBoxY.get());
                transform.scale(scale, 1);
                transform.translate(-drawingBoundingBoxX.get(),  -drawingBoundingBoxY.get());

                JFXShapeManager.INSTANCE.transformSelected(transform);
                JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
                JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::finishTransform);
            }
        });

        drawingBoundingBoxHeight.addListener((observable, oldValue, newValue) -> {
            if(!updatingBounds.get()){
                double scale = newValue.doubleValue() / oldValue.doubleValue();

                AffineTransform transform = new AffineTransform();
                transform.translate(drawingBoundingBoxX.get(),  drawingBoundingBoxY.get());
                transform.scale(1, scale);
                transform.translate(-drawingBoundingBoxX.get(),  -drawingBoundingBoxY.get());

                JFXShapeManager.INSTANCE.transformSelected(transform);
                JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
                JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::finishTransform);
            }
        });


        boundingBox.setOnMouseReleased(e -> onHandleReleased(TransformModes.MOVE, e));
        boundingBox.setCursor(Cursor.MOVE);
        editOverlaysPane.getChildren().add(boundingBox);

        int corner = 8;
        int edge = 6;

        Shape cornerNWResize = setupResizeHandle(new Rectangle(corner, corner), corner, corner, TransformModes.NW_RESIZE);
        Shape cornerNEResize = setupResizeHandle(new Rectangle(corner, corner), corner, corner, TransformModes.NE_RESIZE);
        Shape cornerSWResize = setupResizeHandle(new Rectangle(corner, corner), corner, corner, TransformModes.SW_RESIZE);
        Shape cornerSEResize = setupResizeHandle(new Rectangle(corner, corner), corner, corner, TransformModes.SE_RESIZE);

        Shape edgeNResize = setupResizeHandle(new Rectangle(edge, edge), edge, edge, TransformModes.N_RESIZE);
        Shape edgeWResize = setupResizeHandle(new Rectangle(edge, edge), edge, edge, TransformModes.W_RESIZE);
        Shape edgeEResize = setupResizeHandle(new Rectangle(edge, edge), edge, edge, TransformModes.E_RESIZE);
        Shape edgeSResize = setupResizeHandle(new Rectangle(edge, edge), edge, edge, TransformModes.S_RESIZE);

        Shape cornerNWRotate = setupResizeHandle(new Circle(corner/2D), -1 + corner/4D, -1 + corner/4D, TransformModes.NW_ROTATE);
        Shape cornerNERotate = setupResizeHandle(new Circle(corner/2D), -1 + corner/4D, -1 + corner/4D, TransformModes.NE_ROTATE);
        Shape cornerSWRotate = setupResizeHandle(new Circle(corner/2D), -1 + corner/4D, -1 + corner/4D, TransformModes.SW_ROTATE);
        Shape cornerSERotate = setupResizeHandle(new Circle(corner/2D), -1 + corner/4D, -1 + corner/4D, TransformModes.SE_ROTATE);

        Shape edgeNSkew = setupResizeHandle(new Circle(edge/2D), -1 + edge/4D, -1 + edge/4D, TransformModes.N_SKEW);
        Shape edgeWSkew = setupResizeHandle(new Circle(edge/2D), -1 + edge/4D, -1 + edge/4D, TransformModes.W_SKEW);
        Shape edgeESkew = setupResizeHandle(new Circle(edge/2D), -1 + edge/4D, -1 + edge/4D, TransformModes.E_SKEW);
        Shape edgeSSkew = setupResizeHandle(new Circle(edge/2D), -1 + edge/4D, -1 + edge/4D, TransformModes.S_SKEW);

        anchorPointMarker = createCrosshair();
        anchorPointMarker.setManaged(false);
        anchorPointMarker.setStroke(Color.RED);
        anchorPointMarker.visibleProperty().bind(showDraggingControls);
        anchorPointMarker.setMouseTransparent(true);
        bindSceneToDrawing(anchorPointMarker, anchorPointX, anchorPointY);

        Shape centreMarker = createCrosshair();
        centreMarker.setManaged(false);
        centreMarker.setStroke(Color.BLACK);
        centreMarker.visibleProperty().bind(showDraggingControls.not());
        TransformModes.MOVE.bindings(centreMarker, 0, 0, boundingBox);
        centreMarker.setMouseTransparent(true);

        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if(e.isPrimaryButtonDown()){
                JFXShapeManager.INSTANCE.deselectAll();
            }
        });
        editOverlaysPane.getChildren().addAll(cornerNWResize, cornerNEResize, cornerSWResize, cornerSEResize, edgeNResize, edgeWResize, edgeEResize, edgeSResize);
        editOverlaysPane.getChildren().addAll(cornerNWRotate, cornerNERotate, cornerSWRotate, cornerSERotate, edgeNSkew, edgeWSkew, edgeESkew, edgeSSkew);
        editOverlaysPane.getChildren().addAll(anchorPointMarker, centreMarker);
        editOverlaysPane.visibleProperty().bind(JFXShapeManager.INSTANCE.hasSelection);

        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(geometriesPane);
        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(editOverlaysPane);
        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.setOnScroll(e -> {
            //prevent the overlay pane consuming the scroll event, and preventing it reaching the viewport
            DrawingBotV3.INSTANCE.controller.viewportScrollPane.getContent().getOnScroll().handle(e);
            e.consume();
        });
    }

    public Shape createCrosshair(){
        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M0,-6 L0,-1 M0,1 L0,6 M-6,0 L-1,0 M1,0 L6,0");
        svgPath.setSmooth(false);
        svgPath.setStroke(Color.BLACK);
        return svgPath;
    }

    public Shape setupResizeHandle(Shape shape, double width, double height, TransformModes resizeMode){
        shape.setManaged(false);
        shape.getStyleClass().add(SELECTION_TRANSFORM_HANDLE_STYLE_CLASS);
        shape.setFill(resizeMode.isSkew() || resizeMode.isRotation() ? new Color(0.75, 1, 0.75, 1) : Color.WHITE);
        shape.setSmooth(shape instanceof Circle);
        shape.setCursor(resizeMode.cursor);

        shape.setOnMousePressed(e -> {
            if(e.isPrimaryButtonDown()){
                onHandlePressed(resizeMode, e);
            }
        });
        shape.setOnMouseDragged(e -> {
            if(e.isPrimaryButtonDown()){
                onHandleDragged(resizeMode, e);
            }
        });
        shape.setOnMouseReleased(e -> {
            onHandleReleased(resizeMode, e);
        });

        resizeMode.bindings(shape, width, height, boundingBox);
        shape.visibleProperty().bind(Bindings.createBooleanBinding(() -> !showDraggingControls.get() && (resizeMode.isTranslation() || showRotateControls.get() == (resizeMode.isRotation() || resizeMode.isSkew())), showDraggingControls, showRotateControls));

        return shape;
    }

    private final SimpleBooleanProperty globalTransformDirtyMarker = new SimpleBooleanProperty();

    /**
     * Affine doesn't implement Observable, so we use a marker which is alternated on every change so we can observe the transform
     */
    public void onGlobalTransformChanged(TransformChangedEvent event){
        globalTransformDirtyMarker.set(!globalTransformDirtyMarker.get());
    }

    /**
     * Allows binding nodes in the scene to positions in the drawing without unwanted scaling, primarily for anchor points and guides, which should not scale with the drawing but still be positioned relative to it
     */
    public void bindSceneToDrawing(Node node, DoubleProperty drawingX, DoubleProperty drawingY){
        DoubleBinding[] bindings = createSceneToDrawingBinding(drawingX, drawingY);
        node.layoutXProperty().bind(bindings[0]);
        node.layoutYProperty().bind(bindings[1]);
    }

    /**
     * Creates two scene to drawing bindings for the X and Y components of the point
     */
    public DoubleBinding[] createSceneToDrawingBinding(DoubleProperty absoluteX, DoubleProperty absoluteY){
        return new DoubleBinding[]{
                Bindings.createDoubleBinding(() -> globalTransform.transform(absoluteX.getValue(), absoluteY.getValue()).getX(), globalTransformDirtyMarker, absoluteX, absoluteY),
                Bindings.createDoubleBinding(() -> globalTransform.transform(absoluteX.getValue(), absoluteY.getValue()).getY(), globalTransformDirtyMarker, absoluteX, absoluteY)
        };
    }

    ////////////////////////////

    @Override
    public void doRender() {
        super.doRender();

        //TODO DON'T UPDATE ALL OF THIS ON EVERY TICK IF WE CAN HELP IT

        ICanvas refCanvas = DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas();
        drawingSnappingGuide.rect.x = refCanvas.getDrawingOffsetX();
        drawingSnappingGuide.rect.y = refCanvas.getDrawingOffsetY();
        drawingSnappingGuide.rect.width = refCanvas.getDrawingWidth();
        drawingSnappingGuide.rect.height = refCanvas.getDrawingHeight();

        pageSnappingGuide.rect.x = 0;
        pageSnappingGuide.rect.y = 0;
        pageSnappingGuide.rect.width = refCanvas.getWidth();
        pageSnappingGuide.rect.height = refCanvas.getHeight();

        Point2D viewportOrigin = DrawingBotV3.INSTANCE.controller.viewportScrollPane.localToScene(0, 0);
        Point2D origin = DrawingBotV3.project().displayMode.get().getRenderer().rendererToScene(new Point2D(0, 0)).subtract(viewportOrigin);

        double scale = DrawingBotV3.project().displayMode.get().getRenderer().rendererToSceneScale() * refCanvas.getPlottingScale();
        globalTransform.setToTransform(scale, 0, origin.getX(), 0, scale, origin.getY());

        if(!JFXShapeManager.INSTANCE.selectedShapes.isEmpty()){
            Bounds drawingBox = createDrawingBoundingBox(JFXShapeManager.INSTANCE.selectedShapes);

            this.updatingBounds.set(true);
            this.drawingBoundingBoxX.set(Utils.roundToPrecision(drawingBox.getMinX(), 3));
            this.drawingBoundingBoxY.set(Utils.roundToPrecision(drawingBox.getMinY(), 3));
            this.drawingBoundingBoxWidth.set(Utils.roundToPrecision(drawingBox.getWidth(), 3));
            this.drawingBoundingBoxHeight.set(Utils.roundToPrecision(drawingBox.getHeight(), 3));
            this.updatingBounds.set(false);

            Bounds boundingBox = getViewportBoundingBox(JFXShapeManager.INSTANCE.selectedShapes);
            this.boundingBox.relocate(boundingBox.getMinX(), boundingBox.getMinY());
            this.boundingBox.setWidth(boundingBox.getWidth());
            this.boundingBox.setHeight(boundingBox.getHeight());
        }else{
            this.updatingBounds.set(true);
            this.drawingBoundingBoxX.set(0);
            this.drawingBoundingBoxY.set(0);
            this.drawingBoundingBoxWidth.set(0);
            this.drawingBoundingBoxHeight.set(0);
            this.updatingBounds.set(false);
        }
    }

    ////////////////////////////


    public void updateAnchorPoint(Point2D anchorPoint){
        anchorPointX.set(anchorPoint.getX());
        anchorPointY.set(anchorPoint.getY());
    }

    public double snapXToGuidelines(double x){
        if(!enableSnapping.get()){
            return x;
        }
        double scale = 1/DrawingBotV3.project().displayMode.get().getRenderer().rendererToSceneScale();
        double magnet = 5;
        double snappedX = Double.MAX_VALUE;
        for(ISnappingGuide guide : snappingGuides){
            if(guide.isEnabled()){
                double snapped = guide.snapToX(x, magnet, scale);
                if(snapped != Double.MAX_VALUE){
                    snappedX = snapped;
                    break;
                }
            }
        }
        return snappedX;
    }

    public double snapYToGuidelines(double y){
        if(!enableSnapping.get()){
            return y;
        }
        double scale = 1/DrawingBotV3.project().displayMode.get().getRenderer().rendererToSceneScale();
        double magnet = 5;
        double snappedY = Double.MAX_VALUE;
        for(ISnappingGuide guide : snappingGuides){
            if(guide.isEnabled()){
                double snapped = guide.snapToY(y, magnet, scale);
                if(snapped != Double.MAX_VALUE){
                    snappedY = snapped;
                    break;
                }
            }
        }
        return snappedY;
    }

    public double snapXToGrid(double x){
        if(!enableSnapping.get()){
            return x;
        }
        double snap = 1 * DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas().getPlottingScale();
        return Utils.roundToMultiple(x, snap);
    }

    public double snapYToGrid(double y){
        if(!enableSnapping.get()){
            return y;
        }
        double snap = 1 * DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas().getPlottingScale();
        return Utils.roundToMultiple(y, snap);
    }

    public Point2D snapTransformToGuidelines(double deltaX, double deltaY, Bounds bounds){
        if(!enableSnapping.get()){
            return new Point2D(deltaX, deltaY);
        }

        //try to snap the left side to the nearest guideline
        double offsetX = bounds.getMinX();
        double snapX = snapXToGuidelines(offsetX + deltaX);

        //try to snap the right side to the nearest guideline
        if(snapX == Double.MAX_VALUE){
            offsetX = bounds.getMaxX();
            snapX = snapXToGuidelines(offsetX + deltaX);
        }

        //try to snap the centre to the nearest guideline
        if(snapX == Double.MAX_VALUE){
            offsetX = bounds.getMinX() + bounds.getWidth()/2;
            snapX = snapXToGuidelines(offsetX + deltaX);
        }

        //snap to the default grid if no guidelines were found
        if(snapX == Double.MAX_VALUE){
            offsetX = bounds.getCenterX();
            snapX = snapXToGrid(offsetX + deltaX);
        }


        //try to snap the top side to the nearest guideline
        double offsetY = bounds.getMinY();
        double snapY = snapYToGuidelines(offsetY + deltaY);

        //try to snap the bottom side to the nearest guideline
        if(snapY == Double.MAX_VALUE){
            offsetY = bounds.getMaxY();
            snapY = snapYToGuidelines(offsetY + deltaY);
        }

        //try to snap the centre to the nearest guideline
        if(snapY == Double.MAX_VALUE){
            offsetY = bounds.getCenterY();
            snapY = snapYToGuidelines(offsetY + deltaY);
        }

        //snap to the default grid if no guidelines were found
        if(snapY == Double.MAX_VALUE){
            offsetY = bounds.getCenterY();
            snapY = snapYToGrid(offsetY + deltaY);
        }


        return new Point2D(snapX-offsetX, snapY-offsetY);

    }

    public Point2D snapToGuidelines(Point2D point){
        if(!enableSnapping.get()){
            return point;
        }

        double snapX = snapXToGuidelines(point.getX());
        double snapY = snapYToGuidelines(point.getY());
        if(snapX == Double.MAX_VALUE){
            snapX = snapXToGrid(point.getX());
        }
        if(snapY == Double.MAX_VALUE){
            snapY = snapYToGrid(point.getY());
        }
        return new Point2D(snapX, snapY);
    }

    ////////////////////////////

    private boolean isDragging = false;
    private boolean wasDragging = false;

    public TransformModes resizeMode = null;
    public Bounds originalSize = null;
    public Point2D mouseOrigin = null;
    public KeyCodeCombination delete = new KeyCodeCombination(KeyCode.DELETE);
    public KeyCodeCombination undo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    public KeyCodeCombination redo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

    public void onKeyPressed(KeyEvent event){
        //TODO KEY EVENTS ARE SLIGHTLY UNPREDICATABLE, ONLY REACT ON FOCUS, NOT ON HOVER
        if(delete.match(event)){
            JFXShapeManager.INSTANCE.deleteSelected();
            return;
        }
        if(undo.match(event)){
            JFXShapeManager.INSTANCE.activeShapeList.get().actionManager.undo();;
            event.consume();
            return;
        }

        if(redo.match(event)){
            JFXShapeManager.INSTANCE.activeShapeList.get().actionManager.redo();;
            event.consume();
            return;
        }
        if(isDragging && event.getCode() == KeyCode.ESCAPE){
            JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::cancelTransform);

            isDragging = false;
            event.consume();
        }
    }

    public void onHandlePressed(TransformModes resizeMode, MouseEvent event){
        this.resizeMode = resizeMode;
        if(!JFXShapeManager.INSTANCE.selectedShapes.isEmpty()){
            JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::startTransform);
            originalSize = createDrawingBoundingBox(JFXShapeManager.INSTANCE.selectedShapes);
            mouseOrigin = DrawingBotV3.project().displayMode.get().getRenderer().sceneToRenderer(new Point2D(event.getSceneX(), event.getSceneY()));
            mouseOrigin = mouseOrigin.multiply(1/DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas().getPlottingScale());
            isDragging = true;
            wasDragging = false;
        }
    }

    public void onHandleDragged(TransformModes resizeMode, MouseEvent event){
        if(!isDragging){
            return;
        }
        showDraggingControls.set(true);
        wasDragging = true;

        AffineTransform transform = new AffineTransform();

        Point2D mousePoint = DrawingBotV3.project().displayMode.get().getRenderer().sceneToRenderer(new Point2D(event.getSceneX(), event.getSceneY()));
        mousePoint = mousePoint.multiply(1/DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas().getPlottingScale());

        Point2D anchorPoint = resizeMode.anchorPoint(originalSize);

        double deltaX = mousePoint.getX() - mouseOrigin.getX();
        double deltaY = mousePoint.getY() - mouseOrigin.getY();

        if(resizeMode.isTranslation()){

            Point2D snapped = snapTransformToGuidelines(deltaX, deltaY, originalSize);
            double translateX = snapped.getX();
            double translateY = snapped.getY();

            if(event.isShiftDown()){
                //if shift is down only move the selection on one axis
                if(Math.abs(deltaX) < Math.abs(deltaY)){
                    translateX = 0;
                }else{
                    translateY = 0;
                }
            }
            transform.translate(translateX, translateY);
            JFXShapeManager.INSTANCE.transformSelected(transform);

            //update the anchor point for visual purposes only
            anchorPoint = anchorPoint.add(originalSize.getWidth()/2, originalSize.getHeight()/2).add(translateX, translateY);

        }else if(resizeMode.isScale()){
            mousePoint = snapToGuidelines(mousePoint);

            double scaleX = 1, scaleY = 1;

            switch (resizeMode) {
                case NW_RESIZE, SW_RESIZE, W_RESIZE -> scaleX = (anchorPoint.getX() - mousePoint.getX()) / originalSize.getWidth();
                case NE_RESIZE, SE_RESIZE, E_RESIZE -> scaleX = (mousePoint.getX() - anchorPoint.getX()) / originalSize.getWidth();
            }

            switch (resizeMode) {
                case NW_RESIZE, NE_RESIZE, N_RESIZE -> scaleY = (anchorPoint.getY() - mousePoint.getY()) / originalSize.getHeight();
                case SW_RESIZE, SE_RESIZE, S_RESIZE -> scaleY = (mousePoint.getY() - anchorPoint.getY()) / originalSize.getHeight();
            }

            if (event.isControlDown()) {
                //if ctrl is down make sure the selections proportions are maintained
                switch (resizeMode) {
                    case NW_RESIZE, NE_RESIZE, SW_RESIZE, SE_RESIZE -> {
                        if(Math.abs(scaleX) < Math.abs(scaleY)){
                            scaleY = scaleY < 0 ? -Math.abs(scaleX) : Math.abs(scaleX);
                        }else{
                            scaleX = scaleX < 0 ? -Math.abs(scaleY) : Math.abs(scaleY);
                        }
                    }
                    case N_RESIZE, S_RESIZE -> scaleX = scaleY;
                    case E_RESIZE, W_RESIZE -> scaleY = scaleX;
                }
            }

            if(event.isShiftDown()){
                //if shift is down scale the shape from the center rather than from the anchor point
                anchorPoint = new Point2D(originalSize.getMinX() + originalSize.getWidth()/2, originalSize.getMinY() + originalSize.getHeight()/2);
            }

            transform.translate(anchorPoint.getX(),  anchorPoint.getY());
            transform.scale(scaleX, scaleY);
            transform.translate(-anchorPoint.getX(),  -anchorPoint.getY());

            JFXShapeManager.INSTANCE.transformSelected(transform);
        }else if(resizeMode.isRotation()){
            if(!event.isShiftDown()){
                //if shift isn't down use the centre as the rotation point
                anchorPoint = new Point2D(originalSize.getMinX() + originalSize.getWidth()/2, originalSize.getMinY() + originalSize.getHeight()/2);
            }
            double refAngle = Math.toRadians(Math.atan2(anchorPoint.getY() - mouseOrigin.getY(), anchorPoint.getY() - mouseOrigin.getX()) * 180 / Math.PI);
            double currentAngle = Math.toRadians(Math.atan2(anchorPoint.getY() - mousePoint.getY(), anchorPoint.getY() - mousePoint.getX()) * 180 / Math.PI);

            double theta = currentAngle-refAngle;

            if(event.isControlDown()){
                //if control is down snap the rotations to a multiple of PI
                theta = Utils.roundToMultiple(theta, Math.PI/24D);
            }

            transform.rotate(theta, anchorPoint.getX(), anchorPoint.getY());

            JFXShapeManager.INSTANCE.transformSelected(transform);
        }else if(resizeMode.isSkew()){
            if(!event.isShiftDown()){
                //if shift isn't down use the centre as the rotation point
                anchorPoint = new Point2D(originalSize.getMinX() + originalSize.getWidth()/2, originalSize.getMinY() + originalSize.getHeight()/2);
            }

            double shearX = 0;
            double shearY = 0;

            switch (resizeMode) {
                case N_SKEW, S_SKEW -> shearX = deltaX / originalSize.getWidth();
                case E_SKEW, W_SKEW -> shearY = deltaY / originalSize.getHeight();
            }

            transform.translate(anchorPoint.getX(),  anchorPoint.getY());
            transform.shear(shearX, shearY);
            transform.translate(-anchorPoint.getX(),  -anchorPoint.getY());

            JFXShapeManager.INSTANCE.transformSelected(transform);
        }

        updateAnchorPoint(anchorPoint);
    }

    public void onHandleReleased(TransformModes handle, MouseEvent event){
        if(isDragging){
            isDragging = false;
            JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
            JFXShapeManager.INSTANCE.selectedShapes.forEach(JFXShape::finishTransform);

            if(enableRotation.get() && !wasDragging && handle == TransformModes.MOVE){
                showRotateControls.set(!showRotateControls.get());
            }
        }
        showDraggingControls.set(false);
    }

    public static Bounds createDrawingBoundingBox(Collection<JFXShape> selected){
        return createBoundingBox(selected, JFXShape::getDrawingBounds);
    }

    public static Bounds getViewportBoundingBox(Collection<JFXShape> selected){
        return createBoundingBox(selected, JFXShape::getViewportBounds);
    }

    public static <O> Bounds createBoundingBox(Collection<O> selected, Function<O, Bounds> supplier){
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for(O obj : selected){
            Bounds box = supplier.apply(obj);
            minX = Math.min(box.getMinX(), minX);
            minY = Math.min(box.getMinY(), minY);
            maxX = Math.max(box.getMaxX(), maxX);
            maxY = Math.max(box.getMaxY(), maxY);
        }
        return new BoundingBox(minX, minY, Math.abs(maxX-minX), Math.abs(maxY-minY));
    }

    @Override
    public String getName() {
        return "Shape Overlays";
    }
}
