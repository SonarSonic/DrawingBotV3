package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * A special Change Listener which can be attached to a {@link IProperties} and observe sub properties
 * Similarly to {@link NestedInvalidationListener} there are more reliable ways to observe multiple nested properties,
 */
public abstract class PropertyChangeListener<P extends IProperties> implements ListChangeListener<Observable> {

    private final Map<Object, PropertyChangeListener<IProperties>> subListeners = new HashMap<>();

    /**
     * The custom change function, called when a property is changed, it will provide the origin properites, and the observable value itself
     */
    public BiConsumer<P, Observable> onChange;

    private boolean nested = false;
    private BiFunction<IProperties, Observable, Boolean> propertyFilter = (properties, observable) -> true;

    private boolean attached;

    private PropertyChangeListener(BiConsumer<P, Observable> onChange) {
        this.onChange = onChange;
    }

    public abstract P getTarget();

    public PropertyChangeListener<P> setNested(boolean nested) {
        assert !attached;
        this.nested = nested;
        return this;
    }

    public PropertyChangeListener<P> setPropertyFilter(BiFunction<IProperties, Observable, Boolean> propertyFilter) {
        assert !attached;
        this.propertyFilter = propertyFilter;
        return this;
    }

    public PropertyChangeListener<P> attach(){
        attach(getTarget());
        return this;
    }

    protected void attach(P target){
        target.getPropertyList().addListener(this);
        attachSubListeners(target.getPropertyList());
        attached = true;
    }

    public void detach(){
        detach(getTarget());
    }

    protected void detach(P target){
        target.getPropertyList().removeListener(this);
        detachSubListeners(target.getPropertyList());
        attached = false;
    }

    private void attachSubListeners(List<? extends Observable> observables){
        if(!nested){
            return;
        }
        for(Observable added : observables){
            PropertyChangeListener<IProperties> listener = null;
            if(added instanceof IProperties && propertyFilter.apply(getTarget(), added)){
                listener = new PropertyChangeListener.Static<>((IProperties)added, this::onSubChange);
            }else if(added instanceof ObservableValue<?>){
                ObservableValue<?> value = (ObservableValue<?>) added;
                if(value instanceof IProperties && propertyFilter.apply(getTarget(), (Observable) value.getValue())){
                    listener = new PropertyChangeListener.Wrapped<>((ObservableValue<IProperties>) value, this::onSubChange);
                }
            }


            if(listener != null){
                listener.propertyFilter = propertyFilter;
                listener.nested = nested;
                listener.attach();
                subListeners.put(added, listener);
            }
        }
    }

    private void detachSubListeners(List<? extends Observable> observables){
        if(!nested){
            return;
        }
        for(Observable removed : observables){
            PropertyChangeListener<IProperties> listener = subListeners.remove(removed);
            if(listener != null){
                listener.detach();
            }
        }
    }

    private void onSubChange(IProperties subProps, Observable value){
        onChange.accept((P) subProps, value);
    }

    @Override
    public void onChanged(Change<? extends Observable> c) {
        while (c.next()) {
            if(nested){
                if(c.wasRemoved()){
                    detachSubListeners(c.getRemoved());
                }
                if(c.wasAdded()){
                    attachSubListeners(c.getAddedSubList());
                }
            }
            if (c.wasUpdated()) {
                for(int i = c.getFrom(); i < c.getTo(); i++){
                    onChange.accept(getTarget(), c.getList().get(i));
                }
            }
        }
    }

    public static class Static<P extends IProperties> extends PropertyChangeListener<P>{

        private final P target;

        private Static(P target, BiConsumer<P, Observable> onChange) {
            super(onChange);
            this.target = target;
        }

        @Override
        public P getTarget() {
            return target;
        }
    }

    public static class Wrapped<P extends IProperties> extends PropertyChangeListener<P>{

        private final ObservableValue<P> target;
        private ChangeListener<? super P> listener;

        private Wrapped(ObservableValue<P> target, BiConsumer<P, Observable> onChange) {
            super(onChange);
            this.target = target;
        }

        @Override
        public Wrapped<P> attach() {
            listener = (observable, oldValue, newValue) -> {
                if(oldValue != null){
                    detach(oldValue);
                }
                if(newValue != null){
                    attach(newValue);
                }
            };
            target.addListener(listener);
            attach(getTarget());
            return this;
        }

        @Override
        public void detach() {
            target.removeListener(listener);
            detach(getTarget());
        }

        @Override
        public P getTarget() {
            return target.getValue();
        }
    }
}