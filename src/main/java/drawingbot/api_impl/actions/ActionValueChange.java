package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;
import drawingbot.api.actions.IActionTarget;
import drawingbot.api.actions.ValueChange;

import java.util.function.BiConsumer;

public class ActionValueChange<TARGET, TYPE> extends ActionTargetBase<TARGET> {

    public ValueChange<TYPE> change;
    public BiConsumer<TARGET, TYPE> apply;

    public ActionValueChange(IActionTarget<TARGET> actionTarget, BiConsumer<TARGET, TYPE> apply, TYPE oldValue, TYPE newValue) {
        this(actionTarget, apply, new ValueChange<>(oldValue, newValue));
    }

    public ActionValueChange(IActionTarget<TARGET> actionTarget, BiConsumer<TARGET, TYPE> apply, ValueChange<TYPE> change) {
        super(actionTarget);
        this.apply = apply;
        this.change = change;
    }

    @Override
    public IAction invert() {
        return new ActionValueChange<>(actionTarget, apply, change.invert());
    }

    public void apply(TARGET target){
        if(target != null){
            apply.accept(target, change.getNewValue());
        }
    }

}
