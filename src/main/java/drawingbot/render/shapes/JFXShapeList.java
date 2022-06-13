package drawingbot.render.shapes;

import drawingbot.api_impl.actions.ActionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class JFXShapeList {

    private final UUID uuid;
    private final ObservableList<JFXShape> shapeList;
    public final ActionManager actionManager = new ActionManager();

    public JFXShapeList(){
        this.uuid = UUID.randomUUID();
        this.shapeList = FXCollections.observableArrayList();
        JFXShapeManager.globalShapeListMap.put(uuid, this);
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
}