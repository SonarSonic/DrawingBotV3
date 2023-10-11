package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.snapping.ISnappingGuide;
import drawingbot.geom.snapping.RectangleSnappingGuide;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeManager;
import drawingbot.render.shapes.TransformModes;
import drawingbot.utils.Utils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Affine;
import javafx.scene.transform.TransformChangedEvent;

import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.function.Function;

public class ShapeOverlays extends AbstractOverlay{

    public static final ShapeOverlays INSTANCE = new ShapeOverlays();

    public static final String SELECTION_BOUNDING_BOX_STYLE_CLASS = "selection-bounding-box";
    public static final String SELECTION_TRANSFORM_HANDLE_STYLE_CLASS = "selection-resize-handle";
    public static final String SELECTION_CLOSE_HANDLE_STYLE_CLASS = "selection-close-handle";

    /**
     * The global transform should be applied to any node which should be relative to the canvas and is within the overlay anchor pane
     * JavaFX Viewport Coordinate Space -> Canvas Coordinate Space
     */
    public Affine globalTransform = new Affine();
    {
        globalTransform.setOnTransformChanged(this::onGlobalTransformChanged);
    }
    public final SimpleDoubleProperty relativeStrokeSize = new SimpleDoubleProperty(1D);

    public AnchorPane editOverlaysPane;
    public AnchorPane vertexHandlesPane;
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

    public final SimpleObjectProperty<ToolMode> toolMode = new SimpleObjectProperty<>(ToolMode.SELECT);

    public enum ToolMode {
        SELECT,
        EDIT,
        DRAW_BEZIERS
    }

    @Override
    public void init() {
        setActive(true);
        snappingGuides.add(drawingSnappingGuide);
        snappingGuides.add(pageSnappingGuide);

        geometriesPane.setPickOnBounds(false);

        editOverlaysPane = new AnchorPane();
        editOverlaysPane.setPickOnBounds(false);
        editOverlaysPane.setManaged(false);

        vertexHandlesPane = new AnchorPane();
        vertexHandlesPane.setPickOnBounds(false);
        vertexHandlesPane.setManaged(false);
        vertexHandlesPane.visibleProperty().bind(toolMode.isNotEqualTo(ToolMode.SELECT));

        boundingBox = new Rectangle(0, 0, 400, 400);
        boundingBox.getStyleClass().add(SELECTION_BOUNDING_BOX_STYLE_CLASS);
        boundingBox.setManaged(false);
        boundingBox.setPickOnBounds(false);
        boundingBox.setOnMousePressed(e -> {
            if(e.isPrimaryButtonDown()) {
                onResizeHandlePressed(TransformModes.MOVE, e);
            }
        });
        boundingBox.setOnMouseDragged(e -> {
            if(e.isPrimaryButtonDown()) {
                onResizeHandleDragged(TransformModes.MOVE, e);
            }
        });
        boundingBox.visibleProperty().bind(toolMode.isNotEqualTo(ToolMode.DRAW_BEZIERS));

        drawingBoundingBoxX.addListener((observable, oldValue, newValue) -> {
            if(!updatingBounds.get()){
                double moveX = newValue.doubleValue() - oldValue.doubleValue();
                JFXShapeManager.INSTANCE.transformSelected(AffineTransform.getTranslateInstance(moveX, 0));
                JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
                JFXShapeManager.INSTANCE.activeShapeList.get().getSelectionList().forEach(JFXShape::finishTransform);
            }
        });

        drawingBoundingBoxY.addListener((observable, oldValue, newValue) -> {
            if(!updatingBounds.get()){
                double moveY = newValue.doubleValue() - oldValue.doubleValue();
                JFXShapeManager.INSTANCE.transformSelected(AffineTransform.getTranslateInstance(0, moveY));
                JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
                JFXShapeManager.INSTANCE.activeShapeList.get().getSelectionList().forEach(JFXShape::finishTransform);
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
                JFXShapeManager.INSTANCE.getSelectedShapes().forEach(JFXShape::finishTransform);
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
                JFXShapeManager.INSTANCE.getSelectedShapes().forEach(JFXShape::finishTransform);
            }
        });


        boundingBox.setOnMouseReleased(e -> onResizeHandleReleased(TransformModes.MOVE, e));
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
        anchorPointMarker.visibleProperty().bind(showDraggingControls.and(toolMode.isEqualTo(ToolMode.SELECT)));
        anchorPointMarker.setMouseTransparent(true);
        bindSceneToDrawing(anchorPointMarker, anchorPointX, anchorPointY);

        Shape centreMarker = createCrosshair();
        centreMarker.setManaged(false);
        centreMarker.setStroke(Color.BLACK);
        centreMarker.visibleProperty().bind(showDraggingControls.and(toolMode.isEqualTo(ToolMode.SELECT)));
        TransformModes.MOVE.bindings(centreMarker, 0, 0, boundingBox);
        centreMarker.setMouseTransparent(true);

        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        
        editOverlaysPane.getChildren().addAll(cornerNWResize, cornerNEResize, cornerSWResize, cornerSEResize, edgeNResize, edgeWResize, edgeEResize, edgeSResize);
        editOverlaysPane.getChildren().addAll(cornerNWRotate, cornerNERotate, cornerSWRotate, cornerSERotate, edgeNSkew, edgeWSkew, edgeESkew, edgeSSkew);
        editOverlaysPane.getChildren().addAll(anchorPointMarker, centreMarker, vertexHandlesPane);
        editOverlaysPane.visibleProperty().bind(JFXShapeManager.INSTANCE.hasSelection);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventHandler(KeyEvent.KEY_RELEASED, this::onKeyReleased);

        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(geometriesPane);
        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(editOverlaysPane);
        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.setOnScroll(e -> {
            //prevent the overlay pane consuming the scroll event, and preventing it reaching the viewport
            DrawingBotV3.INSTANCE.controller.viewportScrollPane.getContent().getOnScroll().handle(e);
            e.consume();
        });

        toolMode.addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.INSTANCE.controller.viewportScrollPane.setCursor(newValue == ToolMode.DRAW_BEZIERS ? Cursor.CROSSHAIR : Cursor.DEFAULT);
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
                onResizeHandlePressed(resizeMode, e);
            }
        });
        shape.setOnMouseDragged(e -> {
            if(e.isPrimaryButtonDown()){
                onResizeHandleDragged(resizeMode, e);
            }
        });
        shape.setOnMouseReleased(e -> {
            onResizeHandleReleased(resizeMode, e);
        });

        resizeMode.bindings(shape, width, height, boundingBox);
        shape.visibleProperty().bind(Bindings.createBooleanBinding(() -> toolMode.get() == ToolMode.SELECT && !showDraggingControls.get() && (resizeMode.isTranslation() || showRotateControls.get() == (resizeMode.isRotation() || resizeMode.isSkew())), showDraggingControls, showRotateControls, toolMode));

        return shape;
    }

    public Shape setupVertexHandle(PathElement pathElement, Shape displayShape, double width, double height, DoubleProperty xAnchor, DoubleProperty yAnchor){
        return setupVertexHandle(VertexHandleType.POINT, pathElement, displayShape, width, height, xAnchor, yAnchor, xAnchor, yAnchor);
    }

    public Shape setupVertexHandle(VertexHandleType handleType, PathElement pathElement, Shape displayShape, double width, double height, DoubleProperty xAnchor, DoubleProperty yAnchor){
        return setupVertexHandle(handleType, pathElement, displayShape, width, height, xAnchor, yAnchor, xAnchor, yAnchor);
    }

    public Shape setupVertexHandle(VertexHandleType handleType, PathElement pathElement, Shape displayShape, double width, double height, DoubleProperty xAnchor, DoubleProperty yAnchor, DoubleProperty xProp, DoubleProperty yProp){
        displayShape.setManaged(false);
        displayShape.getStyleClass().add(SELECTION_TRANSFORM_HANDLE_STYLE_CLASS);
        displayShape.setFill(Color.WHITE);
        displayShape.setSmooth(displayShape instanceof Circle);
        displayShape.setCursor(Cursor.DEFAULT);

        displayShape.setOnMousePressed(e -> {
            if(e.isPrimaryButtonDown()){
                onVertexHandlePressed(handleType, pathElement, e, xProp, yProp);
            }
        });
        displayShape.setOnMouseDragged(e -> {
            if(e.isPrimaryButtonDown()){
                onVertexHandleDragged(handleType, pathElement,e, xProp, yProp);
            }
        });
        displayShape.setOnMouseReleased(e -> {
            onVertexHandleReleased(handleType, pathElement,e, xProp, yProp);
        });

        bindSceneToDrawing(displayShape, xAnchor, yAnchor, -width/2, -height/2);

        displayShape.visibleProperty().bind(toolMode.isNotEqualTo(ToolMode.SELECT));
        return displayShape;
    }

    private final SimpleBooleanProperty globalTransformDirtyMarker = new SimpleBooleanProperty();

    /**
     * Affine doesn't implement Observable, so we use a marker which is alternated on every change so we can observe the transform
     */
    public void onGlobalTransformChanged(TransformChangedEvent event){
        globalTransformDirtyMarker.set(!globalTransformDirtyMarker.get());
        relativeStrokeSize.set(Math.abs(1 / globalTransform.getMxx()));
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
     * Allows binding nodes in the scene to positions in the drawing without unwanted scaling, primarily for anchor points and guides, which should not scale with the drawing but still be positioned relative to it
     */
    public void bindSceneToDrawing(Node node, DoubleProperty drawingX, DoubleProperty drawingY, double offsetX, double offsetY){
        DoubleBinding[] bindings = createSceneToDrawingBinding(drawingX, drawingY);
        node.layoutXProperty().bind(bindings[0].add(offsetX));
        node.layoutYProperty().bind(bindings[1].add(offsetY));
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

        if(!JFXShapeManager.INSTANCE.getSelectedShapes().isEmpty()){
            Bounds drawingBox = createDrawingBoundingBox(JFXShapeManager.INSTANCE.getSelectedShapes());

            this.updatingBounds.set(true);
            this.drawingBoundingBoxX.set(Utils.roundToPrecision(drawingBox.getMinX(), 3));
            this.drawingBoundingBoxY.set(Utils.roundToPrecision(drawingBox.getMinY(), 3));
            this.drawingBoundingBoxWidth.set(Utils.roundToPrecision(drawingBox.getWidth(), 3));
            this.drawingBoundingBoxHeight.set(Utils.roundToPrecision(drawingBox.getHeight(), 3));
            this.updatingBounds.set(false);

            Bounds boundingBox = getViewportBoundingBox(JFXShapeManager.INSTANCE.getSelectedShapes());
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
    private boolean hasArrowKeyMove = false;
    private double arrowTranslateX = 0;
    private double arrowTranslateY = 0;

    public TransformModes resizeMode = null;
    public Bounds originalSize = null;
    public Point2D mouseOrigin = null;
    public KeyCodeCombination delete = new KeyCodeCombination(KeyCode.DELETE);
    public KeyCodeCombination undo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    public KeyCodeCombination redo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    public KeyCodeCombination up = new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_ANY);
    public KeyCodeCombination down = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_ANY);
    public KeyCodeCombination left = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_ANY);
    public KeyCodeCombination right = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_ANY);


    public void onKeyPressed(KeyEvent event){
        //TODO KEY EVENTS ARE SLIGHTLY UNPREDICATABLE, ONLY REACT ON FOCUS, NOT ON HOVER
        if(delete.match(event)){
            JFXShapeManager.INSTANCE.deleteSelected();
            return;
        }
        if(undo.match(event)){
            JFXShapeManager.INSTANCE.activeShapeList.get().actionManager.undo();
            event.consume();
            return;
        }

        if(redo.match(event)){
            JFXShapeManager.INSTANCE.activeShapeList.get().actionManager.redo();
            event.consume();
            return;
        }
        if(!JFXShapeManager.INSTANCE.getSelectedShapes().isEmpty()){
            boolean match = false;
            double inc = event.isShiftDown() ? 0.5 : 1D;

            if(up.match(event)){
                arrowTranslateY-=inc;
                match = true;
            }
            if(down.match(event)){
                arrowTranslateY+=inc;
                match = true;
            }
            if(left.match(event)){
                arrowTranslateX-=inc;
                match = true;
            }
            if(right.match(event)){
                arrowTranslateX+=inc;
                match = true;
            }
            if(match){
                JFXShapeManager.INSTANCE.transformSelected(AffineTransform.getTranslateInstance(arrowTranslateX, arrowTranslateY));
                event.consume();
            }
        }
        if((isDragging || hasArrowKeyMove || isVertexHandleDragging) && event.getCode() == KeyCode.ESCAPE){
            JFXShapeManager.INSTANCE.getSelectedShapes().forEach(JFXShape::cancelTransform);

            isDragging = false;
            hasArrowKeyMove = false;
            cancelVertexHandleDrag();
            arrowTranslateX = 0;
            arrowTranslateY = 0;
            event.consume();

        }
    }

    public void onKeyReleased(KeyEvent event){
        if(hasArrowKeyMove && up.match(event) || down.match(event) ||  left.match(event) || right.match(event)){
            JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
            hasArrowKeyMove = false;
            arrowTranslateX = 0;
            arrowTranslateY = 0;
        }
    }

    ////////////////////////////

    public Point2D transformSceneToDrawing(Point2D point2D){
        point2D = DrawingBotV3.project().displayMode.get().getRenderer().sceneToRenderer(point2D);
        point2D = point2D.multiply(1/DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas().getPlottingScale());
        return point2D;
    }

    public void onResizeHandlePressed(TransformModes resizeMode, MouseEvent event){
        this.resizeMode = resizeMode;
        if(!JFXShapeManager.INSTANCE.getSelectedShapes().isEmpty()){
            JFXShapeManager.INSTANCE.getSelectedShapes().forEach(JFXShape::startTransform);
            originalSize = createDrawingBoundingBox(JFXShapeManager.INSTANCE.getSelectedShapes());
            mouseOrigin = transformSceneToDrawing(new Point2D(event.getSceneX(), event.getSceneY()));
            isDragging = true;
            wasDragging = false;
        }
    }

    public void onResizeHandleDragged(TransformModes resizeMode, MouseEvent event){
        if(!isDragging){
            return;
        }
        showDraggingControls.set(true);
        wasDragging = true;

        AffineTransform transform = new AffineTransform();
        Point2D mousePoint = transformSceneToDrawing(new Point2D(event.getSceneX(), event.getSceneY()));

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
            double refAngle = Math.toRadians(Math.atan2(anchorPoint.getY() - mouseOrigin.getY(), anchorPoint.getX() - mouseOrigin.getX()) * 180 / Math.PI);
            double currentAngle = Math.toRadians(Math.atan2(anchorPoint.getY() - mousePoint.getY(), anchorPoint.getX() - mousePoint.getX()) * 180 / Math.PI);

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

    public void onResizeHandleReleased(TransformModes handle, MouseEvent event){
        if(isDragging){
            isDragging = false;
            JFXShapeManager.INSTANCE.runAction(JFXShapeManager.INSTANCE.confirmTransformAction());
            JFXShapeManager.INSTANCE.getSelectedShapes().forEach(JFXShape::finishTransform);

            if(enableRotation.get() && !wasDragging && handle == TransformModes.MOVE){
                showRotateControls.set(!showRotateControls.get());
            }
        }
        showDraggingControls.set(false);
    }

    ////////////////////////////

    //// EDIT TOOLS DRAWING \\\\

    public SimpleObjectProperty<JFXShape> drawingShape = new SimpleObjectProperty<>();
    public SimpleObjectProperty<PathElement> currentPathElement = new SimpleObjectProperty<>();
    private final Map<PathElement, List<Node>> drawingPathElementNodes = new HashMap<>();


    public static int vertexRect = 8;

    {

        // A listener to monitor changes to the drawing shapes elements to make sure they're vertex handle / tools are being rendered
        ListChangeListener<? super PathElement> listChangeListener = c -> {
            while(c.next()){
                for(PathElement removed : c.getRemoved()){
                    onPathElementRemoved(removed);
                }

                for(PathElement added : c.getAddedSubList()){
                    onPathElementAdded(drawingShape.get().jfxShape, added);
                }


                if(drawingShape.get() != null && drawingShape.get().jfxShape.getElements() != null && !drawingShape.get().jfxShape.getElements().isEmpty()){
                    currentPathElement.set(drawingShape.get().jfxShape.getElements().get(drawingShape.get().jfxShape.getElements().size()-1));
                }
            }
        };

        // The generic listener to attach the other listeners to the Drawing Shape and too make sure the Path Elements are fully loaded / unloaded.
        drawingShape.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                Path path = oldValue.jfxShape;
                path.getElements().forEach(this::onPathElementRemoved);
                path.getElements().removeListener(listChangeListener);
            }
            if(newValue != null){
                Path path = newValue.jfxShape;
                path.getElements().forEach(element -> onPathElementAdded(path, element));
                path.getElements().addListener(listChangeListener);
            }
        });
    }

    public void onPathElementAdded(Path path, PathElement element){
        List<Node> nodes = new ArrayList<>();
        if(element instanceof MoveTo){
            MoveTo moveTo = (MoveTo) element;
            Shape handle = setupVertexHandle(moveTo, new Rectangle(vertexRect, vertexRect), vertexRect, vertexRect, moveTo.xProperty(), moveTo.yProperty());

            handle.getStyleClass().add(SELECTION_CLOSE_HANDLE_STYLE_CLASS);
            handle.setOnMousePressed(e -> {
                if(e.isPrimaryButtonDown()){
                    PathElement lastElement = drawingShape.get().jfxShape.getElements().get(drawingShape.get().jfxShape.getElements().size()-1);
                    if(lastElement == moveTo || lastElement instanceof ClosePath){
                        onVertexHandlePressed(VertexHandleType.POINT, moveTo, e, moveTo.xProperty(), moveTo.yProperty());
                    }else {
                        closeDrawingShape();
                        handle.getStyleClass().remove(SELECTION_CLOSE_HANDLE_STYLE_CLASS);
                    }
                }
            });

            nodes.add(handle);
        }else if(element instanceof LineTo){
            LineTo lineTo = (LineTo) element;
            nodes.add(setupVertexHandle(lineTo, new Rectangle(vertexRect, vertexRect), vertexRect, vertexRect, lineTo.xProperty(), lineTo.yProperty()));
        }else if(element instanceof CubicCurveTo){
            CubicCurveTo curveTo = (CubicCurveTo) element;
            DoubleBinding[] endPointBinding = createSceneToDrawingBinding(curveTo.xProperty(), curveTo.yProperty());
            DoubleBinding[] ctrl2Binding = createSceneToDrawingBinding(curveTo.controlX2Property(), curveTo.controlY2Property());

            int index = path.getElements().indexOf(curveTo);
            PathElement prevElement = path.getElements().get(index-1);

            if(prevElement instanceof CubicCurveTo){
                //TODO FIX CURVE EDITING / RECREATE ISSUE WITH HEART SHAPE TYPE
                CubicCurveTo otherCurve = (CubicCurveTo) prevElement;
                curveTo.controlX1Property().bind(otherCurve.xProperty().add(otherCurve.xProperty().subtract(otherCurve.controlX2Property())));
                curveTo.controlY1Property().bind(otherCurve.yProperty().add(otherCurve.yProperty().subtract(otherCurve.controlY2Property())));
            }else{
                //curveTo.controlX1Property().bind(JFXAWTUtils.getXFromPathElement(prevElement));
                //curveTo.controlY1Property().bind(JFXAWTUtils.getYFromPathElement(prevElement));
            }

            BooleanBinding isSelected = currentPathElement.isEqualTo(curveTo);

            SimpleDoubleProperty offsetCtrlPointX = new SimpleDoubleProperty();
            SimpleDoubleProperty offsetCtrlPointY = new SimpleDoubleProperty();
            offsetCtrlPointX.bind(curveTo.xProperty().add(curveTo.xProperty().subtract(curveTo.controlX2Property())));
            offsetCtrlPointY.bind(curveTo.yProperty().add(curveTo.yProperty().subtract(curveTo.controlY2Property())));

            Line ctrl1Line = new Line();
            ctrl1Line.startXProperty().bind(endPointBinding[0]);
            ctrl1Line.startYProperty().bind(endPointBinding[1]);
            ctrl1Line.endXProperty().bind(endPointBinding[0].add(endPointBinding[0].subtract(ctrl2Binding[0])));
            ctrl1Line.endYProperty().bind(endPointBinding[1].add(endPointBinding[1].subtract(ctrl2Binding[1])));
            nodes.add(ctrl1Line);
            ctrl1Line.visibleProperty().bind(isSelected);

            Shape bezierCtrlVertexHandleSlave = setupVertexHandle(VertexHandleType.BEZIER_CTRL_SLAVE, curveTo, new Circle(vertexRect/2D), -1 + vertexRect/4D, -1 + vertexRect/4D, offsetCtrlPointX, offsetCtrlPointY, curveTo.controlX2Property(), curveTo.controlY2Property());
            bezierCtrlVertexHandleSlave.visibleProperty().bind(isSelected);
            nodes.add(bezierCtrlVertexHandleSlave);

            Line ctrl2Line = new Line();
            ctrl2Line.startXProperty().bind(endPointBinding[0]);
            ctrl2Line.startYProperty().bind(endPointBinding[1]);
            ctrl2Line.endXProperty().bind(ctrl2Binding[0]);
            ctrl2Line.endYProperty().bind(ctrl2Binding[1]);
            nodes.add(ctrl2Line);
            ctrl2Line.visibleProperty().bind(isSelected);

            Shape bezierCtrlVertexHandleMaster = setupVertexHandle(VertexHandleType.BEZIER_CTRL_MASTER, curveTo, new Circle(vertexRect/2D), -1 + vertexRect/4D, -1 + vertexRect/4D, curveTo.controlX2Property(), curveTo.controlY2Property());
            nodes.add(bezierCtrlVertexHandleMaster);
            bezierCtrlVertexHandleMaster.visibleProperty().bind(isSelected);
            nodes.add(setupVertexHandle(VertexHandleType.POINT, curveTo, new Rectangle(vertexRect, vertexRect), vertexRect, vertexRect, curveTo.xProperty(), curveTo.yProperty()));
        }
        vertexHandlesPane.getChildren().addAll(nodes);
        drawingPathElementNodes.put(element, nodes);
    }

    public void onPathElementRemoved(PathElement element){
        List<Node> nodes = drawingPathElementNodes.get(element);
        if(nodes != null && !nodes.isEmpty()){
            vertexHandlesPane.getChildren().removeAll(nodes);
        }
    }

    //////////////////////////////////////////////////////

    //// VERTEX HANDLE DRAGGING \\\\

    private boolean isVertexHandleDragging = false;
    private Point2D vertexMouseOrigin = null;
    private Point2D originalPosition = null;
    private Point2D originalCtrlPos = null;
    private DoubleProperty vertexX = null;
    private DoubleProperty vertexY = null;

    public enum VertexHandleType{
        POINT,
        BEZIER_CTRL_MASTER,
        BEZIER_CTRL_SLAVE;

        public boolean shouldMirror(){
            return this == BEZIER_CTRL_SLAVE;
        }
    }

    public void onVertexHandlePressed(VertexHandleType handleType, PathElement pathElement, MouseEvent event, DoubleProperty xAnchor, DoubleProperty yAnchor){
        if(!event.isPrimaryButtonDown()){
            return;
        }
        currentPathElement.set(pathElement);

        vertexMouseOrigin = transformSceneToDrawing(new Point2D(event.getSceneX(), event.getSceneY()));
        originalPosition = new Point2D(xAnchor.get(), yAnchor.get());
        vertexX = xAnchor;
        vertexY = yAnchor;
        isVertexHandleDragging = true;

        // Handle moving the control point along with the main point
        if(handleType == VertexHandleType.POINT && pathElement instanceof CubicCurveTo){
            CubicCurveTo cubicCurveTo = (CubicCurveTo) pathElement;
            originalCtrlPos = new Point2D(cubicCurveTo.getControlX2(), cubicCurveTo.getControlY2());
        }
    }

    public void onVertexHandleDragged(VertexHandleType handleType, PathElement pathElement, MouseEvent event, DoubleProperty xAnchor, DoubleProperty yAnchor){
        if(!isVertexHandleDragging){
            return;
        }
        Point2D mousePoint = transformSceneToDrawing(new Point2D(event.getSceneX(), event.getSceneY()));
        Point2D offset = mousePoint.subtract(vertexMouseOrigin);
        Point2D newPosition = !handleType.shouldMirror() ? originalPosition.add(offset.getX(), offset.getY()) : originalPosition.subtract(offset.getX(), offset.getY());

        xAnchor.set(newPosition.getX());
        yAnchor.set(newPosition.getY());

        // Handle moving the control point along with the main point
        if(handleType == VertexHandleType.POINT && pathElement instanceof CubicCurveTo){
            CubicCurveTo cubicCurveTo = (CubicCurveTo) pathElement;
            Point2D newCtrlPosition = originalCtrlPos.add(offset.getX(), offset.getY());

            cubicCurveTo.setControlX2(newCtrlPosition.getX());
            cubicCurveTo.setControlY2(newCtrlPosition.getY());
        }

    }

    public void onVertexHandleReleased(VertexHandleType handleType, PathElement pathElement, MouseEvent event, DoubleProperty xAnchor, DoubleProperty yAnchor){
        if(isVertexHandleDragging){
            //TODO UNDO / REDO HANDLING?
            if(drawingShape.get() != null){
                drawingShape.get().updateGeometryFromJFXShape();
            }
            resetVertexHandleDragData();
        }
    }

    public void cancelVertexHandleDrag(){
        if(isVertexHandleDragging){
            vertexX.set(originalPosition.getX());
            vertexY.set(originalPosition.getY());
            resetVertexHandleDragData();
        }
    }

    public void resetVertexHandleDragData(){
        isVertexHandleDragging = false;
        vertexMouseOrigin = null;
        originalPosition = null;
        originalCtrlPos = null;
        vertexX = null;
        vertexY = null;
    }

    public void closeDrawingShape(){
        drawingShape.get().addElement(new ClosePath());
        drawingShape.get().setDrawing(false);
        currentPathElement.set(null);
    }

    //////////////////////////////////////////////////////

    //// PATH ELEMENT DRAWING \\\\

    public boolean isDrawingPathElement = false;

    public void onMousePressed(MouseEvent event){
        if(toolMode.get() == ToolMode.DRAW_BEZIERS){
            if(event.isPrimaryButtonDown()){
                // Find the point relative to the drawing
                Point2D mousePoint = transformSceneToDrawing(new Point2D(event.getSceneX(), event.getSceneY()));

                ////////////////////////////////
                // Intersecting an existing shape with a new point TODO

                ////////////////////////////////
                // Placing point at the end of the shape

                // Should we start a new shape
                boolean newShape = false;
                if(drawingShape.get() == null){
                    newShape = true;
                }else if(drawingShape.get() != null){
                    Path path = drawingShape.get().jfxShape;
                    if(path.getElements().get(path.getElements().size()-1) instanceof ClosePath){
                        newShape = true;
                    }
                }

                // Create the new shape
                if(newShape){
                    JFXShape shape = new JFXShape(new GPath());
                    shape.setType(JFXShape.Type.ADD);
                    JFXShapeManager.INSTANCE.activeShapeList.get().addShapeLogged(shape);
                    JFXShapeManager.INSTANCE.deselectAll();
                    shape.setDrawing(true);
                    shape.setSelected(true); //this should set the drawing to be the active drawing shape
                    shape.updatePseudoClassState();
                }

                // Extend the shape with a new element, the drawing shape should be set now, but check it just in case.
                if(drawingShape.get() != null){
                    Path path = drawingShape.get().jfxShape;
                    if(path.getElements().isEmpty()){
                        drawingShape.get().addElement(new MoveTo(mousePoint.getX(), mousePoint.getY()));
                    }else{
                        LineTo lineTo = new LineTo(mousePoint.getX(), mousePoint.getY());
                        drawingShape.get().addTempNextElement(lineTo);
                        currentPathElement.set(lineTo);
                    }
                }
                event.consume();
                isDrawingPathElement = true;
            }
        }else{
            if(event.isPrimaryButtonDown()){
                JFXShapeManager.INSTANCE.deselectAll();
            }
        }
    }

    public void onMouseDragged(MouseEvent event) {
        if (isDrawingPathElement) {
            if(currentPathElement.get() instanceof LineTo){
                LineTo lineTo = (LineTo) currentPathElement.get();
                drawingShape.get().removeTempNextElement();
                CubicCurveTo cubicCurveTo = new CubicCurveTo(lineTo.getX(), lineTo.getY(), lineTo.getX(), lineTo.getY(), lineTo.getX(), lineTo.getY());
                drawingShape.get().addTempNextElement(cubicCurveTo);
                currentPathElement.set(cubicCurveTo);
                onVertexHandlePressed(VertexHandleType.BEZIER_CTRL_MASTER, cubicCurveTo, event, cubicCurveTo.controlX2Property(), cubicCurveTo.controlY2Property());
            }else if(currentPathElement.get() instanceof CubicCurveTo){
                CubicCurveTo cubicCurveTo = (CubicCurveTo) currentPathElement.get();
                onVertexHandleDragged(VertexHandleType.BEZIER_CTRL_MASTER, cubicCurveTo, event, cubicCurveTo.controlX2Property(), cubicCurveTo.controlY2Property());
            }
            event.consume();
        }
    }

    public void onMouseReleased(MouseEvent event) {
        if (isDrawingPathElement) {
            if(currentPathElement.get() instanceof CubicCurveTo){
                CubicCurveTo cubicCurveTo = (CubicCurveTo) currentPathElement.get();
                onVertexHandleReleased(VertexHandleType.BEZIER_CTRL_MASTER, cubicCurveTo, event, cubicCurveTo.controlX2Property(), cubicCurveTo.controlY2Property());
            }
            drawingShape.get().confirmTempNextElement();
            isDrawingPathElement = false;
            event.consume();
        }
    }

    ////////////////////////////

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
