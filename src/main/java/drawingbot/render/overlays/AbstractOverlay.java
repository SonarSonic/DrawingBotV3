package drawingbot.render.overlays;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractOverlay {

    public boolean wasActivated = false;
    public SimpleBooleanProperty active = new SimpleBooleanProperty(false){
        @Override
        protected void invalidated() {
            super.invalidated();
            if(get()){
                activate();
                wasActivated = true;
            }else{
                deactivate();
                wasActivated = false;
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

    public void postRender(){
        wasActivated = false;
    }

    protected void activate(){}

    protected void deactivate(){}

    public abstract String getName();

}