package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;

public class ActionIdentity implements IAction {

    @Override
    public IAction invert() {
        return this;
    }

    @Override
    public void apply() {}

    @Override
    public boolean isIdentity() {
        return true;
    }
}