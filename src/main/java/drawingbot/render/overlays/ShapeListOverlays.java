package drawingbot.render.overlays;

import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;
import drawingbot.render.viewport.Viewport;
import drawingbot.utils.UnitsLength;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;

/**
 * A viewport overlay which supports the rendering of a {@link JFXShapeList} relative to the current {@link drawingbot.api.ICanvas}
 * It can also be overridden for other viewport overlays like {@link ShapeListEditingOverlays}
 */
public class ShapeListOverlays extends ViewportOverlayBase {


    public InvalidationListener updateCanvasListener;

    public ShapeListOverlays(){

        ListChangeListener<JFXShape> displayedListChangeListener = c -> {
            while (c.next()){
                c.getRemoved().forEach(ShapeListOverlays.this::onShapeHidden);
                c.getAddedSubList().forEach(ShapeListOverlays.this::onShapeDisplayed);
            }
        };

        ListChangeListener<JFXShape> selectedListChangeListener = c -> {
            while (c.next()){
                c.getRemoved().forEach(ShapeListOverlays.this::onShapeDeselected);
                c.getAddedSubList().forEach(ShapeListOverlays.this::onShapeSelected);
            }
        };

        ListChangeListener<JFXShape> shapeListChangeListener = c -> {
            while (c.next()){
                c.getRemoved().forEach(ShapeListOverlays.this::onShapeRemoved);
                c.getAddedSubList().forEach(ShapeListOverlays.this::onShapeAdded);
            }
        };

        updateCanvasListener = observable -> updateMaskToCanvasTransform();

        activeList.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.getDisplayedShapes().removeListener(displayedListChangeListener);
                oldValue.getDisplayedShapes().forEach(this::onShapeHidden);

                oldValue.getSelectionList().removeListener(selectedListChangeListener);
                oldValue.getSelectionList().forEach(this::onShapeDeselected);

                oldValue.getShapeList().removeListener(shapeListChangeListener);
                oldValue.getShapeList().forEach(this::onShapeRemoved);

                hasSelection.unbind();
            }
            if(newValue != null){
                newValue.getDisplayedShapes().addListener(displayedListChangeListener);
                newValue.getDisplayedShapes().forEach(this::onShapeDisplayed);

                newValue.getSelectionList().addListener(selectedListChangeListener);
                newValue.getSelectionList().forEach(this::onShapeSelected);

                newValue.getShapeList().addListener(shapeListChangeListener);
                newValue.getShapeList().forEach(this::onShapeAdded);

                hasSelection.bind(Bindings.isNotEmpty(newValue.getSelectionList()));
            }
        });

        shapesUnits.addListener((observable, oldValue, newValue) -> {
            updateMaskToCanvasTransform();
        });

    }

    ////////////////////////////////////////////////////////


    public final ObjectProperty<JFXShapeList> activeList = new SimpleObjectProperty<>();

    public JFXShapeList getActiveList() {
        return activeList.get();
    }

    public ObjectProperty<JFXShapeList> activeListProperty() {
        return activeList;
    }

    public void setActiveList(JFXShapeList activeList) {
        this.activeList.set(activeList);
    }


    ////////////////////////////////////////////////////////

    public final BooleanProperty hasSelection = new SimpleBooleanProperty(false);

    public boolean isHasSelection() {
        return hasSelection.get();
    }

    public BooleanProperty hasSelectionProperty() {
        return hasSelection;
    }

    public void setHasSelection(boolean hasSelection) {
        this.hasSelection.set(hasSelection);
    }

    public final ObjectProperty<UnitsLength> shapesUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public UnitsLength getShapesUnits() {
        return shapesUnits.get();
    }

    public ObjectProperty<UnitsLength> shapesUnitsProperty() {
        return shapesUnits;
    }

    public void setShapesUnits(UnitsLength shapesUnits) {
        this.shapesUnits.set(shapesUnits);
    }

    ////////////////////////////////////////////////////////

    public DoubleProperty strokeWidth = new SimpleDoubleProperty(1);

    public double getStrokeWidth() {
        return strokeWidth.get();
    }

    public DoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }

    ////////////////////////////////////////////////////////

    public Affine maskToCanvasTransform = new Affine();

    public Affine getMaskToCanvasTransform(){
        return maskToCanvasTransform;
    }

    private void updateMaskToCanvasTransform(){
        maskToCanvasTransform.setToIdentity();
        if(getViewport() != null){
            double scale = shapesUnits.get().convertToMM / getViewport().getCanvasUnits().convertToMM;
            maskToCanvasTransform.appendScale(scale, scale);
        }
    }


    @Override
    public void initOverlay() {
        //NOP
    }

    @Override
    public void activateViewportOverlay(Viewport viewport) {
        super.activateViewportOverlay(viewport);

        strokeWidth.bind(Bindings.createDoubleBinding(() -> getViewport().getCanvasToViewportScale() * (viewport.getCanvasUnits().convertToMM /shapesUnits.get().convertToMM), shapesUnits, viewport.canvasUnitsProperty(), viewport.canvasToViewportScaleProperty()));
        viewport.canvasUnitsProperty().addListener(updateCanvasListener);
        updateMaskToCanvasTransform();

        activeList.get().getDisplayedShapes().forEach(this::onShapeDisplayed);
    }

    @Override
    public void deactivateViewportOverlay(Viewport viewport) {
        activeList.get().getDisplayedShapes().forEach(this::onShapeHidden); //Run it first before the viewport is set to null
        viewport.canvasUnitsProperty().removeListener(updateCanvasListener);
        super.deactivateViewportOverlay(viewport);
    }

    public void onShapeAdded(JFXShape added){
        //NOP
    }

    public void onShapeRemoved(JFXShape removed){
        //NOP
    }


    public void onShapeSelected(JFXShape select){
        //NOP
    }

    public void onShapeDeselected(JFXShape deselect){
        //NOP
    }

    public void onShapeDisplayed(JFXShape shape){
        if(getViewport() == null){
            return;
        }
        shape.jfxShape.getTransforms().add(getViewport().getCanvasToViewportTransform());
        shape.jfxShape.getTransforms().add(getMaskToCanvasTransform());
        shape.jfxShape.strokeWidthProperty().bind(strokeWidthProperty());
        shape.jfxShape.setOnMousePressed(e -> onSelectableClicked(shape, e));
        getViewport().getBackgroundOverlayNodes().add(shape.jfxShape);
    }

    public void onShapeHidden(JFXShape shape){
        if(getViewport() == null){
            return;
        }

        shape.jfxShape.getTransforms().remove(getViewport().getCanvasToViewportTransform());
        shape.jfxShape.getTransforms().remove(getMaskToCanvasTransform());
        shape.jfxShape.strokeWidthProperty().unbind();
        shape.jfxShape.setOnMousePressed(null);
        getViewport().getBackgroundOverlayNodes().remove(shape.jfxShape);
    }

    public void onSelectableClicked(JFXShape shape, MouseEvent event){
        if(event.isPrimaryButtonDown() && shape.isSelectable() && shape.isDisplayed() && !shape.isSelected()){

            if(!event.isControlDown() && !event.isShiftDown()){
                activeList.get().deselectAll();
            }

            shape.setSelected(true);
            //getViewport().requestFocus();
            event.consume();
        }
    }

    @Override
    public void onRenderTick() {
        super.onRenderTick();
        //NOP
    }

    @Override
    public String getName() {
        return "Generic Shape Overlays";
    }
}
