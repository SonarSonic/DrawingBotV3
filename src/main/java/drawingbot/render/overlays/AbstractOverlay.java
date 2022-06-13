package drawingbot.render.overlays;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractOverlay {

    public SimpleBooleanProperty active = new SimpleBooleanProperty(false){
        @Override
        protected void invalidated() {
            super.invalidated();
            if(get()){
                activate();
            }else{
                deactivate();
            }
        }
    };

    public abstract void init();

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public void preRender(){}

    public void doRender(){}

    public void postRender(){}

    protected void activate(){}

    protected void deactivate(){}

    public abstract String getName();

}