package drawingbot.render.shapes.actions;

import drawingbot.api.actions.IAction;
import drawingbot.api.actions.IActionTarget;
import drawingbot.api_impl.actions.ActionTargetBase;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;

public class ActionAddShape extends ActionTargetBase<JFXShapeList> {

    public JFXShape shape;

    public ActionAddShape(IActionTarget<JFXShapeList> actionTarget, JFXShape shape) {
        super(actionTarget);
        this.shape = shape;
    }

    @Override
    public IAction invert() {
        return new ActionRemoveShape(actionTarget, shape);
    }

    @Override
    public void apply(JFXShapeList jfxShapeList) {
        jfxShapeList.addShape(shape);
    }
}
