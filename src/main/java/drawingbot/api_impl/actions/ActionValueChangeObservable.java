package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;
import drawingbot.api.actions.IActionTarget;
import drawingbot.api.actions.ValueChange;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;

import java.util.function.Function;

public class ActionValueChangeObservable<TARGET, TYPE, OBSERVABLE extends ObservableValue<TYPE> & ReadOnlyProperty<TYPE> & WritableValue<TYPE>> extends ActionTargetBase<TARGET> {

    public Function<TARGET, OBSERVABLE> provider;
    public ValueChange<TYPE> change;

    public ActionValueChangeObservable(IActionTarget<TARGET> actionTarget, Function<TARGET, OBSERVABLE> provider, TYPE oldValue, TYPE newValue) {
        this(actionTarget, provider, new ValueChange<>(oldValue, newValue));
    }

    public ActionValueChangeObservable(IActionTarget<TARGET> actionTarget, Function<TARGET, OBSERVABLE> provider, ValueChange<TYPE> change) {
        super(actionTarget);
        this.provider = provider;
        this.change = change;
    }

    @Override
    public IAction invert() {
        return new ActionValueChangeObservable<>(actionTarget, provider, change.invert());
    }

    public void apply(TARGET target){
        if(target == null){
            return;
        }
        OBSERVABLE value = provider.apply(target);
        if(value != null){
            value.setValue(change.getNewValue());
        }
    }
}
