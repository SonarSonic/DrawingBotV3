package drawingbot.render.shapes.actions;

import drawingbot.api.actions.IAction;
import drawingbot.api.actions.IActionTarget;
import drawingbot.api_impl.actions.ActionTargetBase;
import drawingbot.render.shapes.JFXShape;

import java.awt.geom.AffineTransform;

public class ActionTransformShape extends ActionTargetBase<JFXShape> {

    public AffineTransform originalTransform;
    public AffineTransform newTransform;

    public ActionTransformShape(IActionTarget<JFXShape> actionTarget, AffineTransform newTransform) {
        super(actionTarget);
        this.originalTransform = actionTarget.getTarget().awtTransform;
        this.newTransform = newTransform;
    }

    @Override
    public IAction invert() {
        return new ActionTransformShape(actionTarget, originalTransform);
    }

    @Override
    public void apply(JFXShape jfxShape) {
        jfxShape.setAwtTransform(newTransform);
    }
}
