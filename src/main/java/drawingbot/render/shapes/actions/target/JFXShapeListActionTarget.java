package drawingbot.render.shapes.actions.target;

import drawingbot.api.actions.IActionTarget;
import drawingbot.render.shapes.JFXShapeList;

import java.util.UUID;

public class JFXShapeListActionTarget implements IActionTarget<JFXShapeList> {

    public UUID listUUID;

    public JFXShapeListActionTarget(JFXShapeList list){
        this(list.getUUID());
    }

    public JFXShapeListActionTarget(UUID listUUID){
        this.listUUID = listUUID;
    }

    @Override
    public JFXShapeList getTarget() {
        return JFXShapeList.getShapeList(listUUID);
    }
}
