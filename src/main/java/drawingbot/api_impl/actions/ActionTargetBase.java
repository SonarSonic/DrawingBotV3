package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;
import drawingbot.api.actions.IActionTarget;

public abstract class ActionTargetBase<TARGET> implements IAction {

    public final IActionTarget<TARGET> actionTarget;

    public ActionTargetBase(IActionTarget<TARGET> actionTarget) {
        this.actionTarget = actionTarget;
    }

    @Override
    public final void apply() {
        apply(actionTarget.getTarget());
    }

    public abstract void apply(TARGET target);
}
