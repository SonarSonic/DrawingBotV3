package drawingbot.render.shapes;

import drawingbot.api.actions.IAction;
import drawingbot.api_impl.actions.ActionGrouped;
import drawingbot.render.overlays.ShapeOverlays;
import drawingbot.render.shapes.actions.ActionAddShape;
import drawingbot.render.shapes.actions.ActionRemoveShape;
import drawingbot.render.shapes.actions.ActionTransformShape;
import drawingbot.render.shapes.actions.target.JFXShapeActionTarget;
import drawingbot.render.shapes.actions.target.JFXShapeListActionTarget;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;

import java.awt.geom.AffineTransform;
import java.util.*;

public class JFXShapeManager{

    public static final Map<UUID, JFXShapeList> globalShapeListMap = new HashMap<>();
    public static final JFXShapeManager INSTANCE = new JFXShapeManager();

    public final JFXShapeList globalShapeList = new JFXShapeList();
    public final SimpleBooleanProperty hasSelection = new SimpleBooleanProperty(false);
    public final SimpleObjectProperty<JFXShapeList> activeShapeList = new SimpleObjectProperty<>();

    public final ObservableList<JFXShape> displayedShapes = FXCollections.observableArrayList();
    public final ObservableList<JFXShape> selectedShapes = FXCollections.observableArrayList();
    public InvalidationListener boundsChangeListener = o -> dirtyBounds = true;
    public boolean dirtyBounds = true;

    {
        ListChangeListener<JFXShape> geometryListListener = this::onGeometryListChanged;
        activeShapeList.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.getShapeList().forEach(this::onGeometryRemoved);
                oldValue.getShapeList().removeListener(geometryListListener);
                selectedShapes.clear();
                displayedShapes.clear();
                //oldValue.actionManager.wipeHistory();
            }
            newValue = newValue == null ? globalShapeList : newValue;
            newValue.getShapeList().addListener(geometryListListener);
            newValue.getShapeList().forEach(this::onGeometryAdded);
        });
        activeShapeList.setValue(globalShapeList);

        displayedShapes.addListener(this::onDisplayedListChanged);
        selectedShapes.addListener(this::onSelectionListChanged);
    }

    ////////////////////////////

    public static JFXShapeList getShapeList(UUID uuid){
        return globalShapeListMap.get(uuid);
    }

    public static JFXShape getShape(UUID listUUID, UUID shapeUUID){
        JFXShapeList list = getShapeList(listUUID);
        if(list != null){
            for(JFXShape shape : list.getShapeList()){
                if(shape.uuid.equals(shapeUUID)){
                    return shape;
                }
            }
        }
        return null;
    }

    /**
     * Creates the required listeners to make the JFX geometry selectable
     * and activates the {@link JFXShape#selectedProperty()} & {@link JFXShape#displayedProperty()} ()}
     */
    public void initJFXGeometry(JFXShape geometry){
        geometry.selected.addListener((observable, oldValue, newValue) -> {
            if(activeShapeList.get().getShapeList().contains(geometry)){
                if(newValue){
                    selectedShapes.add(geometry);
                }else{
                    selectedShapes.remove(geometry);
                }
            }
        });

        geometry.displayed.addListener((observable, oldValue, newValue) -> {
            if(activeShapeList.get().getShapeList().contains(geometry)){
                if(newValue){
                    displayedShapes.add(geometry);
                }else{
                    geometry.setSelected(false);
                    displayedShapes.remove(geometry);
                }
            }
        });

        geometry.jfxShape.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> onSelectableClicked(geometry, e));
    }

    public void onSelectableClicked(JFXShape shape, MouseEvent event){
        if(shape.isSelectable() && shape.isDisplayed() && !shape.isSelected()){
            if(!event.isControlDown() && !event.isShiftDown()){
                deselectAll();
                ShapeOverlays.INSTANCE.showRotateControls.set(false);
            }
            shape.setSelected(true);
        }
    }

    ////////////////////////////

    public void onGeometryListChanged(ListChangeListener.Change<? extends JFXShape> c){
        while(c.next()){
            for(JFXShape removed : c.getRemoved()){
                onGeometryRemoved(removed);
            }

            for(JFXShape added : c.getAddedSubList()){
                onGeometryAdded(added);
            }
        }
    }

    public void onGeometryAdded(JFXShape added){
        if(added.isSelected()){
            selectedShapes.add(added);
        }
        if(added.isDisplayed()){
            displayedShapes.add(added);
        }
    }

    public void onGeometryRemoved(JFXShape removed){
        if(removed.isSelected()) {
            selectedShapes.remove(removed);
        }
        if(removed.isDisplayed()){
            displayedShapes.remove(removed);
        }
    }

    ////////////////////////////

    public void onDisplayedListChanged(ListChangeListener.Change<? extends JFXShape> c){
        ShapeOverlays.INSTANCE.geometriesPane.getChildren().clear();

        for(JFXShape shape : activeShapeList.get().getShapeList()){
            if(shape.isDisplayed()){
                ShapeOverlays.INSTANCE.geometriesPane.getChildren().add(shape.jfxShape);
            }
        }
    }

    ////////////////////////////

    public void onSelectionListChanged(ListChangeListener.Change<? extends JFXShape> c){
        while(c.next()){
            for(JFXShape removed : c.getRemoved()){
                onSelectionRemoved(removed);
            }

            for(JFXShape added : c.getAddedSubList()){
                onSelectionAdded(added);
            }
        }
        hasSelection.set(!selectedShapes.isEmpty());
    }

    public void onSelectionAdded(JFXShape added){
        added.jfxShape.boundsInParentProperty().addListener(boundsChangeListener);
    }

    public void onSelectionRemoved(JFXShape removed){
        removed.jfxShape.boundsInParentProperty().removeListener(boundsChangeListener);
    }

    ////////////////////////////

    public void runAction(IAction action){
        activeShapeList.get().actionManager.runAction(action);
    }

    public void showAll(){
        activeShapeList.get().getShapeList().forEach(g -> g.setDisplayed(true));
    }

    public void hideAll(){
        List.copyOf(activeShapeList.get().getShapeList()).forEach(g -> g.setDisplayed(false));
    }

    public void selectAll(){
        activeShapeList.get().getShapeList().forEach(g -> g.setSelected(true));
    }

    public void deselectAll(){
        List.copyOf(selectedShapes).forEach(g -> g.setSelected(false));
    }

    public void deleteSelected(){
        runAction(deleteSelectedAction());
    }

    public IAction deleteSelectedAction(){
        List<IAction> actions = new ArrayList<>();
        for(JFXShape shape : selectedShapes){
            actions.add(removeGeometryAction(activeShapeList.get(), shape));
        }
        return new ActionGrouped(actions);
    }

    public void transformSelected(AffineTransform transform){
        selectedShapes.forEach(s -> s.transform(transform));
    }

    public IAction addGeometryAction(JFXShapeList list, JFXShape shape){
        return new ActionAddShape(new JFXShapeListActionTarget(list), shape);
    }

    public IAction removeGeometryAction(JFXShapeList list, JFXShape shape){
        return new ActionRemoveShape(new JFXShapeListActionTarget(list), shape);
    }

    public IAction confirmTransformAction(){
        List<IAction> actions = new ArrayList<>();
        for(JFXShape shape : selectedShapes){
            if(shape.getLiveTransform() != null){
                actions.add(new ActionTransformShape(new JFXShapeActionTarget(activeShapeList.get(), shape), shape.getLiveTransform()));
            }
        }
        return new ActionGrouped(actions);
    }

    public static final int TRANSLATE = 0, SCALE = 1, ROTATE = 2, SKEW = 3;
}
