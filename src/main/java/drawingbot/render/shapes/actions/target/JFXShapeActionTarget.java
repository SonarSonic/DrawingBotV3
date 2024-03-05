package drawingbot.render.shapes.actions.target;

import drawingbot.api.actions.IActionTarget;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;

import java.util.UUID;

public class JFXShapeActionTarget implements IActionTarget<JFXShape> {

    public UUID listUUID;
    public UUID shapeUUID;

    public JFXShapeActionTarget(JFXShapeList list, JFXShape shape){
        this(list.getUUID(), shape.uuid);
    }

    public JFXShapeActionTarget(UUID listUUID, UUID shapeUUID){
        this.listUUID = listUUID;
        this.shapeUUID = shapeUUID;
    }

    @Override
    public JFXShape getTarget() {
        return JFXShapeList.getShape(listUUID, shapeUUID);
    }
}
