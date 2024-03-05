package drawingbot.render.shapes;

import drawingbot.api.actions.IAction;
import drawingbot.api_impl.actions.ActionGrouped;
import drawingbot.api_impl.actions.ActionManager;
import drawingbot.render.shapes.actions.ActionAddShape;
import drawingbot.render.shapes.actions.ActionRemoveShape;
import drawingbot.render.shapes.actions.ActionTransformShape;
import drawingbot.render.shapes.actions.target.JFXShapeActionTarget;
import drawingbot.render.shapes.actions.target.JFXShapeListActionTarget;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.awt.geom.AffineTransform;
import java.util.*;

public class JFXShapeList {

    public static final Map<UUID, JFXShapeList> globalShapeListMap = new HashMap<>();

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

    private final UUID uuid;
    private final ObservableList<JFXShape> shapeList;

    // Don't try and edit these lists, use the provided setSelected / setDisplayed
    public final ObservableList<JFXShape> displayedShapes;
    public final ObservableList<JFXShape> selectedShapes;
    public final BooleanProperty hasSelection = new SimpleBooleanProperty();
    public final ActionManager actionManager = new ActionManager();

    public JFXShapeList(){
        this.uuid = UUID.randomUUID();
        this.shapeList = FXCollections.observableArrayList();
        this.displayedShapes = FXCollections.observableArrayList();
        this.selectedShapes = FXCollections.observableArrayList();
        this.hasSelection.bind(Bindings.isNotEmpty(selectedShapes));
        shapeList.addListener((ListChangeListener<JFXShape>) c -> {
            while(c.next()){
                for(JFXShape removed : c.getRemoved()){
                    onShapeRemoved(removed);
                }

                for(JFXShape added : c.getAddedSubList()){
                    onShapeAdded(added);
                }
            }
        });
        globalShapeListMap.put(uuid, this);
    }

    private void onShapeAdded(JFXShape shape){
        if(shape.isSelected()){
            selectedShapes.add(shape);
        }
        if(shape.isDisplayed()){
            displayedShapes.add(shape);
        }
        shape.selected.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                selectedShapes.add(shape);
            }else{
                selectedShapes.remove(shape);
            }
        });
        shape.displayed.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                displayedShapes.add(shape);
            }else{
                shape.setSelected(false); //TODO hide overlays another way, which keeps the selection
                displayedShapes.remove(shape);
            }
        });
    }

    private void onShapeRemoved(JFXShape shape){
        selectedShapes.remove(shape);
        displayedShapes.remove(shape);
    }

    public void addShape(JFXShape shape) {
        shapeList.add(shape);
    }

    public void removeShape(JFXShape shape) {
        shapeList.remove(shape);
    }

    /**
     * Allows the use of undo / redo
     */
    public void addShapeLogged(JFXShape geometry){
        actionManager.runAction(addGeometryAction(geometry));
    }

    /**
     * Allows the use of undo / redo
     */
    public void removeShapeLogged(JFXShape geometry){
        actionManager.runAction(removeGeometryAction(geometry));
    }


    public void showAll(){
        getShapeList().forEach(g -> g.setDisplayed(true));
    }

    public void hideAll(){
        List.copyOf(getShapeList()).forEach(g -> g.setDisplayed(false));
    }

    public void selectAll(){
        getShapeList().forEach(g -> g.setSelected(true));
    }

    public void deselectAll(){
        List.copyOf(getSelectionList()).forEach(g -> g.setSelected(false));
    }

    public void deleteSelected(){
        actionManager.runAction(deleteSelectedAction());
    }

    public IAction deleteSelectedAction(){
        List<IAction> actions = new ArrayList<>();
        for(JFXShape shape : getSelectionList()){
            actions.add(removeGeometryAction(shape));
        }
        return new ActionGrouped(actions);
    }

    public void transformSelected(AffineTransform transform){
        getSelectionList().forEach(s -> s.transform(transform));
    }

    public IAction addGeometryAction(JFXShape shape){
        return new ActionAddShape(new JFXShapeListActionTarget(this), shape);
    }

    public IAction removeGeometryAction(JFXShape shape){
        return new ActionRemoveShape(new JFXShapeListActionTarget(this), shape);
    }

    public IAction confirmTransformAction(){
        List<IAction> actions = new ArrayList<>();
        for(JFXShape shape : getSelectionList()){
            if(shape.getLiveTransform() != null){
                actions.add(new ActionTransformShape(new JFXShapeActionTarget(this, shape), shape.getLiveTransform()));
            }
        }
        return new ActionGrouped(actions);
    }

    public IAction setTransformAction(AffineTransform transform){
        List<IAction> actions = new ArrayList<>();
        for(JFXShape shape : getSelectionList()){
            actions.add(new ActionTransformShape(new JFXShapeActionTarget(this, shape), transform));
        }
        return new ActionGrouped(actions);
    }

    public UUID getUUID() {
        return uuid;
    }

    public ObservableList<JFXShape> getShapeList() {
        return shapeList;
    }

    public ObservableList<JFXShape> getSelectionList() {
        return selectedShapes;
    }

    public ObservableList<JFXShape> getDisplayedShapes() {
        return displayedShapes;
    }


    public void runAction(IAction action) {
        actionManager.runAction(action);
    }
}