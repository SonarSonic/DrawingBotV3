package drawingbot.render.shapes;

import drawingbot.api_impl.actions.ActionManager;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.UUID;

public class JFXShapeList {

    private final UUID uuid;
    private final ObservableList<JFXShape> shapeList;

    // Don't try and edit these lists, use the provided setSelected / setDisplayed
    public final ObservableList<JFXShape> displayedShapes;
    public final ObservableList<JFXShape> selectedShapes;
    public final ActionManager actionManager = new ActionManager();

    public JFXShapeList(){
        this.uuid = UUID.randomUUID();
        this.shapeList = FXCollections.observableArrayList();
        this.displayedShapes = FXCollections.observableArrayList();
        this.selectedShapes = FXCollections.observableArrayList();
        JFXShapeManager.globalShapeListMap.put(uuid, this);

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
        actionManager.runAction(JFXShapeManager.INSTANCE.addGeometryAction(this, geometry));
    }

    /**
     * Allows the use of undo / redo
     */
    public void removeShapeLogged(JFXShape geometry){
        actionManager.runAction(JFXShapeManager.INSTANCE.removeGeometryAction(this, geometry));
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

}