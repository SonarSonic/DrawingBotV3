package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;

import java.util.List;
import java.util.stream.Collectors;

public class ActionGrouped implements IAction {

    public List<IAction> actions;

    public ActionGrouped(List<IAction> actions){
        this.actions = actions.stream().filter(a -> !a.isIdentity()).collect(Collectors.toList());
    }

    @Override
    public IAction invert() {
        return new ActionGrouped(ActionUtils.invertActionList(actions));
    }

    @Override
    public void apply() {
        actions.forEach(IAction::apply);
    }

    @Override
    public boolean isIdentity() {
        return actions.isEmpty();
    }
}
